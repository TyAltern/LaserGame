package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

/** Grossiste : un même consommable peut sortir plusieurs fois dans un tirage et être acheté en double. */
public class DuplicateConsumablesUpgradeEffect implements PermanentUpgradeEffect {

    @Override
    public void onPurchase(GamePlayer gp) {
        gp.setAllowDuplicateConsumables(true);
    }
}
