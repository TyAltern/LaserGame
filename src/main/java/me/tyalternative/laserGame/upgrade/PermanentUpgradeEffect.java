package me.tyalternative.laserGame.upgrade;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

public interface PermanentUpgradeEffect {

    default Optional<StatModifier> getStatModifier() {
        return Optional.empty();
    }

    default Optional<PendingEffect> getPersistentEffect() {
        return Optional.empty();
    }

    default void onPurchase(GamePlayer gp) {
    }
}
