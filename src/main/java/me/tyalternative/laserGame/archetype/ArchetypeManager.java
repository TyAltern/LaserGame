package me.tyalternative.laserGame.archetype;

import me.tyalternative.laserGame.archetype.impl.*;
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

    public ArchetypeManager(Plugin plugin) {
        this.plugin = plugin;
        registerEffects();
    }

    private void registerEffects() {
        effectRegistry.put("berserker", new BerserkerArchetypeEffect());
        effectRegistry.put("nervous_trigger", new NervousTriggerArchetypeEffect());
        effectRegistry.put("vampire", new VampireArchetypeEffect());
    }

    public void loadAll() {
        definitions.clear();

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
                if (!effectRegistry.containsKey(def.effectId())) {
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

        return new ArchetypeDefinition(
                id,
                yaml.getString("display-name", id),
                rarity,
                yaml.getInt("price", 0),
                yaml.getString("effect-id", "")
        );
    }

    public Optional<ArchetypeDefinition> getDefinition(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public Optional<ArchetypeEffect> getEffect(String effectId) {
        return Optional.ofNullable(effectRegistry.get(effectId));
    }

    public List<ArchetypeDefinition> getAllDefinitions() {
        return new ArrayList<>(definitions.values());
    }
}
