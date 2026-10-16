package me.tyalternative.laserGame.game;

import me.tyalternative.laserGame.UI.hud.ActionBarManager;
import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.arena.Arena;
import me.tyalternative.laserGame.arena.ArenaManager;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.shop.ConsumableManager;
import me.tyalternative.laserGame.shop.ShopContext;
import me.tyalternative.laserGame.shop.ShopManager;
import me.tyalternative.laserGame.skill.SkillManager;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeManager;
import me.tyalternative.laserGame.weapon.WeaponAbilityManager;
import me.tyalternative.laserGame.weapon.WeaponAttributeKeys;
import me.tyalternative.laserGame.weapon.WeaponManager;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GameManager {

    private final Plugin plugin;
    private final ConfigManager config;
    private final ArenaManager arenaManager;
    private final WeaponManager weaponManager;
    private final WeaponAbilityManager abilityManager;
    private final ArchetypeManager archetypeManager;
    private final ShopManager shopManager;
    private final ShopContext shopContext;

    private final Map<UUID, Match> matchesByArenaId = new HashMap<>();
    private final Map<UUID, Match> matchByPlayer = new HashMap<>();

    public GameManager(Plugin plugin, ConfigManager config, ArenaManager arenaManager, WeaponManager weaponManager,
                       WeaponAbilityManager abilityManager, ArchetypeManager archetypeManager, ShopManager shopManager,
                       ConsumableManager consumableManager, SkillManager skillManager, PermanentUpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.config = config;
        this.arenaManager = arenaManager;
        this.weaponManager = weaponManager;
        this.abilityManager = abilityManager;
        this.archetypeManager = archetypeManager;
        this.shopManager = shopManager;
        this.shopContext = new ShopContext(shopManager, consumableManager, weaponManager, archetypeManager, skillManager, upgradeManager);
    }


    public boolean joinGame(Player player, String arenaName) {
        if (matchByPlayer.containsKey(player.getUniqueId())) {
            Lang.send(player, "game.already-in-game");
            return false;
        }

        Optional<Arena> arenaOpt = arenaManager.getArena(arenaName);
        if (arenaOpt.isEmpty()) {
            Lang.send(player, "game.unknown-arena", "arena", arenaName);
            return false;
        }
        Arena arena = arenaOpt.get();

        Match match = matchesByArenaId.get(arena.getId());
        if (match == null || match.getState() == MatchState.ENDING) {
            match = new Match(plugin, config, weaponManager, abilityManager, archetypeManager, shopManager, shopContext, arena, this::onGameEnded);
            matchesByArenaId.put(arena.getId(), match);
        }

        if (match.getState() != MatchState.WAITING && match.getState() != MatchState.STARTING) {
            Lang.send(player, "game.already-started");
            return false;
        }

        if (match.isFull()) {
            Lang.send(player, "game.arena-full");
            return false;
        }

        boolean joined = match.addPlayer(player);
        if (joined) {
            matchByPlayer.put(player.getUniqueId(), match);
        }
        return joined;
    }

    public boolean leaveGame(Player player) {
        Match match = matchByPlayer.remove(player.getUniqueId());
        if (match == null) {
            Lang.send(player, "general.not-in-game");
            return false;
        }
        match.removePlayer(player);
        sendToLobby(player);
        return true;
    }

    public void onGameEnded(Match match) {
        for (GamePlayer gp : match.getPlayers()) {
            Player p = gp.getPlayer();
            if (p != null) {
                matchByPlayer.remove(p.getUniqueId());
                sendToLobby(p);
            }
        }
        matchesByArenaId.remove(match.getArena().getId());
    }

    private void sendToLobby(Player player) {
        player.getInventory().clear();

        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(20.0);
        }
        player.setHealth(20.0);

        AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(WeaponAttributeKeys.speedModifier(plugin));
        }

        player.setGameMode(GameMode.SURVIVAL);

        Location lobby = config.getLobbyLocation();
        if (lobby != null) {
            player.teleport(lobby);
        }
    }

    public Optional<Match> getGame(Player player) {
        return Optional.ofNullable(matchByPlayer.get(player.getUniqueId()));
    }

    public Optional<GamePlayer> getGamePlayer(Player player) {
        return getGame(player).flatMap(m -> m.getGamePlayer(player));
    }

    public void handleDisconnect(Player player) {
        Match match = matchByPlayer.get(player.getUniqueId());
        if (match != null) {
            match.removePlayer(player);
            matchByPlayer.remove(player.getUniqueId());
        }
    }

    public List<Match> getActiveGames() {
        return List.copyOf(matchesByArenaId.values());
    }
}
