package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.lang.Lang;

public class ReloadOverflowConsumableEffect implements ConsumableEffect {

    private static final int BONUS_AMMO = 5;

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getWeapon().grantNextReloadBonus(BONUS_AMMO);
        Lang.send(ctx.player, "effect.consumable.reload_overflow.activated", "amount", BONUS_AMMO);
        return true;
    }
}
