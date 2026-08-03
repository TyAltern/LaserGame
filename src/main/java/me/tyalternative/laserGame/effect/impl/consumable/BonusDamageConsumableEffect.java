package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.NextShotBonusDamageEffect;

public class BonusDamageConsumableEffect implements ConsumableEffect {
    @Override
    public void activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new NextShotBonusDamageEffect());
        ctx.player.sendMessage("§7Ton prochain tir réussi infligera 1 dégât supplémentaire.");
    }
}
