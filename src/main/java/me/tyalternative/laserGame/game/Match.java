package me.tyalternative.laserGame.game;

import me.tyalternative.laserGame.UI.hud.ActionBarManager;
import me.tyalternative.laserGame.UI.shop.HologramHoverListener;
import me.tyalternative.laserGame.UI.shop.impl.LaserGameMenu;
import me.tyalternative.laserGame.archetype.ArchetypeDefinition;
import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.arena.Arena;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.economy.CurrencySource;
import me.tyalternative.laserGame.shop.ShopContext;
import me.tyalternative.laserGame.shop.ShopManager;
import me.tyalternative.laserGame.weapon.WeaponAbilityManager;
import me.tyalternative.laserGame.weapon.WeaponManager;
import me.tyalternative.laserGame.weapon.WeaponType;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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
    private final ShopContext shopContext;
    private final Arena arena;
    private final ActionBarManager actionBarManager;
    private final MatchEndCallback endCallback;

    private final Map<UUID,GamePlayer> players = new LinkedHashMap<>();
    private final Map<UUID, LaserGameMenu> openShopMenus = new LinkedHashMap<>();

    private MatchState state = MatchState.WAITING;
    private BukkitTask scheduleTask;
    private BukkitTask shopTimerTickTask;
    private int countdownRemaining;
    private Round currentRound;

    private String teamName = "LaserGame-hidden-team";
    private Team nameTagHideTeam;

    public interface MatchEndCallback {
        void onMatchEnded(Match match);
    }

    public Match(Plugin plugin, ConfigManager config, WeaponManager weaponManager, WeaponAbilityManager abilityManager,
                 ArchetypeManager archetypeManager, ShopManager shopManager, ShopContext shopContext, Arena arena,
                 MatchEndCallback endCallback) {
        this.plugin = plugin;
        this.config = config;
        this.weaponManager = weaponManager;
        this.abilityManager = abilityManager;
        this.archetypeManager = archetypeManager;
        this.shopManager = shopManager;
        this.shopContext = shopContext;
        this.arena = arena;
        this.endCallback = endCallback;
        this.actionBarManager = new ActionBarManager(this);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.nameTagHideTeam = scoreboard.getTeam(teamName);
        if (this.nameTagHideTeam == null) {
            this.nameTagHideTeam = scoreboard.registerNewTeam(teamName);
            this.nameTagHideTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
    }

    public boolean addPlayer(Player player) {
        if (state != MatchState.WAITING && state != MatchState.STARTING) return false;
        if (players.size() >= arena.getConfig().maxPlayers()) return false;
        if (players.containsKey(player.getUniqueId())) return false;

        WeaponType defaultWeapon = weaponManager.getDefault();
        GamePlayer gp = new GamePlayer(player.getUniqueId(), plugin, config, abilityManager, defaultWeapon, config.getBaseItemSlots());
        gp.setMatch(this);
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
        closeShopMenu(player);

        if (state == MatchState.STARTING && players.size() < effectiveMinPlayers()) cancelCountdown();
        if (state == MatchState.ROUND_IN_PROGRESS && currentRound != null) currentRound.onPlayerRemoved(gp);
        if (state == MatchState.SHOP) maybeEndShopPhaseEarly();
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

    public void setPlayerReady(Player player, boolean ready) {
        if (state != MatchState.SHOP) return;
        GamePlayer gp = players.get(player.getUniqueId());
        if (gp == null) return;

        gp.setReady(ready);

        if (ready) maybeEndShopPhaseEarly();
    }

    private void maybeEndShopPhaseEarly() {
        if (state != MatchState.SHOP) return;
        if (players.isEmpty()) return;

        for (GamePlayer gp : players.values()) {
            Player p = gp.getPlayer();
            if (p != null && !gp.isReady()) return;
        }

        endShopPhaseAndStartRound(); // TODO: Préparation phase
    }

    private int effectiveMinPlayers() {
        return Math.max(arena.getConfig().minPlayers(), config.getMinPlayersFallback());
    }

    private void maybeStartCountdown() {
        if (state != MatchState.WAITING) return;
        if (players.size() < effectiveMinPlayers()) return;

        state = MatchState.STARTING;
        countdownRemaining = config.getCountdownSeconds();
        broadcastToAll("§eLe match commence dans " + countdownRemaining + " secondes...");

        scheduleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            countdownRemaining--;
            if (countdownRemaining <= 0) {
                startFirstRound();
                return;
            }
            if (countdownRemaining <= 5 || countdownRemaining % 10 == 0) {
                broadcastToAll("§e" + countdownRemaining + "...");
            }
        },20L,20L);
    }

    private void cancelCountdown() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
            scheduleTask = null;
        }
        state = MatchState.WAITING;
        broadcastToAll("§cPas assez de joueurs, décompte annulé.");
    }

    private void startFirstRound() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
            scheduleTask = null;
        }
        for (GamePlayer gp : players.values()) {
            gp.resetForNewRound(config.getStartingLives());
            this.nameTagHideTeam.addPlayer(gp.getPlayer());

        }
        actionBarManager.start();
        beginRound();
    }

    private void beginRound() {
        state = MatchState.ROUND_IN_PROGRESS;
        if (currentRound != null) currentRound.stop();
        for (GamePlayer gp : players.values()) {
            gp.setShopSession(null);
            gp.setReady(false);
            Player p = gp.getPlayer();
            if (p != null) closeShopMenu(p);
        }
        openShopMenus.clear();

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
        int totalSeconds = config.getShopPhaseSeconds();
        broadcastToAll("§ePhase shop : prochain round dans " + totalSeconds + "s.");

        List<GamePlayer> matchPlayers = List.copyOf(players.values());

        int playerIndex = 0;
        for (GamePlayer gp : players.values()) {
            gp.setShopSession(shopManager.createSession(gp));
            gp.setReady(false);

            Player p = gp.getPlayer();
            if (p != null) {
                p.getInventory().clear();
                p.setGameMode(GameMode.ADVENTURE);

                Location shopRoom = arena.getConfig().shopRoomFor(playerIndex);
                p.teleport(shopRoom);

                LaserGameMenu menu = new LaserGameMenu(shopRoom.clone().add(0, -0.5, -3.4).setRotation(0,0), gp, shopContext,
                        totalSeconds, matchPlayers, ready -> setPlayerReady(p, ready));
                openShopMenus.put(p.getUniqueId(), menu);
                HologramHoverListener.open(p, menu.getHologram());
            }
            playerIndex++;
        }
        scheduleTask = Bukkit.getScheduler().runTaskLater(plugin, this::endShopPhaseAndStartRound, totalSeconds * 20L);


        int[] remaining = {totalSeconds};
        shopTimerTickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            remaining[0]--;
            for (LaserGameMenu menu : openShopMenus.values()) {
                menu.setRemainingSeconds(Math.max(0, remaining[0]));
            }
            if (remaining[0] <= 0 && shopTimerTickTask != null) {
                shopTimerTickTask.cancel();
                shopTimerTickTask = null;
            }
        }, 20L, 20L);
    }

    private void endShopPhaseAndStartRound() {
        if (state != MatchState.SHOP) return;

        if (scheduleTask != null) {
            scheduleTask.cancel();
            scheduleTask = null;
        }
        if (shopTimerTickTask != null) {
            shopTimerTickTask.cancel();
            shopTimerTickTask = null;
        }

        for (GamePlayer gp : players.values()) {
            gp.resetForNewRound(config.getStartingLives());
        }
        beginRound();
    }

    private void closeShopMenu(Player player) {
        HologramHoverListener.close(player);
        LaserGameMenu menu = openShopMenus.remove(player.getUniqueId());
        if (menu != null) menu.getHologram().clearHologram();
    }

    private void endMatch(GamePlayer winner) {
        state = MatchState.ENDING;
        if (currentRound != null) currentRound.stop();

        Player p = winner != null ? winner.getPlayer() : null;
        broadcastToAll("§1" + (p != null ? p.getName() : "???") + "remporte le match !");

        for (GamePlayer gp : players.values()) {
            gp.getWeapon().stop();
            Player gpPlayer = gp.getPlayer();
            if (gpPlayer != null) {
                closeShopMenu(gpPlayer);
                this.nameTagHideTeam.removeEntity(gpPlayer);
            }
        }
        openShopMenus.clear();
        actionBarManager.stop();

        Bukkit.getScheduler().runTaskLater(plugin, () -> endCallback.onMatchEnded(this), 60L);
    }

    public void broadcastToAll(String message) {
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
    public ActionBarManager getActionBarManager() { return actionBarManager; }
    public boolean isFull() { return players.size() >= arena.getConfig().maxPlayers(); }
    public Optional<GamePlayer> getGamePlayer(Player player) { return Optional.ofNullable(players.get(player.getUniqueId())); }
    public List<GamePlayer> getPlayers() { return List.copyOf(players.values()); }
    public List<GamePlayer> getAlivePlayers() { return getCurrentRound() == null ? List.of() : getCurrentRound().getAlivePlayers(); }
    public Plugin getPlugin() { return plugin; }
}
