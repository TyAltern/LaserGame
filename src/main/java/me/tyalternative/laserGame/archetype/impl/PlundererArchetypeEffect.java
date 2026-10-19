package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

/** Pilleur : plus d'argent de combat, mais moins de munitions max. */
public class PlundererArchetypeEffect implements ArchetypeEffect {

    private final StatModifier modifier;

    public PlundererArchetypeEffect(double moneyBonus, double ammoPenalty) {
        this.modifier = stats -> {
            stats.setCombatCurrencyMultiplier(stats.getCombatCurrencyMultiplier() * (1.0 + moneyBonus));
            stats.setMaxAmmo(Math.max(1, (int) Math.round(stats.getMaxAmmo() * (1.0 - ammoPenalty))));
        };
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
