package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.game.Round;

/** Simule une élimination (visuel uniquement) : spectateur temporaire puis retour en jeu sans perte de vie. */
public class FakeDeathConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        Round round = ctx.match.getCurrentRound();
        if (round == null || ctx.gp.isSpectator()) {
            ctx.player.sendMessage("§cImpossible d'utiliser ça maintenant.");
            return false;
        }
        round.simulateElimination(ctx.gp);
        return true;
    }
}
