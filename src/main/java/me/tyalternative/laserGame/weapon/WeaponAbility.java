package me.tyalternative.laserGame.weapon;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;

import java.util.Optional;

public interface WeaponAbility {

    String getId();

    default Optional<StatModifier> getStatModifier() {
        return Optional.empty();
    }

    default Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.empty();
    }
}
