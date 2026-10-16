package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Round;
import me.tyalternative.laserGame.lang.Lang;

/**
 * Pari : tu ne gagnes plus d'argent pour chaque kill/élimination tant que ce pari est actif, mais si tu
 * remportes le round, tu gagnes le double de ce que tu aurais normalement gagné. Doit être utilisé avant
 * la première élimination du round.
 */
public class BetDoubleEarningsConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        Round round = ctx.match.getCurrentRound();
        if (round == null || round.hasFirstEliminationOccurred()) {
            Lang.send(ctx.player, "effect.consumable.bet_double_earnings.too-late");
            return false;
        }

        ctx.gp.placeBet(GamePlayer.BetType.DOUBLE_EARNINGS, 0);
        Lang.send(ctx.player, "effect.consumable.bet_double_earnings.activated");
        return true;
    }
}
