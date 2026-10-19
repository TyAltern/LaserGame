package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

/** Pas de Velours : +X % de durée de la jauge d'accroupissement. */
public class CrouchTimeUpgradeEffect implements PermanentUpgradeEffect {

    private final double percent;

    public CrouchTimeUpgradeEffect(double percent) {
        this.percent = percent;
    }

    @Override
    public void onPurchase(GamePlayer gp) {
        gp.raiseSneakChargeBonus(percent);
    }
}
