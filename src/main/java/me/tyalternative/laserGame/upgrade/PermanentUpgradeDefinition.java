package me.tyalternative.laserGame.upgrade;

import me.tyalternative.laserGame.shop.HasDescription;
import me.tyalternative.laserGame.shop.HasGlyph;
import me.tyalternative.laserGame.shop.HasRarity;
import me.tyalternative.laserGame.shop.Rarity;

import java.util.List;
import java.util.Map;

public record PermanentUpgradeDefinition(
        String id,
        String displayName,
        Rarity rarity,
        int price,
        String effectId,
        String glyph,
        String description,
        List<String> statModification,
        String requires,
        Map<String, Object> params
) implements HasRarity, HasGlyph, HasDescription {

    public boolean hasRequirement() {
        return requires != null && !requires.isBlank();
    }

    public boolean isValid() {
        if (id == null || id.isBlank()) return false;
        if (rarity == null) return false;
        if (price < 0) return false;
        if (effectId == null || effectId.isBlank()) return false;
        return true;
    }
}
