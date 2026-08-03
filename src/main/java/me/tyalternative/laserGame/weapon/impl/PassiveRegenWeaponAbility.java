package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.weapon.StatModifier;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

public class PassiveRegenWeaponAbility implements WeaponAbility {

    private static final int REGEN_AMOUNT = 1;
    private static final long REGEN_INTERVAL_TICKS = 100;

    private final StatModifier modifier = stats -> {
        stats.setPassiveRegenAmount(REGEN_AMOUNT);
        stats.setPassiveRegenIntervalTicks(REGEN_INTERVAL_TICKS);
    };

    @Override
    public String getId() {
        return "passive_ammo_regen";
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
