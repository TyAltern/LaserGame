package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;

public class InstantReloadConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        boolean success = ctx.gp.getWeapon().instantRefill();
        if (success) {
            ctx.player.sendMessage("§aArme rechargée instantanément !");
        } else {
            ctx.player.sendMessage("§cCette arme ne peut pas être rechargée.");
        }
        return success;
    }
}
