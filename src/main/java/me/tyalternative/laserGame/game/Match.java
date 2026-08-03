package me.tyalternative.laserGame.game;

import me.tyalternative.laserGame.archetype.ArchetypeDefinition;
import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.arena.Arena;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.economy.CurrencySource;
import me.tyalternative.laserGame.shop.ShopManager;
import me.tyalternative.laserGame.weapon.WeaponAbilityManager;
import me.tyalternative.laserGame.weapon.WeaponManager;
import me.tyalternative.laserGame.weapon.WeaponType;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Match {

    private final Plugin plugin;
    private final ConfigManager config;
    private final WeaponManager weaponManager;
    private final WeaponAbilityManager abilityManager;
    private final ArchetypeManager archetypeManager;
    private final ShopManager shopManager;
    private final Arena arena;
    private final MatchEndCallback endCallback;

    private final Map<UUID,GamePlayer> players = new LinkedHashMap<>();

    private MatchState state = MatchState.WAITING;
    private BukkitTask scheduleTask;
    private int countdownRemaining;
    private Round currentRound;

    public interface MatchEndCallback {
        void onMatchEnded(Match match);
    }

    public Match(Plugin plugin, ConfigManager config, WeaponManager weaponManager, WeaponAbilityManager abilityManager,
                 ArchetypeManager archetypeManager, ShopManager shopManager, Arena arena, MatchEndCallback endCallback) {
        this.plugin = plugin;
        this.config = config;
        this.weaponManager = weaponManager;
        this.abilityManager = abilityManager;
        this.archetypeManager = archetypeManager;
        this.shopManager = shopManager;
        this.arena = arena;
        this.endCallback = endCallback;
    }

    public boolean addPlayer(Player player) {
        if (state != MatchState.WAITING && state != MatchState.STARTING) return false;
        if (players.size() >= arena.getConfig().maxPlayers()) return false;
        if (players.containsKey(player.getUniqueId())) return false;

        WeaponType defaultWeapon = weaponManager.getDefault();
        GamePlayer gp = new GamePlayer(player.getUniqueId(), plugin, config, abilityManager, defaultWeapon, config.getBaseItemSlots());
        gp.resetForNewRound(config.getStartingLives());
        players.put(player.getUniqueId(), gp);

        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(arena.getConfig().waitingRoom());
        player.sendMessage("§7Arme actuelle : §f" + defaultWeapon.displayName()
                + "§7. Utilise §f/laser weapon <id>§7 pour en changer, §f/laser weapons§7 pour la liste.");

        maybeStartCountdown();
        return true;
    }

    public void removePlayer(Player player) {
        GamePlayer gp = players.remove(player.getUniqueId());
        if (gp == null) return;

        gp.getWeapon().stop();

        if (state == MatchState.STARTING && players.size() < effectiveMinPlayers()) cancelCountdown();
        if (state == MatchState.ROUND_IN_PROGRESS && currentRound != null) currentRound.onPlayerRemoved(gp);
    }

    public boolean setWeaponChoice(Player player, String weaponId) { // TODO : ADD SHOP COMPATIBILITY (CURRENTLY CHOSEN BY COMMAND AND NOT BY SHOP)
        if (state != MatchState.WAITING && state != MatchState.STARTING && state != MatchState.SHOP) {
            player.sendMessage("§cTu ne peux pas changer d'arme pendant un round.");
            return false;
        }
        GamePlayer gp = players.get(player.getUniqueId());
        if (gp == null) return false;

        Optional<WeaponType> typeOpt = weaponManager.getWeapon(weaponId);
        if (typeOpt.isEmpty()) {
            player.sendMessage("§cArme inconnue : " + weaponId + ". Utilise /laser weapons pour la liste.");
            return false;
        }

        gp.setWeapon(typeOpt.get());
        player.sendMessage("§aArme sélectionnée : §f" + typeOpt.get().displayName());
        return true;
    }

    public boolean setArchetypeChoice(Player player, String archetypeId) {
        if (state != MatchState.WAITING && state != MatchState.STARTING && state != MatchState.SHOP) {
            player.sendMessage("§cTu ne peux pas changer d'archétype pendant un round.");
            return false;
        }
        GamePlayer gp = players.get(player.getUniqueId());
        if(gp == null) return false;

        Optional<ArchetypeDefinition> defOpt = archetypeManager.getDefinition(archetypeId);
        if (defOpt.isEmpty()) {
            player.sendMessage("§cArchétype inconnu : " + archetypeId + ". Utilise /laser archetypes pour la liste.");
            return false;
        }

        ArchetypeEffect effect = archetypeManager.getEffect(defOpt.get().effectId()).orElse(null);
        gp.setArchetype(defOpt.get().id(), effect);
        player.sendMessage("§aArchétype équipé : §f" + defOpt.get().displayName());
        return true;
    }

    private int effectiveMinPlayers() {
        return Math.max(arena.getConfig().minPlayers(), config.getMinPlayersFallback());
    }

    private void maybeStartCountdown() {
        if (state != MatchState.WAITING) return;
        if (players.size() < effectiveMinPlayers()) return;

        state = MatchState.STARTING;
        countdownRemaining = config.getCountdownSeconds();
        broadcast("§eLe match commence dans " + countdownRemaining + " secondes...");

        scheduleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            countdownRemaining--;
            if (countdownRemaining <= 0) {
                startFirstRound();
                return;
            }
            if (countdownRemaining <= 5 || countdownRemaining % 10 == 0) {
                broadcast("§e" + countdownRemaining + "...");
            }
        },20L,20L);
    }

    private void cancelCountdown() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
            scheduleTask = null;
        }
        state = MatchState.WAITING;
        broadcast("§cPas assez de joueurs, décompte annulé.");
    }

    private void startFirstRound() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
            scheduleTask = null;
        }
        for (GamePlayer gp : players.values()) {
            gp.resetForNewRound(config.getStartingLives());
        }
        beginRound();
    }

    private void beginRound() {
        state = MatchState.ROUND_IN_PROGRESS;
        for (GamePlayer gp : players.values()) {
            gp.setShopSession(null);
        }
        currentRound = new Round(plugin, config, arena, players, this::onRoundEnded);
        currentRound.start();
    }

    private void onRoundEnded(GamePlayer winner) {
        if (winner != null) {
            winner.incrementRoundWins();
            winner.addCurrency(config.getRoundWinReward(), CurrencySource.ROUND_WIN);
        }

        if (winner != null && winner.getRoundWins() >= config.getRoundsToWin()) {
            endMatch(winner);
        } else {
            startShopPhase();
        }
    }

    private void startShopPhase() {
        state = MatchState.SHOP;
        broadcast("§ePhase shop : prochain round dans " + config.getShopPhaseSeconds() + "s.");

        for (GamePlayer gp : players.values()) {
            gp.setShopSession(shopManager.createSession(gp));

            Player p = gp.getPlayer();
            if (p != null) {
                p.setGameMode(GameMode.ADVENTURE);
                p.teleport(arena.getConfig().waitingRoom()); // TODO : FIXE TELEPORT LOCATION
                p.sendMessage("§eLe shop est ouvert (§f/laser shop§e pour voir les offres)."); // TODO : à REMOVE c'est du debug
            }
        }

        // TODO : OPEN SHOP GUI TO ALL PLAYERS

        scheduleTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (GamePlayer gp : players.values()) {
                gp.resetForNewRound(config.getStartingLives());
            }
            beginRound();
        }, config.getShopPhaseSeconds() * 20L);
    }

    private void endMatch(GamePlayer winner) {
        state = MatchState.ENDING;

        Player p = winner != null ? winner.getPlayer() : null;
        broadcast("§1" + (p != null ? p.getName() : "???") + "remporte le match !");

        for (GamePlayer gp : players.values()) {
            gp.getWeapon().stop();
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> endCallback.onMatchEnded(this), 60L);
    }

    private void broadcast(String message) {
        for (GamePlayer gp : players.values()) {
            Player p = gp.getPlayer();
            if (p != null) {
                p.sendMessage(message);
            }
        }
    }

    public Arena getArena() { return arena; }
    public MatchState getState() { return state; }
    public Round getCurrentRound() { return currentRound; }
    public boolean isFull() { return players.size() >= arena.getConfig().maxPlayers(); }
    public Optional<GamePlayer> getGamePlayer(Player player) { return Optional.ofNullable(players.get(player.getUniqueId())); }
    public List<GamePlayer> getPlayers() { return List.copyOf(players.values()); }
}
