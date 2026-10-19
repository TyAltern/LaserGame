package me.tyalternative.laserGame.shop;

import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import java.util.function.Function;
import me.tyalternative.laserGame.shop.ItemSettings;
import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.consumable.*;
import me.tyalternative.laserGame.effect.impl.effect.CurseNextTargetEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.shop.ItemHint.StatHint;
import me.tyalternative.laserGame.shop.ItemHint.StatTag;
import me.tyalternative.laserGame.lang.Lang;
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
    private final Map<String, Function<ItemSettings, ConsumableEffect>> effectFactories = new HashMap<>();
    private final Map<String, ConsumableEffect> effectInstances = new HashMap<>();

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
                "effect.consumable.curse_troll_weapon.activated"));
        effectRegistry.put("curse_double_cooldown", new CurseConsumableEffect(CurseNextTargetEffect.Kind.DOUBLE_COOLDOWN,
                "effect.consumable.curse_double_cooldown.activated"));
        effectRegistry.put("curse_ammo_loss", new CurseConsumableEffect(CurseNextTargetEffect.Kind.AMMO_LOSS,
                "effect.consumable.curse_ammo_loss.activated"));

        // Armes et munitions
        effectRegistry.put("instant_reload", new InstantReloadConsumableEffect());
        effectRegistry.put("reload_overflow", new ReloadOverflowConsumableEffect());
        effectRegistry.put("round_max_ammo_boost", new RoundWeaponBoostConsumableEffect(
                RoundWeaponBoostConsumableEffect.Kind.MAX_AMMO, 0.20, "effect.consumable.round_max_ammo_boost.activated"));
        effectRegistry.put("round_range_boost", new RoundWeaponBoostConsumableEffect(
                RoundWeaponBoostConsumableEffect.Kind.RANGE, 0.30, "effect.consumable.round_range_boost.activated"));
        effectRegistry.put("round_cooldown_reduction", new RoundWeaponBoostConsumableEffect(
                RoundWeaponBoostConsumableEffect.Kind.COOLDOWN, -0.20, "effect.consumable.round_cooldown_reduction.activated"));
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
                PotionEffectType.SLOWNESS, 250, 20.0, 100, "effect.consumable.freeze_cc.activated"));
        effectRegistry.put("nausea_cc", new AreaPotionConsumableEffect(
                PotionEffectType.NAUSEA, 0, 25.0, 200, "effect.consumable.nausea_cc.activated"));
        effectRegistry.put("slow_cc", new AreaPotionConsumableEffect(
                PotionEffectType.SLOWNESS, 1, 30.0, 300, "effect.consumable.slow_cc.activated"));
        effectRegistry.put("smoke_bomb", new SmokeBombConsumableEffect());

        // Autres
        effectRegistry.put("invisibility_cancel_on_shot", new InvisibilityConsumableEffect());
        effectRegistry.put("detection_immune", new DetectionImmuneConsumableEffect());
        effectRegistry.put("fake_death", new FakeDeathConsumableEffect());

        // Économie (paris)
        effectRegistry.put("bet_win_x4", new BetWinBonusConsumableEffect());
        effectRegistry.put("bet_double_earnings", new BetDoubleEarningsConsumableEffect());

        effectRegistry.put("piercing_shots", new PiercingShotsConsumableEffect());

        // Nouveaux consommables configurables (params dans le YAML)
        effectFactories.put("lotto_ticket", s -> new LottoTicketConsumableEffect(s.getInt("gain", 30)));
        effectFactories.put("bandage", s -> new BandageConsumableEffect(s.getInt("amount", 1)));
        effectFactories.put("pocket_radar", s -> new PocketRadarConsumableEffect());
        effectFactories.put("marked_target", s -> new MarkedTargetConsumableEffect(s.getLong("seconds", 15)));
        effectFactories.put("money_rain", s -> new MoneyRainConsumableEffect(s.getInt("amount", 5)));
        effectFactories.put("surprise_package", s -> new SurprisePackageConsumableEffect(s.getInt("count", 2)));
        effectFactories.put("express_return", s -> new ExpressReturnConsumableEffect());
    }

    private ConsumableEffect resolveEffect(ConsumableDefinition def) {
        ConsumableEffect configured = effectInstances.get(def.id());
        return configured != null ? configured : effectRegistry.get(def.effectId());
    }

    public void loadAll() {
        definitions.clear();
        effectInstances.clear();

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
                if (!effectRegistry.containsKey(def.effectId()) && !effectFactories.containsKey(def.effectId())) {
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

        for (ConsumableDefinition def : definitions.values()) {
            Function<ItemSettings, ConsumableEffect> factory = effectFactories.get(def.effectId());
            if (factory != null) {
                effectInstances.put(def.id(), factory.apply(new ItemSettings(def.id(), def.params())));
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

        List<String> hints = new ArrayList<>();
        List<?> rawHints = yaml.getList("stat-hints");
        if (rawHints != null) {
            for (Object raw : rawHints) {
                if (raw instanceof Map<?, ?> map) {
                    try {
                        StatTag tag = StatTag.valueOf(String.valueOf(map.get("tag")));
                        String value = String.valueOf(map.get("value"));
                        hints.add(new StatHint(tag, value).getLine());
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning(file.getName() + " : stat-hint avec un tag invalide.");
                    }
                }
            }
        }

        Map<String, Object> params = new HashMap<>();
        ConfigurationSection paramsSection = yaml.getConfigurationSection("params");
        if (paramsSection != null) {
            params.putAll(paramsSection.getValues(false));
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
                hints,
                params
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
            Lang.send(player, "consumable.empty-slot", "slot", slot + 1);
            return false;
        }

        ConsumableDefinition def = definitions.get(idOpt.get());
        if (def == null) {
            Lang.send(player, "consumable.invalid");
            gp.getConsumables().clear(slot);
            return false;
        }

        ConsumableEffect effect = resolveEffect(def);
        if (effect == null) {
            Lang.send(player, "consumable.effect-missing", "name", Lang.color(def.displayName()));
            return false;
        }

        ActivationContext context = new ActivationContext(gp, player, match, plugin, def.price());
        boolean success = effect.activate(context);
        if (!success) {
            return false;
        }

        gp.getConsumables().clear(slot);
        gp.incrementConsumablesUsed();
        gp.setLastConsumable(def, effect);
        effect.afterConsumed(context); // le slot est libéré : permet par ex. au Colis Surprise de s'y loger
        return true;
    }

    public boolean repeatLastConsumable(GamePlayer gp, Player player, Match match, Set<String> excludedIds) {
        ConsumableDefinition def = gp.getLastConsumableDefinition();
        ConsumableEffect effect = gp.getLastConsumableEffect();
        if (def == null || effect == null || gp.isLastConsumableRepeated()) return false;
        if (excludedIds.contains(def.id())) return false;

        ActivationContext context = new ActivationContext(gp, player, match, plugin, def.price());
        if (!effect.activate(context)) return false;

        gp.markLastConsumableRepeated();
        effect.afterConsumed(context);
        return true;
    }

    public boolean canRepeatLastConsumable(GamePlayer gp, Set<String> excludedIds) {
        ConsumableDefinition def = gp.getLastConsumableDefinition();
        return def != null && gp.getLastConsumableEffect() != null && !gp.isLastConsumableRepeated()
                && !excludedIds.contains(def.id());
    }
}
