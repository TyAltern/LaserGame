package me.tyalternative.laserGame.shop;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ShopSession {

    private List<String> consumableSlotIds = new ArrayList<>();
    private int consumableRerollCount = 0;

    private final Map<SpecialSlotType, String> specialSlotsIds = new EnumMap<>(SpecialSlotType.class);
    private SpecialSlotType excludedSpecialType;

    public List<String> getConsumableSlotIds() {
        return consumableSlotIds;
    }

    public void setConsumableSlotIds(List<String> ids) {
        this.consumableSlotIds = ids;
    }

    public int getConsumableRerollCount() {
        return consumableRerollCount;
    }

    public void incrementConsumableRerollCount() {
        this.consumableRerollCount++;
    }

    public Map<SpecialSlotType, String> getSpecialSlotIds() {
        return specialSlotsIds;
    }

    public SpecialSlotType getExcludedSpecialType() {
        return excludedSpecialType;
    }

    public void setExcludedSpecialType(SpecialSlotType type) {
        this.excludedSpecialType = type;
    }
}
