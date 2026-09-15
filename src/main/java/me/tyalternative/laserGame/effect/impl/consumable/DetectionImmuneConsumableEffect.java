package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;

/** Invulnérabilité aux items de détection adverse (glow...) pour le round. */
public class DetectionImmuneConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.setDetectionImmune(true);
        ctx.player.sendMessage("§7Tu es immunisé aux effets de détection adverses pour ce round.");
        return true;
    }
}
