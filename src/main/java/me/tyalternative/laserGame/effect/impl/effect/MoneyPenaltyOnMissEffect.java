package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;

public class MoneyPenaltyOnMissEffect implements PendingEffect {

    private static final int PENALTY = 2;

    private final GamePlayer owner;

    public MoneyPenaltyOnMissEffect(GamePlayer owner) {
        this.owner = owner;
    }

    @Override
    public String getId() {
        return "money_penalty_on_miss";
    }

    @Override
    public boolean onShotMissed() {
        owner.deductCurrency(PENALTY);
        return false;
    }
}
