package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.weapon.StatModifier;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Le rechargement peut être interrompu (relâcher le clic) et repris à n'importe quel moment sans perdre la progression. */
public class PausableReloadWeaponAbility implements WeaponAbility {

    private final StatModifier modifier = stats -> stats.setReloadPausable(true);

    @Override
    public String getId() {
        return "pausable_reload";
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
