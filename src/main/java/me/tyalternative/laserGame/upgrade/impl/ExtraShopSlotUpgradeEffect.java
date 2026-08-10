package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

public class ExtraShopSlotUpgradeEffect implements PermanentUpgradeEffect {

    @Override
    public void onPurchase(GamePlayer gp) {
        gp.addBonusShopSlot(1);
    }
}
