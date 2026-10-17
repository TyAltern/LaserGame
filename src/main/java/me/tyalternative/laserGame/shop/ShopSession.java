package me.tyalternative.laserGame.shop;

import java.util.*;

public class ShopSession {

    private List<String> consumableSlotIds = new ArrayList<>();
    private int consumableRerollCount = 0;
    private final Set<Integer> freeConsumablesSlots = new HashSet<>();

    private final Map<SpecialSlotType, String> specialSlotsIds = new EnumMap<>(SpecialSlotType.class);
    private SpecialSlotType excludedSpecialType;

    public List<String> getConsumableSlotIds() {
        return consumableSlotIds;
    }

    public void setConsumableSlotIds(List<String> ids) {
        this.consumableSlotIds = ids;
    }

    public boolean isConsumablesSlotFree(int slotIndex) {
        return freeConsumablesSlots.contains(slotIndex);
    }

    public void markAlConsumableSlotsFree() {
        freeConsumablesSlots.clear();
        for (int i = 0; i < consumableSlotIds.size(); i++) freeConsumablesSlots.add(i);
    }

    public void clearFreeConsumablesSlots() {
        freeConsumablesSlots.clear();
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
