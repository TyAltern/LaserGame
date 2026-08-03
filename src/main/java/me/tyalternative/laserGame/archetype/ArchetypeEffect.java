package me.tyalternative.laserGame.archetype;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

public interface ArchetypeEffect {

    default Optional<StatModifier> getStatModifier() {
        return Optional.empty();
    }

    default Optional<PendingEffect> getPersistentEffect() {
        return Optional.empty();
    }
}
