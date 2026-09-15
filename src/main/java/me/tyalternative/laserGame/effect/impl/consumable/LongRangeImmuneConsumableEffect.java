package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.IgnoreLongRangeHitEffect;

public class LongRangeImmuneConsumableEffect implements ConsumableEffect {

    private static final double MIN_DISTANCE = 50.0;

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new IgnoreLongRangeHitEffect(MIN_DISTANCE));
        ctx.player.sendMessage("§7Le prochain coup reçu de plus de " + (int) MIN_DISTANCE + " blocs sera ignoré.");
        return true;
    }
}
