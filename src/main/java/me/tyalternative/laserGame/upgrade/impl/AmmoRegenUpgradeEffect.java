package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

/** Réserve Vivante : régénère des munitions à intervalle régulier (n'écrase jamais une régénération déjà meilleure). */
public class AmmoRegenUpgradeEffect implements PermanentUpgradeEffect {

    private final StatModifier modifier;

    public AmmoRegenUpgradeEffect(int amount, long intervalSeconds) {
        long intervalTicks = Math.max(1L, intervalSeconds * 20L);
        this.modifier = stats -> {
            long currentInterval = stats.getPassiveRegenIntervalTicks();
            double currentRate = currentInterval <= 0 ? 0.0 : (double) stats.getPassiveRegenAmount() / currentInterval;
            double newRate = (double) amount / intervalTicks;
            if (newRate > currentRate) {
                stats.setPassiveRegenAmount(amount);
                stats.setPassiveRegenIntervalTicks(intervalTicks);
            }
        };
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
