package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.RefundAmmoOnMissEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Un tir manqué ne consomme pas de munitions (munitions max faible en contrepartie). */
public class RefundAmmoOnMissWeaponAbility implements WeaponAbility {

    private final StatModifier modifier = stats -> stats.setMaxAmmo(Math.max(1, (int) Math.round(stats.getMaxAmmo() * 0.4)));

    @Override
    public String getId() {
        return "refund_ammo_on_miss";
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new RefundAmmoOnMissEffect(owner));
    }
}
