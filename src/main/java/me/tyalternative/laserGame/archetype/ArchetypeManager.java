package me.tyalternative.laserGame.archetype;

import org.bukkit.configuration.ConfigurationSection;
import java.util.function.Function;
import me.tyalternative.laserGame.shop.ItemSettings;
import me.tyalternative.laserGame.archetype.impl.*;
import me.tyalternative.laserGame.shop.ItemHint.StatHint;
import me.tyalternative.laserGame.shop.ItemHint.StatTag;
import me.tyalternative.laserGame.shop.Rarity;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArchetypeManager {

    private final Plugin plugin;
    private final Map<String, ArchetypeDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, ArchetypeEffect> effectRegistry = new HashMap<>();
    private final Map<String, Function<ItemSettings, ArchetypeEffect>> effectFactories = new HashMap<>();
    private final Map<String, ArchetypeEffect> effectInstances = new HashMap<>();

    public ArchetypeManager(Plugin plugin) {
        this.plugin = plugin;
        registerEffects();
    }

    private void registerEffects() {
        effectRegistry.put("berserker", new BerserkerArchetypeEffect());
        effectRegistry.put("nervous_trigger", new NervousTriggerArchetypeEffect());
        effectRegistry.put("vampire", new VampireArchetypeEffect());

        effectFactories.put("duelist", s -> new DuelistArchetypeEffect(s.getInt("first-shot-bonus", 2), s.getInt("other-shots-max-damage", 1),
                "hit".equalsIgnoreCase(s.getString("first-shot-mode", "shot"))));
        effectFactories.put("lightning_rod", s -> new LightningRodArchetypeEffect(s.getInt("life-modifier", -1)));
        effectFactories.put("flamboyant", s -> new FlamboyantArchetypeEffect(s.getInt("bonus-damage", 1), s.getInt("life-modifier", 1)));
        effectFactories.put("pay_to_win", s -> new PayToWinArchetypeEffect(s.getInt("full-reload-cost", 5)));
        effectFactories.put("immortal", s -> new ImmortalArchetypeEffect(s.getInt("life-modifier", 3), s.getDouble("slow-speed-penalty", 0.25),
                s.getDouble("slow-fire-factor", 2.0), s.getDouble("slow-reload-factor", 2.0), s.getDouble("slow-ammo-factor", 0.5)));
        effectFactories.put("outsider", s -> new OutsiderArchetypeEffect(s.getInt("bonus-damage", 1), s.getDouble("speed-penalty", 0.10)));
        effectFactories.put("jogger", s -> new JoggerArchetypeEffect(s.getDouble("speed-bonus", 0.15), s.getDouble("range-penalty", 0.20)));
        effectFactories.put("prowler", s -> new ProwlerArchetypeEffect(s.getDouble("crouch-time-bonus", 0.50), s.getDouble("fire-delay-penalty", 0.15)));
        effectFactories.put("plunderer", s -> new PlundererArchetypeEffect(s.getDouble("money-bonus", 0.50), s.getDouble("ammo-penalty", 0.20)));
        effectFactories.put("survivor", s -> new SurvivorArchetypeEffect(s.getInt("heal", 1), s.getInt("life-modifier", -1)));
        effectFactories.put("glass_cannon", s -> new GlassCannonArchetypeEffect(s.getInt("fixed-lives", 1), s.getInt("bonus-damage", 2), s.getDouble("speed-bonus", 0.25)));
    }

    public void loadAll() {
        definitions.clear();
        effectInstances.clear();

        File folder = new File(plugin.getDataFolder(), "archetypes");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Aucun fichier d'archétype trouvé dans " + folder.getPath());
            return;
        }

        for (File file : files) {
            try {
                ArchetypeDefinition def = parse(file);
                if (def == null) continue;
                if (!def.isValid()) {
                    plugin.getLogger().warning("Archétype '" + file.getName() + "' invalide - ignoré.");
                    continue;
                }
                if (!effectRegistry.containsKey(def.effectId()) && !effectFactories.containsKey(def.effectId())) {
                    plugin.getLogger().warning("Archétype '" + def.id() + "' référence un effect-id inconnu ('"
                            + def.effectId() + "') - ignoré.");
                    continue;
                }
                if (definitions.containsKey(def.id())) {
                    plugin.getLogger().warning("Id d'archétype dupliqué '" + def.id() + "' dans " + file.getName() + " - ignoré.");
                    continue;
                }
                definitions.put(def.id(), def);
                plugin.getLogger().info("Archétype chargé : " + def.id() + " (" + def.displayName() + ")");
            } catch (Exception e) {
                plugin.getLogger().severe("Erreur lors du chargement de " + file.getName() + " : " + e.getMessage());
            }
        }

        for (ArchetypeDefinition def : definitions.values()) {
            Function<ItemSettings, ArchetypeEffect> factory = effectFactories.get(def.effectId());
            if (factory != null) {
                effectInstances.put(def.id(), factory.apply(new ItemSettings(def.id(), def.params())));
            }
        }
    }

    private ArchetypeDefinition parse(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        String id = yaml.getString("id");
        if (id == null || id.isBlank()) {
            plugin.getLogger().warning(file.getName() + " : champ 'id' manquant - ignoré.");
            return null;
        }

        Rarity rarity;
        try {
            rarity = Rarity.valueOf(yaml.getString("rarity", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(file.getName() + " : 'rarity' invalide - ignoré.");
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

        return new ArchetypeDefinition(
                id,
                yaml.getString("display-name", id),
                rarity,
                yaml.getInt("price", 0),
                yaml.getString("effect-id", ""),
                yaml.getString("glyph", ""),
                yaml.getString("description", ""),
                hints,
                params
        );
    }

    public Optional<ArchetypeDefinition> getDefinition(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public Optional<ArchetypeEffect> getEffect(ArchetypeDefinition def) {
        ArchetypeEffect configured = effectInstances.get(def.id());
        return Optional.ofNullable(configured != null ? configured : effectRegistry.get(def.effectId()));
    }

    public List<ArchetypeDefinition> getAllDefinitions() {
        return new ArrayList<>(definitions.values());
    }
}
