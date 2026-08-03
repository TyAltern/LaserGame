package me.tyalternative.laserGame.command;

import me.tyalternative.laserGame.archetype.ArchetypeDefinition;
import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.arena.Arena;
import me.tyalternative.laserGame.arena.ArenaManager;
import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.game.MatchState;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    private final GameManager gameManager;
    private final ArenaManager arenaManager;
    private final WeaponManager weaponManager;
    private final ConsumableManager consumableManager;
    private final SkillManager skillManager;
    private final ArchetypeManager archetypeManager;
    private final PermanentUpgradeManager upgradeManager;
    private final ShopManager shopManager;

    public LaserCommand(GameManager gameManager, ArenaManager arenaManager, WeaponManager weaponManager,
                        ConsumableManager consumableManager, SkillManager skillManager,
                        ArchetypeManager archetypeManager, PermanentUpgradeManager upgradeManager,
                        ShopManager shopManager) {
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
            sender.sendMessage("§eUsage: /laser <join|leave|list|weapon|weapons|skill|skills|archetype|archetypes|upgrades|shop|shopreroll|buy|give|reload> [argument]");
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
            default -> sender.sendMessage("§eUsage: /laser <join|leave|list|weapon|weapons|skill|skills|archetype|archetypes|upgrades|shop|shopreroll|buy|give|reload> [argument]");
        }
        return true;
    }

    private void handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§eUsage: /laser join <arène>");
            return;
        }
        boolean joined = gameManager.joinGame(player, args[1]);
        if (joined) {
            player.sendMessage("§aTu as rejoint l'arène " + args[1] + ".");
        }
    }

    private void handleLeave(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return;
        }
        boolean left = gameManager.leaveGame(player);
        if (left) {
            player.sendMessage("§aTu as quitté la partie.");
        }
    }

    private void handleList(CommandSender sender) {
        List<Arena> arenas = arenaManager.getAllArenas();
        if (arenas.isEmpty()) {
            sender.sendMessage("§cAucune arène disponible.");
            return;
        }
        String names = arenas.stream()
                .map(a -> a.getName() + " (" + a.getConfig().minPlayers() + "-" + a.getConfig().maxPlayers() + ")")
                .collect(Collectors.joining(", "));
        sender.sendMessage("§eArènes disponibles : §f" + names);
    }

    private void handleWeaponChoice(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§eUsage: /laser weapon <id>");
            return;
        }

        Optional<Match> gameOpt = gameManager.getGame(player);
        if (gameOpt.isEmpty()) {
            player.sendMessage("§cTu n'es dans aucune partie. Rejoins d'abord une arène avec /laser join.");
            return;
        }

        gameOpt.get().setWeaponChoice(player, args[1]);
        // les messages de succès/échec sont déjà envoyés par Match#setWeaponChoice
    }

    private void handleWeaponsList(CommandSender sender) {
        List<WeaponType> weapons = weaponManager.getAllWeapons();
        if (weapons.isEmpty()) {
            sender.sendMessage("§cAucune arme disponible.");
            return;
        }
        String names = weapons.stream()
                .map(w -> w.id() + " (" + ChatColor.translateAlternateColorCodes('&', w.displayName()) + "§e)")
                .collect(Collectors.joining("§e, "));
        sender.sendMessage("§eArmes disponibles : §f" + names);
    }

    private void handleSkillChoice(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§eUsage: /laser skill <id>");
            return;
        }

        Optional<Match> matchOpt = gameManager.getGame(player);
        if (matchOpt.isEmpty()) {
            player.sendMessage("§cTu n'es dans aucune partie. Rejoins d'abord une arène avec /laser join.");
            return;
        }
        Match match = matchOpt.get();
        if (match.getState() != MatchState.WAITING && match.getState() != MatchState.STARTING
                && match.getState() != MatchState.SHOP) {
            player.sendMessage("§cTu ne peux pas changer de compétence pendant un round.");
            return;
        }

        Optional<GamePlayer> gpOpt = match.getGamePlayer(player);
        if (gpOpt.isEmpty()) return;

        Optional<SkillDefinition> defOpt = skillManager.getDefinition(args[1]);
        if (defOpt.isEmpty()) {
            player.sendMessage("§cCompétence inconnue : " + args[1] + ". Utilise /laser skills pour la liste.");
            return;
        }

        gpOpt.get().setEquippedSkillId(defOpt.get().id());
        player.sendMessage("§aCompétence équipée : §f" + defOpt.get().displayName());
    }

    private void handleSkillsList(CommandSender sender) {
        List<SkillDefinition> skills = skillManager.getAllDefinitions();
        if (skills.isEmpty()) {
            sender.sendMessage("§cAucune compétence disponible.");
            return;
        }
        String names = skills.stream()
                .map(s -> s.id() + " (" + ChatColor.translateAlternateColorCodes('&', s.displayName()) + "§e)")
                .collect(Collectors.joining("§e, "));
        sender.sendMessage("§eCompétences disponibles : §f" + names);
    }

    private void handleArchetypeChoice(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§eUsage: /laser archetype <id>");
            return;
        }

        Optional<Match> matchOpt = gameManager.getGame(player);
        if (matchOpt.isEmpty()) {
            player.sendMessage("§cTu n'es dans aucune partie. Rejoins d'abord une arène avec /laser join.");
            return;
        }

        matchOpt.get().setArchetypeChoice(player, args[1]);
        // les messages de succès/échec sont déjà envoyés par Match#setArchetypeChoice
    }

    private void handleArchetypesList(CommandSender sender) {
        List<ArchetypeDefinition> archetypes = archetypeManager.getAllDefinitions();
        if (archetypes.isEmpty()) {
            sender.sendMessage("§cAucun archétype disponible.");
            return;
        }
        String names = archetypes.stream()
                .map(a -> a.id() + " (" + ChatColor.translateAlternateColorCodes('&', a.displayName()) + "§e)")
                .collect(Collectors.joining("§e, "));
        sender.sendMessage("§eArchétypes disponibles : §f" + names);
    }

    /**
     * Interface texte minimale pour le shop, en attendant le rendu custom
     * TextDisplay/Interaction. Affiche les 5 offres consommables (avec prix)
     * et les 3 slots spéciaux offerts cette phase, avec le type exclu indiqué.
     */
    private void handleShopView(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return;
        }
        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(player);
        if (gpOpt.isEmpty()) {
            player.sendMessage("§cTu n'es dans aucune partie.");
            return;
        }
        ShopSession session = gpOpt.get().getShopSession();
        if (session == null) {
            player.sendMessage("§cLe shop n'est pas ouvert en ce moment.");
            return;
        }

        player.sendMessage("§e--- Shop (argent : §f" + gpOpt.get().getCurrency() + "§e) ---");

        List<String> consumableIds = session.getConsumableSlotIds();
        for (int i = 0; i < consumableIds.size(); i++) {
            String id = consumableIds.get(i);
            Optional<ConsumableDefinition> defOpt = id != null ? consumableManager.getDefinition(id) : Optional.empty();
            if (defOpt.isPresent()) {
                ConsumableDefinition def = defOpt.get();
                player.sendMessage("§7[" + (i + 1) + "] §f" + ChatColor.translateAlternateColorCodes('&', def.displayName())
                        + " §7(" + def.rarity() + ", §6" + def.price() + "$§7)");
            } else {
                player.sendMessage("§7[" + (i + 1) + "] §8(vide)");
            }
        }
        player.sendMessage("§7Reroll consommables : §6" + shopManager.getConsumableRerollCost(session)
                + "$ §7(§f/laser shopreroll§7)");

        player.sendMessage("§e--- Slots spéciaux (exclu ce shop : " + session.getExcludedSpecialType() + ") ---");
        for (SpecialSlotType type : SpecialSlotType.values()) {
            String id = session.getSpecialSlotIds().get(type);
            if (id == null) continue; // type exclu cette phase, ou aucun item trouvé
            player.sendMessage("§7" + type + " : §f" + id + " §7(§f/laser buy special " + type.name().toLowerCase() + "§7)");
        }
    }

    private void handleShopReroll(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return;
        }
        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(player);
        if (gpOpt.isEmpty()) return;
        GamePlayer gp = gpOpt.get();

        ShopSession session = gp.getShopSession();
        if (session == null) {
            player.sendMessage("§cLe shop n'est pas ouvert en ce moment.");
            return;
        }

        boolean success = shopManager.rerollConsumables(gp, session);
        if (success) {
            player.sendMessage("§aSlots consommables rerollés.");
        } else {
            player.sendMessage("§cPas assez d'argent pour reroll (" + shopManager.getConsumableRerollCost(session) + "$).");
        }
    }

    /**
     * /laser buy consumable <slot 1-5>
     * /laser buy special <weapon|upgrade|skill|archetype>
     */
    private void handleBuy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return;
        }
        if (args.length < 3) {
            player.sendMessage("§eUsage: /laser buy <consumable <slot>|special <type>>");
            return;
        }

        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(player);
        if (gpOpt.isEmpty()) return;
        GamePlayer gp = gpOpt.get();

        ShopSession session = gp.getShopSession();
        if (session == null) {
            player.sendMessage("§cLe shop n'est pas ouvert en ce moment.");
            return;
        }

        String kind = args[1].toLowerCase();
        if (kind.equals("consumable")) {
            int slot;
            try {
                slot = Integer.parseInt(args[2]) - 1; // affiché 1-indexé, stocké 0-indexé
            } catch (NumberFormatException e) {
                player.sendMessage("§eUsage: /laser buy consumable <slot 1-5>");
                return;
            }
            boolean success = shopManager.purchaseConsumableSlot(gp, session, slot);
            player.sendMessage(success ? "§aAchat effectué." : "§cAchat impossible (prix, slot vide, ou inventaire plein).");
        } else if (kind.equals("special")) {
            SpecialSlotType type;
            try {
                type = SpecialSlotType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§eType invalide. Utilise : weapon, upgrade, skill, archetype.");
                return;
            }
            boolean success = shopManager.purchaseSpecialSlot(gp, session, type);
            player.sendMessage(success ? "§aAchat effectué." : "§cAchat impossible (prix, slot exclu ce shop, ou déjà possédé).");
        } else {
            player.sendMessage("§eUsage: /laser buy <consumable <slot>|special <type>>");
        }
    }

    private void handleUpgradesList(CommandSender sender) {
        List<PermanentUpgradeDefinition> upgrades = upgradeManager.getAllDefinitions();
        if (upgrades.isEmpty()) {
            sender.sendMessage("§cAucune amélioration disponible.");
            return;
        }
        String names = upgrades.stream()
                .map(u -> u.id() + " (" + ChatColor.translateAlternateColorCodes('&', u.displayName()) + "§e)")
                .collect(Collectors.joining("§e, "));
        sender.sendMessage("§eAméliorations disponibles : §f" + names);
    }

    /**
     * Commande de test réservée aux admins : donne un consommable, équipe une
     * compétence/archétype, ou débloque une amélioration permanente sans
     * passer par le shop (Phase 11 pas encore implémentée).
     */
    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lasergame.admin")) {
            sender.sendMessage("§cTu n'as pas la permission.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("§eUsage: /laser give <consumable|skill|archetype|upgrade> <id> [joueur]");
            return;
        }

        String type = args[1].toLowerCase();
        String id = args[2];

        Player target;
        if (args.length >= 4) {
            target = Bukkit.getPlayer(args[3]);
            if (target == null) {
                sender.sendMessage("§cJoueur introuvable : " + args[3]);
                return;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage("§cPrécise un joueur cible depuis la console.");
            return;
        }

        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(target);
        if (gpOpt.isEmpty()) {
            sender.sendMessage("§c" + target.getName() + " n'est dans aucune partie.");
            return;
        }
        GamePlayer gp = gpOpt.get();

        switch (type) {
            case "consumable" -> {
                if (consumableManager.getDefinition(id).isEmpty()) {
                    sender.sendMessage("§cConsommable inconnu : " + id);
                    return;
                }
                boolean added = gp.getConsumables().add(id, DuplicatePolicy.SEPARATE_SLOT);
                sender.sendMessage(added
                        ? "§a" + id + " donné à " + target.getName() + "."
                        : "§cAucun slot disponible pour " + target.getName() + ".");
            }
            case "skill" -> {
                if (skillManager.getDefinition(id).isEmpty()) {
                    sender.sendMessage("§cCompétence inconnue : " + id);
                    return;
                }
                gp.setEquippedSkillId(id);
                sender.sendMessage("§a" + id + " équipée par " + target.getName() + ".");
            }
            case "archetype" -> {
                Optional<ArchetypeDefinition> defOpt = archetypeManager.getDefinition(id);
                if (defOpt.isEmpty()) {
                    sender.sendMessage("§cArchétype inconnu : " + id);
                    return;
                }
                ArchetypeEffect effect = archetypeManager.getEffect(defOpt.get().effectId()).orElse(null);
                gp.setArchetype(defOpt.get().id(), effect);
                sender.sendMessage("§a" + id + " équipé par " + target.getName() + ".");
            }
            case "upgrade" -> {
                Optional<PermanentUpgradeDefinition> defOpt = upgradeManager.getDefinition(id);
                if (defOpt.isEmpty()) {
                    sender.sendMessage("§cAmélioration inconnue : " + id);
                    return;
                }
                PermanentUpgradeEffect effect = upgradeManager.getEffect(defOpt.get().effectId()).orElse(null);
                boolean added = gp.addPermanentUpgrade(defOpt.get().id(), effect);
                sender.sendMessage(added
                        ? "§a" + id + " débloquée pour " + target.getName() + "."
                        : "§c" + target.getName() + " possède déjà cette amélioration.");
            }
            default -> sender.sendMessage("§eUsage: /laser give <consumable|skill|archetype|upgrade> <id> [joueur]");
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("lasergame.admin")) {
            sender.sendMessage("§cTu n'as pas la permission.");
            return;
        }
        arenaManager.loadAll();
        weaponManager.loadAll();
        consumableManager.loadAll();
        skillManager.loadAll();
        archetypeManager.loadAll();
        upgradeManager.loadAll();
        sender.sendMessage("§aArènes, armes, consommables, compétences, archétypes et améliorations rechargés.");
    }
}
