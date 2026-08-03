package me.tyalternative.laserGame.weapon;

import me.tyalternative.laserGame.shop.Rarity;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WeaponManager {

    private final Plugin plugin;
    private final Map<String, WeaponType> weaponsById = new LinkedHashMap<>();
    private String defaultWeaponId;

    public WeaponManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public static NamespacedKey weaponIdKey(Plugin plugin) {
        return new NamespacedKey(plugin, "weapon_id");
    }

    public void loadAll() {
        weaponsById.clear();
        defaultWeaponId = null;

        File weaponsFolder = new File(plugin.getDataFolder(), "weapons");
        if (!weaponsFolder.exists()) {
            weaponsFolder.mkdirs();
        }

        File[] files = weaponsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Aucun fichier d'arme trouvé dans " + weaponsFolder.getPath()
                    + " - une arme de secours sera utilisée.");
            return;
        }

        for (File file : files) {
            try {
                WeaponType type = parse(file);
                if (type == null) continue;
                if (!type.isValid()) {
                    plugin.getLogger().warning("Arme '" + file.getName() + "' invalide (vérifie max-ammo, range, lives-per-hit...) - ignorée.");
                    continue;
                }
                if (weaponsById.containsKey(type.id())) {
                    plugin.getLogger().warning("Id d'arme dupliqué '" + type.id() + "' dans " + file.getName() + " - ignoré.");
                    continue;
                }
                weaponsById.put(type.id(), type);

                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                if (yaml.getBoolean("default", false) && defaultWeaponId == null) {
                    defaultWeaponId = type.id();
                }

                plugin.getLogger().info("Arme chargée : " + type.id() + " (" + type.displayName() + ")");
            } catch (Exception e) {
                plugin.getLogger().severe("Erreur lors du chargement de " + file.getName() + " : " + e.getMessage());
            }
        }

        if (defaultWeaponId == null && !weaponsById.isEmpty()) {
            defaultWeaponId = weaponsById.keySet().iterator().next();
            plugin.getLogger().info("Aucune arme marquée 'default: true', utilisation de '" + defaultWeaponId + "' par défaut.");
        }
    }

    private WeaponType parse(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        String id = yaml.getString("id");
        if (id == null || id.isBlank()) {
            plugin.getLogger().warning(file.getName() + " : champ 'id' manquant - ignoré.");
            return null;
        }

        Material material = Material.matchMaterial(yaml.getString("material", "BLAZE_ROD"));
        if (material == null) {
            plugin.getLogger().warning(file.getName() + " : matériau invalide - ignoré.");
            return null;
        }

        Rarity rarity;
        try {
            rarity = Rarity.valueOf(yaml.getString("rarity", "COMMON").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(file.getName() + " : 'rarity' invalide, fallback sur COMMON.");
            rarity = Rarity.COMMON;
        }

        return new WeaponType(
                id,
                yaml.getString("display-name", "&bArme"),
                material,
                yaml.getInt("max-ammo", 20),
                yaml.getLong("shot-cooldown-ticks", 4),
                yaml.getLong("reload-cooldown-ticks", 40),
                yaml.getDouble("range", 50.0),
                yaml.getDouble("hit-radius", 0.3),
                yaml.getInt("lives-per-hit", 1),
                yaml.getDouble("movement-speed-modifier", 0.0),
                yaml.getString("special-ability", null),
                rarity,
                yaml.getInt("price", 0)
        );
    }

    public Optional<WeaponType> resolve(ItemStack item) {
        if (item == null) return Optional.empty();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Optional.empty();

        String id = meta.getPersistentDataContainer().get(weaponIdKey(plugin), PersistentDataType.STRING);
        if (id == null) return Optional.empty();

        return getWeapon(id);
    }

    public Optional<WeaponType> getWeapon(String id) {
        return Optional.ofNullable(weaponsById.get(id));
    }

    public WeaponType getDefault() {
        if (defaultWeaponId != null) {
            WeaponType type = weaponsById.get(defaultWeaponId);
            if (type != null) return type;
        }
        return FALLBACK_WEAPON;
    }

    public List<WeaponType> getAllWeapons() {
        return new ArrayList<>(weaponsById.values());
    }

    private static final WeaponType FALLBACK_WEAPON = new WeaponType(
            "fallback", "&7Arme de secours", Material.BLAZE_ROD,
            20, 4, 40, 50.0, 0.3,
            1, 0.0, null, Rarity.COMMON, 0
    );
}
