package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.CurseNextTargetEffect;

/** Malédiction appliquée au prochain joueur touché (voir CurseNextTargetEffect.Kind). */
public class CurseConsumableEffect implements ConsumableEffect {

    private final CurseNextTargetEffect.Kind kind;
    private final String message;

    public CurseConsumableEffect(CurseNextTargetEffect.Kind kind, String message) {
        this.kind = kind;
        this.message = message;
    }

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new CurseNextTargetEffect(kind));
        ctx.player.sendMessage(message);
        return true;
    }
}
