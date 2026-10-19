package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.economy.CurrencySource;
import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.lang.Lang;

/** Ticket de Loto : gagne de l'argent. */
public class LottoTicketConsumableEffect implements ConsumableEffect {

    private final int gain;

    public LottoTicketConsumableEffect(int gain) {
        this.gain = gain;
    }

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.addCurrency(gain, CurrencySource.REFOUND);
        Lang.send(ctx.player, "effect.consumable.lotto_ticket.activated", "gain", gain);
        return true;
    }
}
