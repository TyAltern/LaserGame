package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.GlowSelfOnMissEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Chaque tir raté vous fait briller pendant 1 seconde. */
public class GlowSelfOnMissWeaponAbility implements WeaponAbility {

    @Override
    public String getId() {
        return "glow_self_on_miss";
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new GlowSelfOnMissEffect(owner));
    }
}
