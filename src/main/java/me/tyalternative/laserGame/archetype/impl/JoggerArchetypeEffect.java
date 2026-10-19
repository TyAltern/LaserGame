package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

/** Jogger : plus rapide, mais portée réduite. */
public class JoggerArchetypeEffect implements ArchetypeEffect {

    private final StatModifier modifier;

    public JoggerArchetypeEffect(double speedBonus, double rangePenalty) {
        this.modifier = stats -> {
            stats.setMovementSpeedModifier(stats.getMovementSpeedModifier() + speedBonus);
            stats.setRange(stats.getRange() * (1.0 - rangePenalty));
        };
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
