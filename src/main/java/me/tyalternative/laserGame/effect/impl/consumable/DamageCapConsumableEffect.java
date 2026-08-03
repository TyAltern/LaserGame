package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.DamageCapNextHitEffect;

public class DamageCapConsumableEffect implements ConsumableEffect {

    @Override
    public void activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new DamageCapNextHitEffect(1));
        ctx.player.sendMessage("§7Ton prochain coup reçu sera plafonné à 1 dégât.");
    }
}
