package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.game.Round;
import me.tyalternative.laserGame.lang.Lang;

/** Pansement : rend des vies perdues, sans dépasser le maximum. Pas consommé si les vies sont déjà pleines. */
public class BandageConsumableEffect implements ConsumableEffect {

    private final int amount;

    public BandageConsumableEffect(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean activate(ActivationContext ctx) {
        Round round = ctx.match.getCurrentRound();
        int healed = round != null ? round.healPlayer(ctx.gp, amount) : ctx.gp.heal(amount);
        if (healed <= 0) {
            Lang.send(ctx.player, "effect.consumable.bandage.full");
            return false;
        }
        Lang.send(ctx.player, "effect.consumable.bandage.activated", "healed", healed);
        return true;
    }
}
