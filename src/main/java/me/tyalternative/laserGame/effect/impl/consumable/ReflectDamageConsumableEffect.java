package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.ReflectNextHitEffect;

public class ReflectDamageConsumableEffect implements ConsumableEffect {
    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new ReflectNextHitEffect(ctx.match));
        ctx.player.sendMessage("§7Le prochain coup que tu reçois infligera aussi 1 dégât au tireur.");
        return true;
    }
}
