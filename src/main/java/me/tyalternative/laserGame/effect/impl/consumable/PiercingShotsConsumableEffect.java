package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.lang.Lang;

/** Les prochains tirs traversent les joueurs touchés au lieu de s'arrêter au premier (empilable). */
public class PiercingShotsConsumableEffect implements ConsumableEffect {

    private static final int SHOTS_GRANTED = 10;

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.grantPiercingShots(SHOTS_GRANTED);
        Lang.send(ctx.player, "effect.consumable.piercing_shots.activated", "shots", SHOTS_GRANTED);
        return true;
    }
}
