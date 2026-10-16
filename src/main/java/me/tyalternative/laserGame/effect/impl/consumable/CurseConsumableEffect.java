package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.CurseNextTargetEffect;
import me.tyalternative.laserGame.lang.Lang;

/** Malédiction appliquée au prochain joueur touché (voir CurseNextTargetEffect.Kind). */
public class CurseConsumableEffect implements ConsumableEffect {

    private final CurseNextTargetEffect.Kind kind;
    private final String messageKey;

    public CurseConsumableEffect(CurseNextTargetEffect.Kind kind, String messageKey) {
        this.kind = kind;
        this.messageKey = messageKey;
    }

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new CurseNextTargetEffect(kind));
        Lang.send(ctx.player, messageKey);
        return true;
    }
}
