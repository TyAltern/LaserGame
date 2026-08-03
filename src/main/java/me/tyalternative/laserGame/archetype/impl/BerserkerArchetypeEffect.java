package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.BerserkerEffect;

import java.util.Optional;

public class BerserkerArchetypeEffect implements ArchetypeEffect {
    @Override
    public Optional<PendingEffect> getPersistentEffect() {
        return Optional.of(new BerserkerEffect());
    }
}
