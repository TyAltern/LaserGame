package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

/** Sans Faute : point(s) de victoire en plus quand tu gagnes un round sans avoir perdu de vie. */
public class FlawlessBonusUpgradeEffect implements PermanentUpgradeEffect {

    private final int points;

    public FlawlessBonusUpgradeEffect(int points) {
        this.points = points;
    }

    @Override
    public void onPurchase(GamePlayer gp) {
        gp.addFlawlessBonusWins(points);
    }
}
