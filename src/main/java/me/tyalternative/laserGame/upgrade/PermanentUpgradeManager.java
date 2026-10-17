package me.tyalternative.laserGame.upgrade;

import java.util.function.Function;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.shop.ItemHint.StatHint;
import me.tyalternative.laserGame.shop.ItemHint.StatTag;
import me.tyalternative.laserGame.shop.Rarity;
import me.tyalternative.laserGame.upgrade.impl.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PermanentUpgradeManager {

    private final Plugin plugin;
    private final Map<String, PermanentUpgradeDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, Function<UpgradeSettings, PermanentUpgradeEffect>> effectFactories = new HashMap<>();
    private final Map<String, PermanentUpgradeEffect> effectByUpgradeId = new HashMap<>();

    public PermanentUpgradeManager(Plugin plugin) {
        this.plugin = plugin;
        registerEffects();
    }

    private void registerEffects() {
        effectFactories.put("speed_boost_permanent", s -> new PermanentSpeedBoostEffect());
        effectFactories.put("fast_reload_permanent", s -> new FastReloadUpgradeEffect());
        effectFactories.put("extra_slot", s -> new ExtraSlotUpgradeEffect());
        effectFactories.put("extra_shop_slot", s -> new ExtraShopSlotUpgradeEffect());
        effectFactories.put("reload_shield", s -> new ReloadShieldUpgradeEffect());

        // Combat
        effectFactories.put("revenge", s -> new RevengeUpgradeEffect(
                "all".equalsIgnoreCase(s.getString("mode", "last")), s.getInt("bonus", 1), s.getString("overridden-by", "")));
        effectFactories.put("first_hit_bonus", s -> new FirstHitBonusUpgradeEffect(s.getInt("bonus", 1)));
        effectFactories.put("ambush", s -> new AmbushUpgradeEffect(s.getLong("seconds", 3), s.getInt("bonus", 1)));
        effectFactories.put("respawn_immunity", s -> new RespawnImmunityUpgradeEffect(s.getLong("seconds", 10)));
        effectFactories.put("bonus_life", s -> new BonusLifeUpgradeEffect(s.getInt("amount", 1)));
        effectFactories.put("speed_per_hit", s -> new SpeedPerHitUpgradeEffect(s.getDouble("per-hit", 0.05), s.getDouble("cap", 0.30)));
        effectFactories.put("skill_cooldown", s -> new SkillCooldownUpgradeEffect(s.getDouble("reduction", 0.10)));
        effectFactories.put("weapon_stat", s -> new WeaponStatUpgradeEffect(s.getString("stat", "range"), s.getDouble("percent", 0.20)));
        effectFactories.put("ammo_regen", s -> new AmmoRegenUpgradeEffect(s.getInt("amount", 1), s.getLong("interval-seconds", 10)));
        effectFactories.put("crouch_time", s -> new CrouchTimeUpgradeEffect(s.getDouble("percent", 0.25)));
        effectFactories.put("radar", s -> new RadarUpgradeEffect(s.getLong("reduction-seconds", 30), s.getBoolean("from-start", false)));

        // Économie et boutique
        effectFactories.put("combat_money", s -> new CombatMoneyUpgradeEffect(s.getString("kind", "hit"), s.getInt("amount", 1)));
        effectFactories.put("round_start_money", s -> new RoundStartMoneyUpgradeEffect(
                s.getInt("amount", 0), s.getDouble("percent", 0.0), s.getInt("cap", Integer.MAX_VALUE)));
        effectFactories.put("shop_discount", s -> new ShopDiscountUpgradeEffect(s.getDouble("percent", 0.10)));
        effectFactories.put("duplicate_consumables", s -> new DuplicateConsumablesUpgradeEffect());
        effectFactories.put("free_first_roll", s -> new FreeFirstRollUpgradeEffect());
        effectFactories.put("flawless_bonus", s -> new FlawlessBonusUpgradeEffect(s.getInt("points", 1)));
    }

    public void loadAll() {
        definitions.clear();
        effectByUpgradeId.clear();

        File folder = new File(plugin.getDataFolder(), "upgrades");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Aucun fichier d'amélioration trouvé dans " + folder.getPath());
            return;
        }

        for (File file : files) {
            try {
                PermanentUpgradeDefinition def = parse(file);
                if (def == null) continue;
                if (!def.isValid()) {
                    plugin.getLogger().warning("Amélioration '" + file.getName() + "' invalide -> ignorée.");
                    continue;
                }
                if (!effectByUpgradeId.containsKey(def.effectId())) {
                    plugin.getLogger().warning("Amélioration '" + def.id() + "' référence un effect-id inconnu ('"
                            + def.effectId() + "') -> ignorée.");
                    continue;
                }
                if (definitions.containsKey(def.id())) {
                    plugin.getLogger().warning("Id d'amélioration dupliqué '" + def.id() + "' dans " + file.getName() + " -> ignoré.");
                    continue;
                }
                definitions.put(def.id(), def);
                plugin.getLogger().info("Amélioration chargée : " + def.id() + " (" + def.displayName() + ")");
            } catch (Exception e) {
                plugin.getLogger().severe("Erreur lors du chargement de " + file.getName() + " : " + e.getMessage());
            }
        }

        validateRequirements();

        for (PermanentUpgradeDefinition def : definitions.values()) {
            effectByUpgradeId.put(def.id(),
                    effectFactories.get(def.effectId()).apply(new UpgradeSettings(def.id(), def.params())));
        }
    }

    private void validateRequirements() {
        boolean removed;
        do {
            removed = false;
            for (PermanentUpgradeDefinition def : new ArrayList<>(definitions.values())) {
                if (!def.hasRequirement()) continue;

                String problem = null;
                Set<String> seen = new HashSet<>();
                PermanentUpgradeDefinition cursor = def;
                while (cursor != null && cursor.hasRequirement()) {
                    if (!seen.add(cursor.id())) {
                        problem = "chaine de prérequis circulaire";
                        break;
                    }
                    PermanentUpgradeDefinition parent = definitions.get(cursor.requires());
                    if (parent == null) {
                        problem = "prérequis inconnu ('" + cursor.requires() + "')";
                        break;
                    }
                    cursor = parent;
                }

                if (problem != null) {
                    plugin.getLogger().warning("Amélioration '" + def.id() + "' : " + problem + " -> ignorée.");
                    definitions.remove(def.id());
                    removed = true;
                }
            }
        } while (removed);
    }

    private PermanentUpgradeDefinition parse(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        String id = yaml.getString("id");
        if (id == null || id.isBlank()) {
            plugin.getLogger().warning(file.getName() + " : champ 'id' manquant -> ignoré.");
            return null;
        }

        Rarity rarity;
        try {
            rarity = Rarity.valueOf(yaml.getString("rarity", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(file.getName() + " : 'rarity' invalide -> ignoré.");
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

        return new PermanentUpgradeDefinition(
                id,
                yaml.getString("display-name", id),
                rarity,
                yaml.getInt("price", 0),
                yaml.getString("effect-id", ""),
                yaml.getString("glyph", ""),
                yaml.getString("description", ""),
                hints,
                yaml.getString("requires"),
                params
        );
    }

    public Optional<PermanentUpgradeDefinition> getDefinition(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public Optional<PermanentUpgradeEffect> getEffect(PermanentUpgradeDefinition def) {
        return Optional.ofNullable(effectByUpgradeId.get(def.id()));
    }

    public List<PermanentUpgradeDefinition> getAllDefinitions() {
        return new ArrayList<>(definitions.values());
    }

    public boolean isUnlocked(GamePlayer gp, PermanentUpgradeDefinition def) {
        return !def.hasRequirement() || gp.ownsUpgrade(def.requires());
    }

    /** Améliorations que ce joueur peut acheter maintenant (non possédées et débloquées). */
    public List<PermanentUpgradeDefinition> getAvailableFor(GamePlayer gp) {
        List<PermanentUpgradeDefinition> available = new ArrayList<>();
        for (PermanentUpgradeDefinition def : definitions.values()) {
            if (!gp.ownsUpgrade(def.id()) && isUnlocked(gp, def)) {
                available.add(def);
            }
        }
        return available;
    }
}
