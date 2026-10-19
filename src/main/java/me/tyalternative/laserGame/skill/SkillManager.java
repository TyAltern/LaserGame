package me.tyalternative.laserGame.skill;

import me.tyalternative.laserGame.effect.ConditionalSkillEffect;
import org.bukkit.configuration.ConfigurationSection;
import java.util.function.Function;
import me.tyalternative.laserGame.shop.ItemSettings;
import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.effect.impl.skillEffect.*;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.shop.ItemHint.StatHint;
import me.tyalternative.laserGame.shop.ItemHint.StatTag;
import me.tyalternative.laserGame.shop.Rarity;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.Material;
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
    private final Map<String, Function<ItemSettings, SkillEffect>> effectFactories = new HashMap<>();
    private final Map<String, SkillEffect> effectInstances = new HashMap<>();

    public SkillManager(Plugin plugin) {
        this.plugin = plugin;
        registerEffects();
    }

    private void registerEffects() {
        effectRegistry.put("grapple", new GrappleSkillEffect());
        effectRegistry.put("emergency_shield", new EmergencyShieldSkillEffect());
        effectRegistry.put("speed_burst", new SpeedBurstSkillEffect());

        effectFactories.put("infinite_magazine", s -> new InfiniteMagazineSkillEffect(s.getLong("seconds", 5), s.getDouble("fire-rate-bonus", 0.50)));
        effectFactories.put("spectral_veil", s -> new SpectralVeilSkillEffect(s.getLong("invisible-seconds", 20), s.getLong("lock-after-seconds", 3)));
        effectFactories.put("spring_box", s -> new SpringBoxSkillEffect(s.getDouble("power", 1.1)));
        effectFactories.put("emergency_reload", s -> new EmergencyReloadSkillEffect());
        effectFactories.put("position_swap", s -> new PositionSwapSkillEffect());
        effectFactories.put("repeat_consumable", s -> new RepeatConsumableSkillEffect(s.getString("excluded", "")));
        effectFactories.put("sonar", s -> new SonarSkillEffect(s.getLong("seconds", 3)));
        effectFactories.put("mirror", s -> new MirrorSkillEffect(s.getLong("seconds", 3), s.getInt("damage", 1)));
        effectFactories.put("second_wind", s -> new SecondWindSkillEffect());
        effectFactories.put("pickpocket", s -> new PickpocketSkillEffect(s.getLong("window-seconds", 10), s.getInt("amount", 10)));
    }

    private SkillEffect resolveEffect(SkillDefinition def) {
        SkillEffect configured = effectInstances.get(def.id());
        return configured != null ? configured : effectRegistry.get(def.effectId());
    }

    public void loadAll() {
        definitions.clear();
        effectInstances.clear();

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
                if (!effectRegistry.containsKey(def.effectId()) && !effectFactories.containsKey(def.effectId())) {
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

        for (SkillDefinition def : definitions.values()) {
            Function<ItemSettings, SkillEffect> factory = effectFactories.get(def.effectId());
            if (factory != null) {
                effectInstances.put(def.id(), factory.apply(new ItemSettings(def.id(), def.params())));
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

        return new SkillDefinition(
                id,
                yaml.getString("display-name", id),
                rarity,
                yaml.getInt("price", 0),
                yaml.getLong("cooldown-ticks", 100),
                yaml.getString("effect-id", ""),
                yaml.getString("glyph", ""),
                material,
                yaml.getString("description", ""),
                hints,
                params
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
            Lang.send(player, "skill.none-equipped");
            return false;
        }

        SkillDefinition def = definitions.get(skillId);
        if (def == null) {
            Lang.send(player, "skill.invalid");
            return false;
        }

        if (!gp.isSkillReady()) {
            long remainingSeconds = (gp.getSkillCooldownRemainingTicks() + 19) / 20; // arrondi au supérieur
            Lang.send(player, "skill.cooldown", "seconds", remainingSeconds);
            return false;
        }

        SkillEffect effect = effectRegistry.get(def.effectId());
        if (effect == null) {
            Lang.send(player, "skill.effect-missing", "name", Lang.color(def.displayName()));
            return false;
        }

        ActivationContext context = new ActivationContext(gp, player, match, plugin, def.price());
        if (effect instanceof ConditionalSkillEffect conditional && !conditional.canActivate(context)) {
            return false;
        }

        gp.startSkillCooldown(Math.max(1L, Math.round(def.cooldownTicks() * gp.getSkillCooldownMultiplier())));
        effect.activate(new ActivationContext(gp, player, match, plugin, def.price()));
        return true;
    }
}
