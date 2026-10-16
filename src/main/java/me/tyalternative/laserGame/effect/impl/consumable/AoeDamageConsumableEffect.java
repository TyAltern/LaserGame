package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.AoeNextShotEffect;
import me.tyalternative.laserGame.lang.Lang;

public class AoeDamageConsumableEffect implements ConsumableEffect {

    private static final double RADIUS = 3.0;
    private static final int SPLASH_LIVES = 1;

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new AoeNextShotEffect(ctx.match, RADIUS, SPLASH_LIVES));
        Lang.send(ctx.player, "effect.consumable.aoe_next_shot.activated", "damage", SPLASH_LIVES);
        return true;
    }
}
