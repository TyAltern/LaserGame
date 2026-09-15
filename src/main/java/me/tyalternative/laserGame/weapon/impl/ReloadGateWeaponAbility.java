package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.IgnoreDamageWhileReloadingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Les coups reçus en rechargeant sont ignorés, mais on ne peut recharger qu'après avoir utilisé 3/4 du chargeur. */
public class ReloadGateWeaponAbility implements WeaponAbility {

    private final StatModifier modifier = stats -> stats.setMinAmmoUsedFractionToReload(0.75);

    @Override
    public String getId() {
        return "reload_gate_ignore_damage";
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new IgnoreDamageWhileReloadingEffect());
    }
}
