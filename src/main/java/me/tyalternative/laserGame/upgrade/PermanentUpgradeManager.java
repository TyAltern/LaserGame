package me.tyalternative.laserGame.upgrade;

import me.tyalternative.laserGame.shop.Rarity;
import me.tyalternative.laserGame.upgrade.impl.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PermanentUpgradeManager {

    private final Plugin plugin;
    private final Map<String, PermanentUpgradeDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, PermanentUpgradeEffect> effectRegistry = new HashMap<>();

    public PermanentUpgradeManager(Plugin plugin) {
        this.plugin = plugin;
        registerEffects();
    }

    private void registerEffects() {
        effectRegistry.put("speed_boost_permanent", new PermanentSpeedBoostEffect());
        effectRegistry.put("fast_reload_permanent", new FastReloadUpgradeEffect());
        effectRegistry.put("extra_slot", new ExtraSlotUpgradeEffect());
        effectRegistry.put("reload_shield", new ReloadShieldUpgradeEffect());
    }

    public void loadAll() {
        definitions.clear();

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
                if (!effectRegistry.containsKey(def.effectId())) {
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

        return new PermanentUpgradeDefinition(
                id,
                yaml.getString("display-name", id),
                rarity,
                yaml.getInt("price", 0),
                yaml.getString("effect-id", "")
        );
    }

    public Optional<PermanentUpgradeDefinition> getDefinition(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public Optional<PermanentUpgradeEffect> getEffect(String effectId) {
        return Optional.ofNullable(effectRegistry.get(effectId));
    }

    public List<PermanentUpgradeDefinition> getAllDefinitions() {
        return new ArrayList<>(definitions.values());
    }
}
