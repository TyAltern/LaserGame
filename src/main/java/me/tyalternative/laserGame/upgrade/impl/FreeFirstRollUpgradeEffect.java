package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

/** Tournée Générale : après une victoire de round, le premier tirage de consommables est gratuit (pas les rerolls). */
public class FreeFirstRollUpgradeEffect implements PermanentUpgradeEffect {

    @Override
    public void onPurchase(GamePlayer gp) {
        gp.setFreeFirstRollAfterWin(true);
    }
}
