package me.tyalternative.laserGame.weapon;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class WeaponAttributeKeys {

    private WeaponAttributeKeys() {
    }

    public static NamespacedKey speedModifier(Plugin plugin) {
        return new NamespacedKey(plugin, "weapon_speed_modifier");
    }
}
