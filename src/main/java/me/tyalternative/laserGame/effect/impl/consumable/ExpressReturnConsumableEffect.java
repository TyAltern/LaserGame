package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.game.Round;
import me.tyalternative.laserGame.lang.Lang;

/** Retour Express : te téléporte sur un point de spawn de l'arène. */
public class ExpressReturnConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        Round round = ctx.match.getCurrentRound();
        if (round == null) {
            Lang.send(ctx.player, "effect.consumable.express_return.unavailable");
            return false;
        }
        round.teleportToRandomSpawn(ctx.gp);
        Lang.send(ctx.player, "effect.consumable.express_return.activated");
        return true;
    }
}
