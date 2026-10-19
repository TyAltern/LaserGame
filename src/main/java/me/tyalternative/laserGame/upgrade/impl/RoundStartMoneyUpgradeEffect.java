package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.economy.CurrencySource;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

import java.util.Optional;

/**
 * Argent de Poche / Investisseur : argent donné à chaque début de round.
 * {@code amount} fixe, plus {@code percent} de l'argent possédé (plafonné à {@code cap}).
 */
public class RoundStartMoneyUpgradeEffect implements PermanentUpgradeEffect {

    private final int amount;
    private final double percent;
    private final int cap;

    public RoundStartMoneyUpgradeEffect(int amount, double percent, int cap) {
        this.amount = amount;
        this.percent = percent;
        this.cap = cap;
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new PendingEffect() {
            @Override
            public String getId() {
                return "round_start_money";
            }

            @Override
            public boolean onRoundStart() {
                int interest = percent > 0 ? (int) Math.min(cap, Math.round(owner.getCurrency() * percent)) : 0;
                int total = amount + interest;
                if (total > 0) owner.addCurrency(total, CurrencySource.PASSIVE);
                return false;
            }
        });
    }
}
