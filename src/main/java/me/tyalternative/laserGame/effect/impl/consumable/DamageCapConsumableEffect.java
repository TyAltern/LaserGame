package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.DamageCapNextHitEffect;
import me.tyalternative.laserGame.lang.Lang;

public class DamageCapConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new DamageCapNextHitEffect(1));
        Lang.send(ctx.player, "effect.consumable.damage_cap_next_hit.activated");
        return true;
    }
}
