package me.tyalternative.laserGame.skill;

import me.tyalternative.laserGame.shop.HasRarity;
import me.tyalternative.laserGame.shop.Rarity;

public record SkillDefinition(
        String id,
        String displayName,
        Rarity rarity,
        int price,
        long cooldownTicks,
        String effectId
) implements HasRarity {
    public boolean isValid() {
        if (id == null || id.isBlank()) return false;
        if (rarity == null) return false;
        if (price < 0) return false;
        if (cooldownTicks < 0) return false;
        if (effectId == null || effectId.isBlank()) return false;
        return true;
    }
}
