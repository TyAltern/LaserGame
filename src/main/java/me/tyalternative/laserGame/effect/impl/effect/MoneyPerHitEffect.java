package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.economy.CurrencySource;
import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;

/** Argent supplémentaire à chaque touche, jusqu'à la fin du round (effets non permanents effacés entre rounds). */
public class MoneyPerHitEffect implements PendingEffect {

    private final GamePlayer owner;
    private final int amount;

    public MoneyPerHitEffect(GamePlayer owner, int amount) {
        this.owner = owner;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return "money_per_hit";
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        owner.addCurrency(amount, CurrencySource.SHOT_HIT);
        return false;
    }
}
