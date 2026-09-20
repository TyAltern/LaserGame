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
    private final List<GamePlayer> alivePlayers = new ArrayList<>();
    private final RoundEndCallback endCallback;

    private final Map<UUID, Integer> occupiedSpawns = new LinkedHashMap<>();
    private boolean firstBloodClaimed = false;
    private boolean firstEliminationClaimed = false;
    private org.bukkit.scheduler.BukkitTask sneakChargeTask;
    private org.bukkit.scheduler.BukkitTask radarCooldownTask;

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
        firstBloodClaimed = false;
        firstEliminationClaimed = false;
        alivePlayers.clear();
        for (GamePlayer gp : players.values()) {
            gp.addCurrency(config.getPassivePerRound(), CurrencySource.PASSIVE);

            gp.getEffects().fireRoundStart();
            alivePlayers.add(gp);
            Player player = gp.getPlayer();
            if (player != null) {
                enterRound(gp, player);
            }
        }
        broadcast("§aGO!");

        radarCooldownTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (GamePlayer gamePlayer : players.values()) {
                gamePlayer.setCanSeeRadar(true);
            }
        }, config.getRadarStartingCooldownTicks());
        sneakChargeTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickSneakCharges, config.getSneakRegenIntervalTicks(), config.getSneakRegenIntervalTicks());
    }

    private void tickSneakCharges() {
        for (GamePlayer gp : players.values()) {
            if (gp.isSpectator()) continue;
            Player player = gp.getPlayer();
            if (player == null) continue;

            boolean sneaking = player.isSneaking();
            gp.tickSneakCharge(sneaking, config.getSneakRegenIntervalTicks());

            if (sneaking && !gp.hasSneakCharge()) {
                player.setSneaking(false);
                player.sendMessage("§cPlus de charge pour rester accroupi !");
            }
        }
    }

    public void roundEnd() {
        if (sneakChargeTask != null) {
            sneakChargeTask.cancel();
            sneakChargeTask = null;
        }
        if (radarCooldownTask != null) {
            radarCooldownTask.cancel();
            radarCooldownTask = null;
        }

        for (GamePlayer gp : players.values()) {
            revokeSneakLockLogic(gp);
            gp.setCanSeeRadar(false);
        }
    }

    public void stop() {
        roundEnd();
    }

    private void enterRound(GamePlayer gp, Player player) {
        WeaponType type = gp.getWeaponType();

        player.getInventory().clear();
        player.getInventory().setItem(4, WeaponItemFactory.create(plugin, type));
        player.getInventory().setHeldItemSlot(4);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(allocateRandomSpawn(gp.getUuid()));

        applyLivesToHealthBar(player, gp.getLives());
        applyWeaponSpeedModifier(player, gp);
        applySneakLockLogic(gp);
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

    private void applySneakLockLogic(GamePlayer gp) {
        gp.setSneakChargeTicks(config.getMaxSneakChargeTicks());
        AttributeModifier modifier = gp.getSneakSpeedModifier();

        if (modifier == null) {
            modifier = new AttributeModifier(new NamespacedKey("sneak_speed_modifier",gp.getUuid().toString()),-1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
            gp.setSneakSpeedModifier(modifier);
        }

        AttributeInstance sneakAttr = gp.getPlayer().getAttribute(Attribute.SNEAKING_SPEED);
        if (sneakAttr != null) {
            if (!sneakAttr.getModifiers().contains(modifier)) sneakAttr.addModifier(modifier);
        }
        gp.setSneakTimed(true);
    }

    private void revokeSneakLockLogic(GamePlayer gp) {
        AttributeModifier modifier = gp.getSneakSpeedModifier();

        if (modifier == null) {
            modifier = new AttributeModifier(new NamespacedKey("sneak_speed_modifier",gp.getUuid().toString()),-1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
            gp.setSneakSpeedModifier(modifier);
        }

        AttributeInstance sneakAttr = gp.getPlayer().getAttribute(Attribute.SNEAKING_SPEED);
        if (sneakAttr != null) {
            if (sneakAttr.getModifiers().contains(modifier)) sneakAttr.removeModifier(modifier);
        }
        gp.setSneakTimed(false);
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

    public boolean hasFirstEliminationOccurred() { return firstEliminationClaimed; }

    public void onPlayerHit(GamePlayer shooter, GamePlayer target, int baseLivesToRemoves, double shotDistance) {
        HitResolutionContext ctx = new HitResolutionContext(shooter, target, baseLivesToRemoves, shotDistance);
        ctx.wouldBeFirstElimination = !firstEliminationClaimed && target.getLives() - ctx.livesToRemove <= 0;
        shooter.getEffects().fireShotHit(ctx);
        ctx.wouldBeFirstElimination = !firstEliminationClaimed && target.getLives() - ctx.livesToRemove <= 0;
        target.getEffects().fireDamageTaken(ctx);

        shooter.incrementKills();

        if (!firstBloodClaimed) {
            firstBloodClaimed = true;
            shooter.addCurrency(config.getFirstBloodReward(), CurrencySource.FIRST_BLOOD);
            Player shooterPlayer = shooter.getPlayer();
            if (shooterPlayer != null) {
                shooterPlayer.sendMessage("§6Premier sang ! +" + config.getFirstBloodReward() + "$");
            }
        }

        boolean eliminated = target.removeLife(ctx.livesToRemove);
        Player targetPlayer = target.getPlayer();

        shooter.addCurrency(
                eliminated ? config.getEliminationReward() : config.getShotHitReward(),
                eliminated ? CurrencySource.ELIMINATION : CurrencySource.SHOT_HIT);

        if (eliminated) {
            onPlayerElimination(shooter, target, shotDistance);
        } else {
            if (targetPlayer != null) {
                applyLivesToHealthBar(targetPlayer, target.getLives());
            }
            scheduleRespawn(target);
        }
    }

    public void onPlayerElimination(GamePlayer shooter, GamePlayer target, double shotDistance) {
        Player targetPlayer = target.getPlayer();
        shooter.incrementEliminations();

        if (!firstEliminationClaimed) {
            firstEliminationClaimed = true;
            shooter.addCurrency(config.getFirstEliminationReward(), CurrencySource.FIRST_ELIMINATION);
            Player shooterPlayer = shooter.getPlayer();
            if (shooterPlayer != null) {
                shooterPlayer.sendMessage("§6Première élimination ! +" + config.getFirstEliminationReward() + "$");
            }
        }

        if (target.getPendingBetType() == GamePlayer.BetType.WIN_X4 && target.getPendingBetAmount() > 0) {
            int payout = target.getPendingBetAmount();
            shooter.addCurrency(payout, CurrencySource.BET_PAYOUT);
            Player shooterPlayer = shooter.getPlayer();
            if (shooterPlayer != null) {
                shooterPlayer.sendMessage("§6" + (targetPlayer != null ? targetPlayer.getName() : "Ta cible")
                        + " avait parié : tu récupères " + payout + "$ !");
            }
        }
        target.clearBet();

        target.setSpectator(true);
        target.getWeapon().stop();
        occupiedSpawns.remove(target.getUuid());
        alivePlayers.remove(target);
        if (targetPlayer != null) {
            targetPlayer.setGameMode(GameMode.SPECTATOR);
            targetPlayer.teleport(arena.getConfig().spectatorSpawn());
            resetPlayerAttributes(targetPlayer);
        }
        broadcast("§c" + (targetPlayer != null ? targetPlayer.getName() : "Un joueur") + " est éliminé !");
        checkVictoryCondition();
    }

    public void applyExtraDamage(GamePlayer target, int amount) {
        if (amount <= 0 || target.isSpectator()) return;

        boolean eliminated = target.removeLife(amount); // TODO: add item protection from it (ex: damage_cap)
        Player targetPlayer = target.getPlayer();

        if (eliminated) {
            target.setSpectator(true);
            target.getWeapon().stop();
            occupiedSpawns.remove(target.getUuid());
            if (targetPlayer != null) {
                targetPlayer.setGameMode(GameMode.SPECTATOR);
                targetPlayer.teleport(arena.getConfig().spectatorSpawn());
                resetPlayerAttributes(targetPlayer);
            }
            target.clearBet();
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
            gp.getEffects().fireRespawn();
        }, config.getRespawnDelayTicks());
    }

    public void simulateElimination(GamePlayer gp) {
        Player player = gp.getPlayer();
        if (player == null || gp.isSpectator()) return;

        broadcast("§c" + player.getName() + " est éliminé !");
        gp.getWeapon().stop();
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(arena.getConfig().spectatorSpawn());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = gp.getPlayer();
            if (p == null || gp.isSpectator()) return; // déco, ou vraie élimination/round terminé entre-temps

            enterRound(gp, p);
            gp.getEffects().fireRespawn();
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

        AttributeInstance sneakAttr = player.getAttribute(Attribute.SNEAKING_SPEED);
        if (sneakAttr != null) {
            sneakAttr.setBaseValue(0.3);
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
            GamePlayer winner = alive.isEmpty() ? null : alive.getFirst();
            if (winner != null) {
                resolveBetsOnRoundWin(winner);
                Player p = winner.getPlayer();
                broadcast("§6" + (p != null ? p.getName() : "???") + " remporte le round !");
            }
            roundEnd();
            Bukkit.getScheduler().runTaskLater(plugin, () -> endCallback.onRoundEnded(winner), 40L);
        }
    }

    private void resolveBetsOnRoundWin(GamePlayer winner) {
        GamePlayer.BetType betType = winner.getPendingBetType();
        if (betType == null) return;

        Player winnerPlayer = winner.getPlayer();
        int payout = switch (betType) {
            case WIN_X4 -> winner.getPendingBetAmount() * 4;
            case DOUBLE_EARNINGS -> winner.getEarningsSinceBet() * 2;
        };

        if (payout > 0) {
            winner.addCurrency(payout, CurrencySource.BET_PAYOUT);
            if (winnerPlayer != null) {
                winnerPlayer.sendMessage("§6Pari remporté : +" + payout + "$ !");
            }
        }
        winner.clearBet();
    }

    private void broadcast(String message) {
        for (GamePlayer gp : players.values()) {
            Player p = gp.getPlayer();
            if (p != null) {
                p.sendMessage(message);
            }
        }
    }

    public List<GamePlayer> getAlivePlayers() {
        return List.copyOf(alivePlayers);
    }
}
