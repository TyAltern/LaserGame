package me.tyalternative.laserGame.shop;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class RarityPool {

    private RarityPool() {
    }

    public static <T extends HasRarity> Optional<T> draw(List<T> candidates, Map<Rarity, Integer> baseWeights,
                                                         int pityLevel, int pityShiftPerLevel) {
        if (candidates.isEmpty()) return Optional.empty();

        Map<Rarity, Integer> effectiveWeights = applyPity(baseWeights, pityLevel, pityShiftPerLevel);

        List<T> weighted = new ArrayList<>();
        List<Integer> cumulative = new ArrayList<>();
        int total = 0;
        for (T candidate : candidates) {
            int w = effectiveWeights.getOrDefault(candidate.rarity(), 1);
            if (w <= 0) continue;
            total += w;
            weighted.add(candidate);
            cumulative.add(total);
        }

        if (weighted.isEmpty() || total <= 0) {
            return Optional.of(candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())));
        }

        int roll = ThreadLocalRandom.current().nextInt(total);
        for (int i = 0; i < weighted.size(); i++) {
            if (roll < cumulative.get(i)) {
                return Optional.of(weighted.get(i));
            }
        }
        return Optional.of(weighted.getLast());
    }

    private static Map<Rarity, Integer> applyPity(Map<Rarity, Integer> base, int pityLevel, int shiftPerLevel) {
        if (pityLevel <= 0 || shiftPerLevel <= 0) return base;

        Map<Rarity, Integer> result = new EnumMap<>(base);
        int totalShift = 0;
        for (Rarity r : new Rarity[]{Rarity.UNIQUE, Rarity.RARE, Rarity.LEGENDARY}) {
            int shift = shiftPerLevel * pityLevel;
            result.merge(r, shift, Integer::sum);
            totalShift += shift;
        }
        int commonWeight = result.getOrDefault(Rarity.COMMON,0);
        result.put(Rarity.COMMON, Math.max(0, commonWeight - totalShift));
        return result;
    }
}
