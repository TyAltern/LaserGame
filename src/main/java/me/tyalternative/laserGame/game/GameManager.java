package me.tyalternative.laserGame.game;

import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.arena.Arena;
import me.tyalternative.laserGame.arena.ArenaManager;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.shop.ShopManager;
import me.tyalternative.laserGame.weapon.WeaponAbilityManager;
import me.tyalternative.laserGame.weapon.WeaponAttributeKeys;
import me.tyalternative.laserGame.weapon.WeaponManager;
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

    private final Map<UUID, Match> matchesByArenaId = new HashMap<>();
    private final Map<UUID, Match> matchByPlayer = new HashMap<>();

    public GameManager(Plugin plugin, ConfigManager config, ArenaManager arenaManager, WeaponManager weaponManager,
                       WeaponAbilityManager abilityManager, ArchetypeManager archetypeManager, ShopManager shopManager) {
        this.plugin = plugin;
        this.config = config;
        this.arenaManager = arenaManager;
        this.weaponManager = weaponManager;
        this.abilityManager = abilityManager;
        this.archetypeManager = archetypeManager;
        this.shopManager = shopManager;
    }

    public boolean joinGame(Player player, String arenaName) {
        if (matchByPlayer.containsKey(player.getUniqueId())) {
            player.sendMessage("§cTu es déjà dans une partie. Utilise /laser leave d'abord.");
            return false;
        }

        Optional<Arena> arenaOpt = arenaManager.getArena(arenaName);
        if (arenaOpt.isEmpty()) {
            player.sendMessage("§cArène inconnue : " + arenaName);
            return false;
        }
        Arena arena = arenaOpt.get();

        Match match = matchesByArenaId.get(arena.getId());
        if (match == null || match.getState() == MatchState.ENDING) {
            match = new Match(plugin, config, weaponManager, abilityManager, archetypeManager, shopManager, arena, this::onGameEnded);
            matchesByArenaId.put(arena.getId(), match);
        }

        if (match.getState() != MatchState.WAITING && match.getState() != MatchState.STARTING) {
            player.sendMessage("§cCe match a déjà commencé.");
            return false;
        }

        if (match.isFull()) {
            player.sendMessage("§cCette arène est pleine.");
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
            player.sendMessage("§cTu n'es dans aucune partie.");
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
