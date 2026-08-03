package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.weapon.StatModifier;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

public class MassiveMagazineWeaponAbility implements WeaponAbility {

    private static final int MULTIPLIER = 5;

    private final StatModifier modifier = stats -> {
        stats.setMaxAmmo(stats.getMaxAmmo() * MULTIPLIER);
        stats.setReloadDisabled(true);
    };

    @Override
    public String getId() {
        return "massive_magazine";
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
