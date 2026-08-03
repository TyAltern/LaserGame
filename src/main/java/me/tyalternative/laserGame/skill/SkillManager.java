package me.tyalternative.laserGame.skill;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.effect.impl.skillEffect.*;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.shop.Rarity;
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

public class SkillManager {

    private final Plugin plugin;
    private final Map<String, SkillDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, SkillEffect> effectRegistry = new HashMap<>();

    public SkillManager(Plugin plugin) {
        this.plugin = plugin;
        registerEffects();
    }

    private void registerEffects() {
        effectRegistry.put("grapple", new GrappleSkillEffect());
        effectRegistry.put("emergency_shield", new EmergencyShieldSkillEffect());
        effectRegistry.put("speed_burst", new SpeedBurstSkillEffect());
    }

    public void loadAll() {
        definitions.clear();

        File folder = new File(plugin.getDataFolder(), "skills");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Aucun fichier de compétence trouvé dans " + folder.getPath());
            return;
        }

        for (File file : files) {
            try {
                SkillDefinition def = parse(file);
                if (def == null) continue;
                if (!def.isValid()) {
                    plugin.getLogger().warning("Compétence '" + file.getName() + "' invalide - ignorée.");
                    continue;
                }
                if (!effectRegistry.containsKey(def.effectId())) {
                    plugin.getLogger().warning("Compétence '" + def.id() + "' référence un effect-id inconnu ('"
                            + def.effectId() + "') - ignorée.");
                    continue;
                }
                if (definitions.containsKey(def.id())) {
                    plugin.getLogger().warning("Id de compétence dupliqué '" + def.id() + "' dans " + file.getName() + " - ignoré.");
                    continue;
                }
                definitions.put(def.id(), def);
                plugin.getLogger().info("Compétence chargée : " + def.id() + " (" + def.displayName() + ")");
            } catch (Exception e) {
                plugin.getLogger().severe("Erreur lors du chargement de " + file.getName() + " : " + e.getMessage());
            }
        }
    }

    private SkillDefinition parse(File file) {
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

        return new SkillDefinition(
                id,
                yaml.getString("display-name", id),
                rarity,
                yaml.getInt("price", 0),
                yaml.getLong("cooldown-ticks", 100),
                yaml.getString("effect-id", "")
        );
    }

    public Optional<SkillDefinition> getDefinition(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public List<SkillDefinition> getAllDefinitions() {
        return new ArrayList<>(definitions.values());
    }

    public boolean activate(GamePlayer gp, Player player, Match match) {
        String skillId = gp.getEquippedSkillId();
        if (skillId == null) {
            player.sendMessage("§7Aucune compétence équipée.");
            return false;
        }

        SkillDefinition def = definitions.get(skillId);
        if (def == null) {
            player.sendMessage("§cCompétence invalide (config manquante).");
            return false;
        }

        if (!gp.isSkillReady()) {
            long remainingSeconds = (gp.getSkillCooldownRemainingTicks() + 19) / 20; // arrondi au supérieur
            player.sendMessage("§cCompétence encore en recharge (" + remainingSeconds + "s).");
            return false;
        }

        SkillEffect effect = effectRegistry.get(def.effectId());
        if (effect == null) {
            player.sendMessage("§cEffet introuvable pour " + def.displayName() + ".");
            return false;
        }

        gp.startSkillCooldown(def.cooldownTicks());
        effect.activate(new ActivationContext(gp, player, match, plugin));
        return true;
    }
}
