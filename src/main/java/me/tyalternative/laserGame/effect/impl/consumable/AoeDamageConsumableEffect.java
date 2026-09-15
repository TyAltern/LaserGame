package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.AoeNextShotEffect;

public class AoeDamageConsumableEffect implements ConsumableEffect {

    private static final double RADIUS = 3.0;
    private static final int SPLASH_LIVES = 1;

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new AoeNextShotEffect(ctx.match, RADIUS, SPLASH_LIVES));
        ctx.player.sendMessage("§7Ton prochain tir réussi infligera aussi 1 dégât aux joueurs proches de la cible.");
        return true;
    }
}
