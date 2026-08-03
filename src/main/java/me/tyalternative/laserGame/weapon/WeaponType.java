package me.tyalternative.laserGame.weapon;

import me.tyalternative.laserGame.shop.HasRarity;
import me.tyalternative.laserGame.shop.Rarity;
import org.bukkit.Material;

public record WeaponType(
        String id,
        String displayName,
        Material material,
        int maxAmmo,
        long shotCooldownTicks,
        long reloadCooldownTicks,
        double range,
        double hitRadius,
        int livesPerHit,
        double movementSpeedModifier,
        String specialAbilityId,
        Rarity rarity,
        int price
) implements HasRarity {
    public boolean isValid() {
        if (id == null || id.isBlank()) return false;
        if (material == null) return false;
        if (maxAmmo < 1) return false;
        if (shotCooldownTicks < 0 || reloadCooldownTicks < 0) return false;
        if (range <= 0 || hitRadius <= 0) return false;
        if (livesPerHit < 1) return false;
        if (rarity == null) return false;
        if (price < 0) return false;
        return true;
    }
}
