package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.VampireOnHitEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

public class VampireArchetypeEffect implements ArchetypeEffect {

    private final StatModifier modifier = stats ->
            stats.setReloadCooldownTicks(stats.getReloadCooldownTicks() * 3);

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect() {
        return Optional.of(new VampireOnHitEffect());
    }
}
