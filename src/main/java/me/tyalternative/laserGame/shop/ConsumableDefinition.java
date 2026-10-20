package me.tyalternative.laserGame.shop;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public record ConsumableDefinition(
        String id,
        String displayName,
        ConsumableCategory category,
        Rarity rarity,
        int price,
        Material material,
        String effectId,
        String description,
        List<String> statModification,
        Map<String, Object> params
) implements HasRarity, HasDescription {
    public boolean isValid() {
        if (id == null || id.isBlank()) return false;
        if (category == null || rarity == null) return false;
        if (price < 0) return false;
        if (effectId == null || effectId.isBlank()) return false;
        return true;
    }
}
