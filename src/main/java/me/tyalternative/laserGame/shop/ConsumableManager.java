package me.tyalternative.laserGame.shop;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.consumable.*;
import me.tyalternative.laserGame.effect.impl.effect.CurseNextTargetEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConsumableManager {

    private final Plugin plugin;
    private final Map<String, ConsumableDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, ConsumableEffect> effectRegistry = new HashMap<>();

    public ConsumableManager(Plugin plugin) {
        this.plugin = plugin;
        registerEffects();
    }

    private void registerEffects() {

        // Défense
        effectRegistry.put("damage_cap_next_hit", new DamageCapConsumableEffect());
        effectRegistry.put("reflect_next_hit", new ReflectDamageConsumableEffect());
        effectRegistry.put("long_range_immune", new LongRangeImmuneConsumableEffect());

        // Offensif
        effectRegistry.put("bonus_damage_next_shot", new BonusDamageConsumableEffect());
        effectRegistry.put("aoe_next_shot", new AoeDamageConsumableEffect());
        effectRegistry.put("execute_next_shot", new ExecuteConsumableEffect());
        effectRegistry.put("free_shot", new FreeShotConsumableEffect());

        // Malédictions (prochain joueur touché)
        effectRegistry.put("curse_troll_weapon", new CurseConsumableEffect(CurseNextTargetEffect.Kind.TROLL_WEAPON,
                "§7Le prochain joueur touché aura une arme dégradée, jusqu'à son prochain tir ou coup reçu."));
        effectRegistry.put("curse_double_cooldown", new CurseConsumableEffect(CurseNextTargetEffect.Kind.DOUBLE_COOLDOWN,
                "§7Le prochain joueur touché aura son cooldown de tir doublé, jusqu'à son prochain tir ou coup reçu."));
        effectRegistry.put("curse_ammo_loss", new CurseConsumableEffect(CurseNextTargetEffect.Kind.AMMO_LOSS,
                "§7Le prochain joueur touché perdra 40% de ses munitions max pour le round."));

        // Armes et munitions
        effectRegistry.put("instant_reload", new InstantReloadConsumableEffect());
        effectRegistry.put("reload_overflow", new ReloadOverflowConsumableEffect());
        effectRegistry.put("round_max_ammo_boost", new RoundWeaponBoostConsumableEffect(
                RoundWeaponBoostConsumableEffect.Kind.MAX_AMMO, 0.20, "§7+20% de munitions max pour le round."));
        effectRegistry.put("round_range_boost", new RoundWeaponBoostConsumableEffect(
                RoundWeaponBoostConsumableEffect.Kind.RANGE, 0.30, "§7+30% de portée pour le round."));
        effectRegistry.put("round_cooldown_reduction", new RoundWeaponBoostConsumableEffect(
                RoundWeaponBoostConsumableEffect.Kind.COOLDOWN, -0.20, "§7-20% de cooldown entre deux tirs pour le round."));
        effectRegistry.put("timed_passive_regen", new TimedPassiveRegenConsumableEffect());

        // Affichage
        effectRegistry.put("glow_nearby", new GlowNearbyConsumableEffect());
        effectRegistry.put("glow_crouching", new GlowCrouchingConsumableEffect());

        // Mobilité
        effectRegistry.put("speed_boost", new SpeedBoostConsumableEffect());
        effectRegistry.put("sneak_speed_unlock", new SneakSpeedUnlockConsumableEffect());
        effectRegistry.put("jump_boost_round", new JumpBoostRoundConsumableEffect());

        // CC
        effectRegistry.put("freeze_cc", new AreaPotionConsumableEffect(
                PotionEffectType.SLOWNESS, 250, 20.0, 100, "§7{count} joueur(s) figé(s) pendant 5 secondes."));
        effectRegistry.put("nausea_cc", new AreaPotionConsumableEffect(
                PotionEffectType.NAUSEA, 0, 25.0, 200, "§7{count} joueur(s) pris de nausée pendant 10 secondes."));
        effectRegistry.put("slow_cc", new AreaPotionConsumableEffect(
                PotionEffectType.SLOWNESS, 1, 30.0, 100, "§7{count} joueur(s) ralenti(s) pendant 5 secondes."));
        effectRegistry.put("smoke_bomb", new SmokeBombConsumableEffect());

        // Autres
        effectRegistry.put("invisibility_cancel_on_shot", new InvisibilityConsumableEffect());
        effectRegistry.put("detection_immune", new DetectionImmuneConsumableEffect());
        effectRegistry.put("fake_death", new FakeDeathConsumableEffect());

        // Économie (paris)
        effectRegistry.put("bet_win_x4", new BetWinBonusConsumableEffect());
        effectRegistry.put("bet_double_earnings", new BetDoubleEarningsConsumableEffect());

        effectRegistry.put("piercing_shots", new PiercingShotsConsumableEffect());
    }

    public void loadAll() {
        definitions.clear();

        File folder = new File(plugin.getDataFolder(), "consumables");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Aucun fichier de consommable trouvé dans " + folder.getPath());
            return;
        }

        for (File file : files) {
            try {
                ConsumableDefinition def = parse(file);
                if (def == null) continue;
                if (!def.isValid()) {
                    plugin.getLogger().warning("Consommable '" + file.getName() + "' invalide - ignoré.");
                    continue;
                }
                if (!effectRegistry.containsKey(def.effectId())) {
                    plugin.getLogger().warning("Consommable '" + def.id() + "' référence un effect-id inconnu ('"
                            + def.effectId() + "') - ignoré.");
                    continue;
                }
                if (definitions.containsKey(def.id())) {
                    plugin.getLogger().warning("Id de consommable dupliqué '" + def.id() + "' dans " + file.getName() + " - ignoré.");
                    continue;
                }
                definitions.put(def.id(), def);
                plugin.getLogger().info("Consommable chargé : " + def.id() + " (" + def.displayName() + ")");
            } catch (Exception e) {
                plugin.getLogger().severe("Erreur lors du chargement de " + file.getName() + " : " + e.getMessage());
            }
        }
    }

    private ConsumableDefinition parse(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        String id = yaml.getString("id");
        if (id == null || id.isBlank()) {
            plugin.getLogger().warning(file.getName() + " : champ 'id' manquant - ignoré.");
            return null;
        }

        ConsumableCategory category;
        try {
            category = ConsumableCategory.valueOf(yaml.getString("category", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(file.getName() + " : 'category' invalide - ignoré.");
            return null;
        }

        Rarity rarity;
        try {
            rarity = Rarity.valueOf(yaml.getString("rarity", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(file.getName() + " : 'rarity' invalide - ignoré.");
            return null;
        }

        Material material;
        try {
            material = Material.valueOf(yaml.getString("icon-material","AIR").toUpperCase());
        }catch (IllegalArgumentException e) {
            plugin.getLogger().warning(file.getName() + " : 'material' invalide - ignoré.");
            return null;
        }

        return new ConsumableDefinition(
                id,
                yaml.getString("display-name", id),
                category,
                rarity,
                yaml.getInt("price", 0),
                material,
                yaml.getString("effect-id", ""),
                yaml.getString("glyph", ""),
                yaml.getString("description", ""),
                yaml.getString("stat-modification", "")
        );
    }

    public Optional<ConsumableDefinition> getDefinition(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public List<ConsumableDefinition> getAllDefinitions() {
        return new ArrayList<>(definitions.values());
    }

    public boolean activate(GamePlayer gp, Player player, Match match, int slot) {
        Optional<String> idOpt = gp.getConsumables().get(slot);
        if (idOpt.isEmpty()) {
            player.sendMessage("§7Slot " + (slot + 1) + " vide.");
            return false;
        }

        ConsumableDefinition def = definitions.get(idOpt.get());
        if (def == null) {
            player.sendMessage("§cObjet invalide (config manquante), retiré de ton inventaire.");
            gp.getConsumables().clear(slot);
            return false;
        }

        ConsumableEffect effect = effectRegistry.get(def.effectId());
        if (effect == null) {
            player.sendMessage("§cEffet introuvable pour " + def.displayName() + ".");
            return false;
        }

        boolean success = effect.activate(new ActivationContext(gp, player, match, plugin, def.price()));
        if (!success) {
            return false;
        }

        gp.getConsumables().clear(slot);
        gp.incrementConsumablesUsed();
        return true;
    }
}
