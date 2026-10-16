package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.lang.Lang;

/** Invulnérabilité aux items de détection adverse (glow...) pour le round. */
public class DetectionImmuneConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.setDetectionImmune(true);
        Lang.send(ctx.player, "effect.consumable.detection_immune.activated");
        return true;
    }
}
