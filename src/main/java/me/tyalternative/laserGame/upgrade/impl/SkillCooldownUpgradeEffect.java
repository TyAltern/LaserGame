package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

/**
 * Sang-Froid / Maîtrise Absolue : réduction du cooldown de la compétence.
 * {@code reduction} est la valeur TOTALE du niveau (0.25 = -25 % au total) : on garde la meilleure possédée.
 */
public class SkillCooldownUpgradeEffect implements PermanentUpgradeEffect {

    private final double reduction;

    public SkillCooldownUpgradeEffect(double reduction) {
        this.reduction = reduction;
    }

    @Override
    public void onPurchase(GamePlayer gp) {
        gp.raiseSkillCooldownReduction(reduction);
    }
}
