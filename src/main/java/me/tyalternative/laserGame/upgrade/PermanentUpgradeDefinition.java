package me.tyalternative.laserGame.upgrade;

import me.tyalternative.laserGame.shop.HasRarity;
import me.tyalternative.laserGame.shop.Rarity;

public record PermanentUpgradeDefinition(
        String id,
        String displayName,
        Rarity rarity,
        int price,
        String effectId
) implements HasRarity {
    public boolean isValid() {
        if (id == null || id.isBlank()) return false;
        if (rarity == null) return false;
        if (price < 0) return false;
        if (effectId == null || effectId.isBlank()) return false;
        return true;
    }
}
