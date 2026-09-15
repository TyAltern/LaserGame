package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.LuckyMagazineEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Chaque tir a une chance de recharger instantanément le chargeur, et une petite chance de le vider. */
public class LuckyMagazineWeaponAbility implements WeaponAbility {

    @Override
    public String getId() {
        return "lucky_magazine";
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new LuckyMagazineEffect(owner));
    }
}
