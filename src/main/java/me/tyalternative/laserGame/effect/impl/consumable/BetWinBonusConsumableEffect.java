package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Round;
import me.tyalternative.laserGame.lang.Lang;

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
            Lang.send(ctx.player, "effect.consumable.bet_win_x4.too-late");
            return false;
        }

        ctx.gp.placeBet(GamePlayer.BetType.WIN_X4, ctx.price);
        ctx.match.broadcastToAll(Lang.get("effect.consumable.bet_win_x4.announce", "player", ctx.player.getName()));
        return true;
    }
}
