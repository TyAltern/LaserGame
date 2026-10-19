package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

/**
 * Argent supplémentaire. kind :
 * <ul>
 *   <li>elimination : à chaque élimination ;</li>
 *   <li>hit : à chaque touche (le jeu appelle "kill" une touche) ;</li>
 *   <li>first_blood : sur le premier sang du round.</li>
 * </ul>
 */
public class CombatMoneyUpgradeEffect implements PermanentUpgradeEffect {

    private final String kind;
    private final int amount;

    public CombatMoneyUpgradeEffect(String kind, int amount) {
        this.kind = kind.toLowerCase();
        this.amount = amount;
    }

    @Override
    public void onPurchase(GamePlayer gp) {
        switch (kind) {
            case "elimination" -> gp.addEliminationRewardBonus(amount);
            case "first_blood" -> gp.addFirstBloodRewardBonus(amount);
            default -> gp.addHitRewardBonus(amount);
        }
    }
}
