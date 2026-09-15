package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.ReviveOnFirstEliminationEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Si vous êtes le premier joueur éliminé du round, vous réapparaissez avec 1 cœur. */
public class ReviveOnFirstEliminationWeaponAbility implements WeaponAbility {

    @Override
    public String getId() {
        return "revive_on_first_elimination";
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new ReviveOnFirstEliminationEffect());
    }
}
