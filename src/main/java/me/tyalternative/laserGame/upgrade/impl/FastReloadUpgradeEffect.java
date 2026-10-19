package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

public class FastReloadUpgradeEffect implements PermanentUpgradeEffect {

    private final double reduction;
    private final StatModifier modifier;

    public FastReloadUpgradeEffect(double reduction) {
        this.reduction = reduction;
        this.modifier = stats ->
                stats.setReloadCooldownTicks(Math.max(1, Math.round(stats.getReloadCooldownTicks() * reduction)));
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
