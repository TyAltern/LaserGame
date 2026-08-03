package me.tyalternative.laserGame.shop;

public record ConsumableDefinition(
        String id,
        String displayName,
        ConsumableCategory category,
        Rarity rarity,
        int price,
        String effectId
) implements HasRarity {
    public boolean isValid() {
        if (id == null || id.isBlank()) return false;
        if (category == null || rarity == null) return false;
        if (price < 0) return false;
        if (effectId == null || effectId.isBlank()) return false;
        return true;
    }
}
