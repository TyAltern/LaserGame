package me.tyalternative.laserGame.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConsumableInventory {

    private final List<String> slots = new ArrayList<>();

    public ConsumableInventory(int initialSize) {
        for (int i = 0; i < initialSize; i++) {
            slots.add(null);
        }
    }

    public void grow(int newSize) {
        while (slots.size() < newSize) {
            slots.add(null);
        }
    }

    public boolean add(String consumableId, DuplicatePolicy policy) {
        if (policy == DuplicatePolicy.BLOCK_PURCHASE && slots.contains(consumableId)) return false;

        int freeIndex = slots.indexOf(null);
        if (freeIndex == -1) {
            return false;
        }
        slots.set(freeIndex, consumableId);
        return true;
    }

    public Optional<String> get(int index) {
        if (index < 0 || index >= slots.size()) return Optional.empty();
        return Optional.ofNullable(slots.get(index));
    }

    public void clear(int index) {
        if (index >= 0 && index <= slots.size()) {
            slots.set(index, null);
        }
    }

    public int size() {
        return slots.size();
    }
}
