package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

public class NervousTriggerArchetypeEffect implements ArchetypeEffect {

    private final StatModifier modifier = stats -> {
        stats.setReloadCooldownTicks(Math.max(1, stats.getReloadCooldownTicks()) / 2);
        stats.setMaxAmmo(Math.max(1, Math.round(stats.getMaxAmmo() * 0.66f)));
    };

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
