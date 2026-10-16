package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.FreeShotNextEffect;
import me.tyalternative.laserGame.lang.Lang;

public class FreeShotConsumableEffect implements ConsumableEffect {
    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new FreeShotNextEffect());
        Lang.send(ctx.player, "effect.consumable.free_shot.activated");
        return true;
    }
}
