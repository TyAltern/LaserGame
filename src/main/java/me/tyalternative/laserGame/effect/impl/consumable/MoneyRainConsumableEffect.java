package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.MoneyPerHitEffect;
import me.tyalternative.laserGame.lang.Lang;

/** Pluie d'Argent : de l'argent à chaque touche jusqu'à la fin du round. */
public class MoneyRainConsumableEffect implements ConsumableEffect {

    private final int amount;

    public MoneyRainConsumableEffect(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new MoneyPerHitEffect(ctx.gp, amount));
        Lang.send(ctx.player, "effect.consumable.money_rain.activated", "amount", amount);
        return true;
    }
}
