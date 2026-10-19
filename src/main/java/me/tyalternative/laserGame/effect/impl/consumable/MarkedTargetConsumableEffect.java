package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.MarkNextTargetEffect;
import me.tyalternative.laserGame.lang.Lang;

/** Cible Marquée : le prochain joueur touché reste en surbrillance (visible de tous, comme les autres surbrillances). */
public class MarkedTargetConsumableEffect implements ConsumableEffect {

    private final long seconds;

    public MarkedTargetConsumableEffect(long seconds) {
        this.seconds = seconds;
    }

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new MarkNextTargetEffect((int) (seconds * 20L)));
        Lang.send(ctx.player, "effect.consumable.marked_target.activated", "duration", Lang.duration(seconds));
        return true;
    }
}
