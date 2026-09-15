package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.RadiusRampAfterMissesEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Après 5 tirs ratés sans avoir été touché, augmente le rayon du tir jusqu'à réussir, être touché, ou fin de round. */
public class RadiusRampWeaponAbility implements WeaponAbility {

    @Override
    public String getId() {
        return "radius_ramp_after_misses";
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new RadiusRampAfterMissesEffect(owner));
    }
}
