package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.lang.Lang;

public class InstantReloadConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        boolean success = ctx.gp.getWeapon().instantRefill();
        if (success) {
            Lang.send(ctx.player, "effect.consumable.instant_reload.success");
        } else {
            Lang.send(ctx.player, "effect.consumable.instant_reload.failure");
        }
        return success;
    }
}
