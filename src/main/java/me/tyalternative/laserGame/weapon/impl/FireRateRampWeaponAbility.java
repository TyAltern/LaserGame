package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.FireRateRampOnMissEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** La vitesse de tir augmente à chaque tir raté, reset au tir réussi ou au rechargement. */
public class FireRateRampWeaponAbility implements WeaponAbility {

    @Override
    public String getId() {
        return "fire_rate_ramp_on_miss";
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new FireRateRampOnMissEffect(owner));
    }
}
