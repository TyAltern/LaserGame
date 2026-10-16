package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.IgnoreLongRangeHitEffect;
import me.tyalternative.laserGame.lang.Lang;

public class LongRangeImmuneConsumableEffect implements ConsumableEffect {

    private static final double MIN_DISTANCE = 15.0;

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new IgnoreLongRangeHitEffect(MIN_DISTANCE));
        Lang.send(ctx.player, "effect.consumable.long_range_immune.activated", "distance", (int) MIN_DISTANCE);
        return true;
    }
}
