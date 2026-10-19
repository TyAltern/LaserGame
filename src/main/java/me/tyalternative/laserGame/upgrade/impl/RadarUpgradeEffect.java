package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

/**
 * Jumelles / Oeil Omniscient : le radar s'active plus tôt pour le porteur.
 * {@code reduction-seconds} secondes plus tôt, ou dès le début du round avec {@code from-start: true}.
 */
public class RadarUpgradeEffect implements PermanentUpgradeEffect {

    private final long reductionTicks;

    public RadarUpgradeEffect(long reductionSeconds, boolean fromStart) {
        this.reductionTicks = fromStart ? Long.MAX_VALUE / 4 : reductionSeconds * 20L;
    }

    @Override
    public void onPurchase(GamePlayer gp) {
        gp.raiseRadarDelayReduction(reductionTicks);
    }
}
