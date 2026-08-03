package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

public class PermanentSpeedBoostEffect implements PermanentUpgradeEffect {

    private final StatModifier modifier = stats ->
            stats.setMovementSpeedModifier(stats.getMovementSpeedModifier() + 0.10);

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
