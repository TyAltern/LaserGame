package me.tyalternative.laserGame.command;

import me.tyalternative.laserGame.UI.shop.*;
import me.tyalternative.laserGame.UI.shop.impl.LaserGameMenu;
import me.tyalternative.laserGame.archetype.ArchetypeDefinition;
import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.arena.Arena;
import me.tyalternative.laserGame.arena.ArenaManager;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.game.MatchState;
import me.tyalternative.laserGame.lang.Lang;
import me.tyalternative.laserGame.shop.ConsumableDefinition;
import me.tyalternative.laserGame.shop.ConsumableManager;
import me.tyalternative.laserGame.shop.DuplicatePolicy;
import me.tyalternative.laserGame.shop.ShopManager;
import me.tyalternative.laserGame.shop.ShopSession;
import me.tyalternative.laserGame.shop.SpecialSlotType;
import me.tyalternative.laserGame.skill.SkillDefinition;
import me.tyalternative.laserGame.skill.SkillManager;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeDefinition;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeManager;
import me.tyalternative.laserGame.weapon.WeaponManager;
import me.tyalternative.laserGame.weapon.WeaponType;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * /laser join <arène>
 * /laser leave
 * /laser list
 * /laser weapon <id>
 * /laser weapons
 * /laser skill <id>
 * /laser skills
 * /laser archetype <id>
 * /laser archetypes
 * /laser upgrades
 * /laser shop                                                    (voir les offres de la phase shop en cours)
 * /laser shopreroll                                               (reroll payant des 5 slots consommables)
 * /laser buy consumable <slot 1-5>
 * /laser buy special <weapon|upgrade|skill|archetype>
 * /laser give <consumable|skill|archetype|upgrade> <id> [joueur]   (permission admin, testing)
 * /laser reload   (permission admin)
 */
