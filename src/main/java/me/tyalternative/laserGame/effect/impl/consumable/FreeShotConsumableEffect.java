package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.FreeShotNextEffect;

public class FreeShotConsumableEffect implements ConsumableEffect {
    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new FreeShotNextEffect());
        ctx.player.sendMessage("§7Ton prochain tir ne consommera pas de munition.");
        return true;
    }
}
