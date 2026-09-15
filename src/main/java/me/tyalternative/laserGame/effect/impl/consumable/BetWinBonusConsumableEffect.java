package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Round;

/**
 * Pari : annonce publiquement l'utilisation. Si tu remportes le round, tu gagnes 4x le prix d'achat.
 * Si tu es éliminé après l'avoir utilisé, le joueur qui t'élimine gagne le prix d'achat.
 * Doit être utilisé avant la première élimination du round.
 */
public class BetWinBonusConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        Round round = ctx.match.getCurrentRound();
        if (round == null || round.hasFirstEliminationOccurred()) {
            ctx.player.sendMessage("§cTrop tard : une élimination a déjà eu lieu ce round.");
            return false;
        }

        ctx.gp.placeBet(GamePlayer.BetType.WIN_X4, ctx.price);
        ctx.match.broadcastToAll("§6" + ctx.player.getName() + " a parié sur sa victoire !");
        return true;
    }
}