public class LaserCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final GameManager gameManager;
    private final ArenaManager arenaManager;
    private final WeaponManager weaponManager;
    private final ConsumableManager consumableManager;
    private final SkillManager skillManager;
    private final ArchetypeManager archetypeManager;
    private final PermanentUpgradeManager upgradeManager;
    private final ShopManager shopManager;

    public LaserCommand(JavaPlugin plugin, ConfigManager configManager, GameManager gameManager, ArenaManager arenaManager, WeaponManager weaponManager,
                        ConsumableManager consumableManager, SkillManager skillManager,
                        ArchetypeManager archetypeManager, PermanentUpgradeManager upgradeManager,
                        ShopManager shopManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gameManager = gameManager;
        this.arenaManager = arenaManager;
        this.weaponManager = weaponManager;
        this.consumableManager = consumableManager;
        this.skillManager = skillManager;
        this.archetypeManager = archetypeManager;
        this.upgradeManager = upgradeManager;
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Lang.send(sender, "command.usage.main");
            return true;
        }


        String sub = args[0].toLowerCase();
        switch (sub) {
            case "join" -> handleJoin(sender, args);
            case "leave" -> handleLeave(sender);
            case "list" -> handleList(sender);
            case "weapon" -> handleWeaponChoice(sender, args);
            case "weapons" -> handleWeaponsList(sender);
            case "skill" -> handleSkillChoice(sender, args);
            case "skills" -> handleSkillsList(sender);
            case "archetype" -> handleArchetypeChoice(sender, args);
            case "archetypes" -> handleArchetypesList(sender);
            case "upgrades" -> handleUpgradesList(sender);
            case "shop" -> handleShopView(sender);
            case "shopreroll" -> handleShopReroll(sender);
            case "buy" -> handleBuy(sender, args);
            case "give" -> handleGive(sender, args);
            case "reload" -> handleReload(sender);
            default -> Lang.send(sender, "command.usage.main");
        }
        return true;
    }

    private void handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Lang.send(sender, "general.player-only");
            return;
        }
        if (args.length < 2) {
            Lang.send(player, "command.usage.join");
            return;
        }
        boolean joined = gameManager.joinGame(player, args[1]);
        if (joined) {
            Lang.send(player, "command.join-success", "arena", args[1]);
        }
    }

    private void handleLeave(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Lang.send(sender, "general.player-only");
            return;
        }
        boolean left = gameManager.leaveGame(player);
        if (left) {
            Lang.send(player, "command.leave-success");
        }
    }

    private void handleList(CommandSender sender) {
        List<Arena> arenas = arenaManager.getAllArenas();
        if (arenas.isEmpty()) {
            Lang.send(sender, "command.arenas-none");
            return;
        }
        String names = arenas.stream()
                .map(a -> Lang.get("command.arena-entry", "name", a.getName(),
                        "min", a.getConfig().minPlayers(), "max", a.getConfig().maxPlayers()))
                .collect(Collectors.joining(Lang.get("command.arena-separator")));
        Lang.send(sender, "command.arenas-list", "list", names);
    }

    private void handleWeaponChoice(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Lang.send(sender, "general.player-only");
            return;
        }
        if (args.length < 2) {
            Lang.send(player, "command.usage.weapon");
            return;
        }

        Optional<Match> gameOpt = gameManager.getGame(player);
        if (gameOpt.isEmpty()) {
            Lang.send(player, "general.not-in-game-join-hint");
            return;
        }

        gameOpt.get().setWeaponChoice(player, args[1]);
        // les messages de succès/échec sont déjà envoyés par Match#setWeaponChoice
    }

    private void handleWeaponsList(CommandSender sender) {
        List<WeaponType> weapons = weaponManager.getAllWeapons();
        if (weapons.isEmpty()) {
            Lang.send(sender, "command.weapons-none");
            return;
        }
        String names = weapons.stream()
                .map(w -> Lang.get("command.list-entry", "id", w.id(), "name", Lang.color(w.displayName())))
                .collect(Collectors.joining(Lang.get("command.list-separator")));
        Lang.send(sender, "command.weapons-list", "list", names);
    }

    private void handleSkillChoice(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Lang.send(sender, "general.player-only");
            return;
        }
        if (args.length < 2) {
            Lang.send(player, "command.usage.skill");
            return;
        }

        Optional<Match> matchOpt = gameManager.getGame(player);
        if (matchOpt.isEmpty()) {
            Lang.send(player, "general.not-in-game-join-hint");
            return;
        }
        Match match = matchOpt.get();
        if (match.getState() != MatchState.WAITING && match.getState() != MatchState.STARTING
                && match.getState() != MatchState.SHOP) {
            Lang.send(player, "command.skill-change-forbidden");
            return;
        }

        Optional<GamePlayer> gpOpt = match.getGamePlayer(player);
        if (gpOpt.isEmpty()) return;

        Optional<SkillDefinition> defOpt = skillManager.getDefinition(args[1]);
        if (defOpt.isEmpty()) {
            Lang.send(player, "command.skill-unknown", "id", args[1]);
            return;
        }

        gpOpt.get().setEquippedSkillId(defOpt.get().id());
        Lang.send(player, "command.skill-equipped", "name", Lang.color(defOpt.get().displayName()));
    }

    private void handleSkillsList(CommandSender sender) {
        List<SkillDefinition> skills = skillManager.getAllDefinitions();
        if (skills.isEmpty()) {
            Lang.send(sender, "command.skills-none");
            return;
        }
        String names = skills.stream()
                .map(s -> Lang.get("command.list-entry", "id", s.id(), "name", Lang.color(s.displayName())))
                .collect(Collectors.joining(Lang.get("command.list-separator")));
        Lang.send(sender, "command.skills-list", "list", names);
    }

    private void handleArchetypeChoice(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Lang.send(sender, "general.player-only");
            return;
        }
        if (args.length < 2) {
            Lang.send(player, "command.usage.archetype");
            return;
        }

        Optional<Match> matchOpt = gameManager.getGame(player);
        if (matchOpt.isEmpty()) {
            Lang.send(player, "general.not-in-game-join-hint");
            return;
        }

        matchOpt.get().setArchetypeChoice(player, args[1]);
        // les messages de succès/échec sont déjà envoyés par Match#setArchetypeChoice
    }

    private void handleArchetypesList(CommandSender sender) {
        List<ArchetypeDefinition> archetypes = archetypeManager.getAllDefinitions();
        if (archetypes.isEmpty()) {
            Lang.send(sender, "command.archetypes-none");
            return;
        }
        String names = archetypes.stream()
                .map(a -> Lang.get("command.list-entry", "id", a.id(), "name", Lang.color(a.displayName())))
                .collect(Collectors.joining(Lang.get("command.list-separator")));
        Lang.send(sender, "command.archetypes-list", "list", names);
    }

    /**
     * Interface texte minimale pour le shop, en attendant le rendu custom
     * TextDisplay/Interaction. Affiche les 5 offres consommables (avec prix)
     * et les 3 slots spéciaux offerts cette phase, avec le type exclu indiqué.
     */
    private void handleShopView(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Lang.send(sender, "general.player-only");
            return;
        }
        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(player);
        if (gpOpt.isEmpty()) {
            Lang.send(player, "general.not-in-game");
            return;
        }
        ShopSession session = gpOpt.get().getShopSession();
        if (session == null) {
            Lang.send(player, "general.shop-closed");
            return;
        }

        Lang.send(player, "command.shop.header", "money", gpOpt.get().getCurrency());

        List<String> consumableIds = session.getConsumableSlotIds();
        for (int i = 0; i < consumableIds.size(); i++) {
            String id = consumableIds.get(i);
            Optional<ConsumableDefinition> defOpt = id != null ? consumableManager.getDefinition(id) : Optional.empty();
            if (defOpt.isPresent()) {
                ConsumableDefinition def = defOpt.get();
                Lang.send(player, "command.shop.consumable-line", "slot", i + 1,
                        "name", Lang.color(def.displayName()), "rarity", def.rarity(),
                        "price", shopManager.getConsumablePrice(gpOpt.get(), session, i, def));
            } else {
                Lang.send(player, "command.shop.consumable-empty", "slot", i + 1);
            }
        }
        Lang.send(player, "command.shop.reroll-line", "cost", shopManager.getConsumableRerollCost(session));

        Lang.send(player, "command.shop.special-header", "excluded", session.getExcludedSpecialType());
        for (SpecialSlotType type : SpecialSlotType.values()) {
            String id = session.getSpecialSlotIds().get(type);
            if (id == null) continue; // type exclu cette phase, ou aucun item trouvé
            Lang.send(player, "command.shop.special-line", "type", type, "id", id, "command", type.name().toLowerCase());
        }
    }

    private void handleShopReroll(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Lang.send(sender, "general.player-only");
            return;
        }
        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(player);
        if (gpOpt.isEmpty()) return;
        GamePlayer gp = gpOpt.get();

        ShopSession session = gp.getShopSession();
        if (session == null) {
            Lang.send(player, "general.shop-closed");
            return;
        }

        boolean success = shopManager.rerollConsumables(gp, session);
        if (success) {
            Lang.send(player, "command.shop.reroll-success");
        } else {
            Lang.send(player, "command.shop.reroll-fail", "cost", shopManager.getConsumableRerollCost(session));
        }
    }

    /**
     * /laser buy consumable <slot 1-5>
     * /laser buy special <weapon|upgrade|skill|archetype>
     */
    private void handleBuy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Lang.send(sender, "general.player-only");
            return;
        }
        if (args.length < 3) {
            Lang.send(player, "command.usage.buy");
            return;
        }

        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(player);
        if (gpOpt.isEmpty()) return;
        GamePlayer gp = gpOpt.get();

        ShopSession session = gp.getShopSession();
        if (session == null) {
            Lang.send(player, "general.shop-closed");
            return;
        }

        String kind = args[1].toLowerCase();
        if (kind.equals("consumable")) {
            int slot;
            try {
                slot = Integer.parseInt(args[2]) - 1; // affiché 1-indexé, stocké 0-indexé
            } catch (NumberFormatException e) {
                Lang.send(player, "command.usage.buy-consumable");
                return;
            }
            boolean success = shopManager.purchaseConsumableSlot(gp, session, slot);
            Lang.send(player, success ? "general.purchase-success" : "command.shop.buy-consumable-fail");
        } else if (kind.equals("special")) {
            SpecialSlotType type;
            try {
                type = SpecialSlotType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                Lang.send(player, "command.shop.invalid-type");
                return;
            }
            boolean success = shopManager.purchaseSpecialSlot(gp, session, type);
            Lang.send(player, success ? "general.purchase-success" : "command.shop.buy-special-fail");
        } else {
            Lang.send(player, "command.usage.buy");
        }
    }

    private void handleUpgradesList(CommandSender sender) {
        List<PermanentUpgradeDefinition> upgrades = upgradeManager.getAllDefinitions();
        if (upgrades.isEmpty()) {
            Lang.send(sender, "command.upgrades-none");
            return;
        }
        String names = upgrades.stream()
                .map(u -> Lang.get("command.list-entry", "id", u.id(), "name", Lang.color(u.displayName())))
                .collect(Collectors.joining(Lang.get("command.list-separator")));
        Lang.send(sender, "command.upgrades-list", "list", names);
    }

    /**
     * Commande de test réservée aux admins : donne un consommable, équipe une
     * compétence/archétype, ou débloque une amélioration permanente sans
     * passer par le shop (Phase 11 pas encore implémentée).
     */
    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lasergame.admin")) {
            Lang.send(sender, "general.no-permission");
            return;
        }
        if (args.length < 3) {
            Lang.send(sender, "command.usage.give");
            return;
        }

        String type = args[1].toLowerCase();
        String id = args[2];

        Player target;
        if (args.length >= 4) {
            target = Bukkit.getPlayer(args[3]);
            if (target == null) {
                Lang.send(sender, "command.give.player-not-found", "player", args[3]);
                return;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            Lang.send(sender, "command.give.console-needs-target");
            return;
        }

        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(target);
        if (gpOpt.isEmpty()) {
            Lang.send(sender, "command.give.target-not-in-game", "player", target.getName());
            return;
        }
        GamePlayer gp = gpOpt.get();

        switch (type) {
            case "consumable" -> {
                if (consumableManager.getDefinition(id).isEmpty()) {
                    Lang.send(sender, "command.give.consumable-unknown", "id", id);
                    return;
                }
                boolean added = gp.getConsumables().add(id, DuplicatePolicy.SEPARATE_SLOT);
                Lang.send(sender, added ? "command.give.consumable-given" : "command.give.consumable-no-slot",
                        "id", id, "player", target.getName());
            }
            case "skill" -> {
                if (skillManager.getDefinition(id).isEmpty()) {
                    Lang.send(sender, "command.give.skill-unknown", "id", id);
                    return;
                }
                gp.setEquippedSkillId(id);
                Lang.send(sender, "command.give.skill-equipped", "id", id, "player", target.getName());
            }
            case "archetype" -> {
                Optional<ArchetypeDefinition> defOpt = archetypeManager.getDefinition(id);
                if (defOpt.isEmpty()) {
                    Lang.send(sender, "command.give.archetype-unknown", "id", id);
                    return;
                }
                ArchetypeEffect effect = archetypeManager.getEffect(defOpt.get()).orElse(null);
                gp.setArchetype(defOpt.get().id(), effect);
                Lang.send(sender, "command.give.archetype-equipped", "id", id, "player", target.getName());
            }
            case "upgrade" -> {
                Optional<PermanentUpgradeDefinition> defOpt = upgradeManager.getDefinition(id);
                if (defOpt.isEmpty()) {
                    Lang.send(sender, "command.give.upgrade-unknown", "id", id);
                    return;
                }
                PermanentUpgradeEffect effect = upgradeManager.getEffect(defOpt.get()).orElse(null);
                boolean added = gp.addPermanentUpgrade(defOpt.get().id(), effect);
                Lang.send(sender, added ? "command.give.upgrade-unlocked" : "command.give.upgrade-already-owned",
                        "id", id, "player", target.getName());
            }
            default -> Lang.send(sender, "command.usage.give");
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("lasergame.admin")) {
            Lang.send(sender, "general.no-permission");
            return;
        }
        configManager.load();
        arenaManager.loadAll();
        weaponManager.loadAll();
        consumableManager.loadAll();
        skillManager.loadAll();
        archetypeManager.loadAll();
        upgradeManager.loadAll();
        Lang.load(plugin, plugin.getConfig().getString("language", Lang.DEFAULT_LANGUAGE));
        Lang.send(sender, "command.reload-success");
    }
}
