package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

/** Client Satisfait : réduction sur les items du shop (pas sur le coût du reroll). */
public class ShopDiscountUpgradeEffect implements PermanentUpgradeEffect {

    private final double percent;

    public ShopDiscountUpgradeEffect(double percent) {
        this.percent = percent;
    }

    @Override
    public void onPurchase(GamePlayer gp) {
        gp.raiseShopDiscount(percent);
    }
}
