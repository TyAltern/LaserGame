package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.RespawnGraceEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Touché moins de 15 secondes après avoir respawn : aucune perte de vie. */
public class RespawnGraceWeaponAbility implements WeaponAbility {

    @Override
    public String getId() {
        return "respawn_grace_period";
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new RespawnGraceEffect());
    }
}
