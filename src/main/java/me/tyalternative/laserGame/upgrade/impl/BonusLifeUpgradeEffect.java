package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

/** Vitalité : des vies supplémentaires à chaque début de round (appliquées aussi dès le prochain round). */
public class BonusLifeUpgradeEffect implements PermanentUpgradeEffect {

    private final int amount;

    public BonusLifeUpgradeEffect(int amount) {
        this.amount = amount;
    }

    @Override
    public void onPurchase(GamePlayer gp) {
        gp.addBonusStartingLives(amount);
    }
}
