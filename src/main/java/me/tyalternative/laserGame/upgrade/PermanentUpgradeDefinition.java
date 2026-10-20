package me.tyalternative.laserGame.upgrade;

import me.tyalternative.laserGame.shop.HasDescription;
import me.tyalternative.laserGame.shop.HasRarity;
import me.tyalternative.laserGame.shop.Rarity;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public record PermanentUpgradeDefinition(
        String id,
        String displayName,
        Rarity rarity,
        int price,
        String effectId,
        Material material,
        String description,
        List<String> statModification,
        String requires,
        Map<String, Object> params
) implements HasRarity, HasDescription {

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
