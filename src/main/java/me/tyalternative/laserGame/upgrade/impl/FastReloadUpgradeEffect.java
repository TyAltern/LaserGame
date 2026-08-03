package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

public class FastReloadUpgradeEffect implements PermanentUpgradeEffect {

    private final StatModifier modifier = stats ->
            stats.setReloadCooldownTicks(Math.max(1, Math.round(stats.getReloadCooldownTicks() * 0.90)));

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
