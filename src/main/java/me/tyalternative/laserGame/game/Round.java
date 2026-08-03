package me.tyalternative.laserGame.game;

import me.tyalternative.laserGame.arena.Arena;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.economy.CurrencySource;
import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.weapon.WeaponAttributeKeys;
import me.tyalternative.laserGame.weapon.WeaponItemFactory;
import me.tyalternative.laserGame.weapon.WeaponType;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Round {

    private final Plugin plugin;
    private final ConfigManager config;
    private final Arena arena;
    private final Map<UUID, GamePlayer> players;
    private final RoundEndCallback endCallback;

    private final Map<UUID, Integer> occupiedSpawns = new LinkedHashMap<>();

    public interface RoundEndCallback {
        void onRoundEnded(GamePlayer winner);
    }

    public Round(Plugin plugin, ConfigManager config, Arena arena,
                 Map<UUID, GamePlayer> players, RoundEndCallback endCallback) {
        this.plugin = plugin;
        this.config = config;
        this.arena = arena;
        this.players = players;
        this.endCallback = endCallback;
    }

    public void start() {
        for (GamePlayer gp : players.values()) {
            gp.addCurrency(config.getPassivePerRound(), CurrencySource.PASSIVE);
            Player player = gp.getPlayer();
            if (player != null) {
                enterRound(gp, player);
            }
        }
        broadcast("§aGO!");
    }

    private void enterRound(GamePlayer gp, Player player) {
        WeaponType type = gp.getWeaponType();

        player.getInventory().clear();
        player.getInventory().setItem(0, WeaponItemFactory.create(plugin, type));
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(allocateRandomSpawn(gp.getUuid()));

        applyLivesToHealthBar(player, gp.getLives());
        applyWeaponSpeedModifier(player, gp);
        gp.getWeapon().start();
    }

    private Location allocateRandomSpawn(UUID playerUuid) {
        int total = arena.getConfig().spawns().size();
        List<Integer> free = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            if (!occupiedSpawns.containsValue(i)) {
                free.add(i);
            }
        }
        int index = free.isEmpty()
                ? ThreadLocalRandom.current().nextInt(total)
                : free.get(ThreadLocalRandom.current().nextInt(free.size()));

        occupiedSpawns.put(playerUuid, index);
        return arena.getConfig().spawns().get(index);
    }

    private void applyLivesToHealthBar(Player player, int lives) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr == null) return;
        double hp = Math.max(lives, 1) * 2.0;
        maxHealthAttr.setBaseValue(hp);
        player.setHealth(hp);
    }

    private void applyWeaponSpeedModifier(Player player, GamePlayer gp) {
        AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        NamespacedKey key = WeaponAttributeKeys.speedModifier(plugin);
        speedAttr.removeModifier(key);

        double modifier = gp.getEffectiveStats().getMovementSpeedModifier();
        if (modifier != 0.0) {
            speedAttr.addModifier(new AttributeModifier(
                    key, modifier, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }
    }

    public void onPlayerHit(GamePlayer shooter, GamePlayer target, int baseLivesToRemoves) {
        HitResolutionContext ctx = new HitResolutionContext(shooter, target, baseLivesToRemoves);
        shooter.getEffects().fireShotHit(ctx);
        shooter.getEffects().fireDamageTaken(ctx);

        boolean eliminated = target.removeLife(ctx.livesToRemove);
        Player targetPlayer = target.getPlayer();

        shooter.addCurrency(
                eliminated ? config.getEliminationReward() : config.getShotHitReward(),
                eliminated ? CurrencySource.ELIMINATION : CurrencySource.SHOT_HIT);

        if (eliminated) {
            target.setSpectator(true);
            target.getWeapon().stop();
            occupiedSpawns.remove(target.getUuid());
            if (targetPlayer != null) {
                targetPlayer.setGameMode(GameMode.SPECTATOR);
                targetPlayer.teleport(arena.getConfig().spectatorSpawn());
                resetPlayerAttributes(targetPlayer);
            }
            broadcast("§c" + (targetPlayer != null ? targetPlayer.getName() : "Un joueur") + " est éliminé !");
            checkVictoryCondition();
        } else {
            if (targetPlayer != null) {
                applyLivesToHealthBar(targetPlayer, target.getLives());
            }
            scheduleRespawn(target);
        }
    }

    private void scheduleRespawn(GamePlayer gp) {
        Player player = gp.getPlayer();
        gp.getWeapon().stop();
        if (player != null) {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(arena.getConfig().spectatorSpawn());
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = gp.getPlayer();
            if (p == null || gp.isSpectator()) return; // déco, ou déjà éliminé/round terminé entre-temps

            enterRound(gp, p);
        }, config.getRespawnDelayTicks());
    }

    private void resetPlayerAttributes(Player player) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(20.0);
        }
        player.setHealth(20.0);

        AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(WeaponAttributeKeys.speedModifier(plugin));
        }
    }

    public void onPlayerRemoved(GamePlayer gp) {
        occupiedSpawns.remove(gp.getUuid());
        checkVictoryCondition();
    }

    private void checkVictoryCondition() {
        List<GamePlayer> alive = players.values().stream()
                .filter(p -> !p.isSpectator())
                .toList();

        if (alive.size() <= 1) {
            for (GamePlayer gp : players.values()) {
                gp.getWeapon().stop();
            }
            GamePlayer winner = alive.isEmpty() ? null : alive.get(0);
            if (winner != null) {
                Player p = winner.getPlayer();
                broadcast("§6" + (p != null ? p.getName() : "???") + " remporte le round !");
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> endCallback.onRoundEnded(winner), 40L);
        }
    }

    private void broadcast(String message) {
        for (GamePlayer gp : players.values()) {
            Player p = gp.getPlayer();
            if (p != null) {
                p.sendMessage(message);
            }
        }
    }
}
