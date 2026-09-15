package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.ExecuteNextShotEffect;

public class ExecuteConsumableEffect implements ConsumableEffect {
    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new ExecuteNextShotEffect());
        ctx.player.sendMessage("§7Ton prochain tir qui amène un joueur à sa dernière vie l'éliminera directement.");
        return true;
    }
}
