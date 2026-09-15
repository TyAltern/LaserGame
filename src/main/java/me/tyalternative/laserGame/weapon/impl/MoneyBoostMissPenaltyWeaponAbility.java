package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.MoneyPenaltyOnMissEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Augmente les gains d'argent au combat, mais chaque tir manqué en coûte un peu (plafonné à zéro). */
public class MoneyBoostMissPenaltyWeaponAbility implements WeaponAbility {

    private static final double MULTIPLIER = 1.5;

    private final StatModifier modifier = stats -> stats.setCombatCurrencyMultiplier(MULTIPLIER);

    @Override
    public String getId() {
        return "money_boost_miss_penalty";
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new MoneyPenaltyOnMissEffect(owner));
    }
}
