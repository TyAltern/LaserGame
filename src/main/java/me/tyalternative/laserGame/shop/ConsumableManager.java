package me.tyalternative.laserGame.shop;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.consumable.*;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
        effectRegistry.put("damage_cap_next_hit", new DamageCapConsumableEffect());
        effectRegistry.put("bonus_damage_next_shot", new BonusDamageConsumableEffect());
        effectRegistry.put("instant_reload", new InstantReloadConsumableEffect());
        effectRegistry.put("glow_nearby", new GlowNearbyConsumableEffect());
        effectRegistry.put("speed_boost", new SpeedBoostConsumableEffect());
        effectRegistry.put("invisibility_cancel_on_shot", new InvisibilityConsumableEffect());
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

        return new ConsumableDefinition(
                id,
                yaml.getString("display-name", id),
                category,
                rarity,
                yaml.getInt("price", 0),
                yaml.getString("effect-id", "")
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

        gp.getConsumables().clear(slot);
        effect.activate(new ActivationContext(gp, player, match, plugin));
        return true;
    }
}
