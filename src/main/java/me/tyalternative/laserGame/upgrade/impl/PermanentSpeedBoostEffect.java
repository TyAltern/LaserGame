package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

public class PermanentSpeedBoostEffect implements PermanentUpgradeEffect {

    private final double percent;
    private final StatModifier modifier;

    public PermanentSpeedBoostEffect(double percent) {
        this.percent = percent;
        this.modifier = stats ->
                stats.setMovementSpeedModifier(stats.getMovementSpeedModifier() + percent);
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
