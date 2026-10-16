package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.ExecuteNextShotEffect;
import me.tyalternative.laserGame.lang.Lang;

public class ExecuteConsumableEffect implements ConsumableEffect {
    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new ExecuteNextShotEffect());
        Lang.send(ctx.player, "effect.consumable.execute_next_shot.activated");
        return true;
    }
}
