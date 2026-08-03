package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.StreakBonusDamageEffect;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

public class StreakDamageWeaponAbility implements WeaponAbility {

    @Override
    public String getId() {
        return "streak_bonus_damage";
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect() {
        return Optional.of(new StreakBonusDamageEffect());
    }
}
