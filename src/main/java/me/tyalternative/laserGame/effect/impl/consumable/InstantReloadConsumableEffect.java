package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;

public class InstantReloadConsumableEffect implements ConsumableEffect {

    @Override
    public void activate(ActivationContext ctx) {
        boolean success = ctx.gp.getWeapon().instantRefill();
        if (success) {
            ctx.player.sendMessage("§aArme rechargée instantanément !");
        } else {
            // Le consommable a déjà été retiré donc pas de reimbursement à voir si c'est à garder
            ctx.player.sendMessage("§cCette arme ne peut pas être rechargée.");
        }
    }
}
