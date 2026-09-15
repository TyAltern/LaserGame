package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.RoundWinStreakBonusEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Round gagné sans perdre de vie -> boost stackable (vitesse/munitions/cooldown) pour le round suivant, reset au premier coup reçu. */
public class RoundWinStreakBonusWeaponAbility implements WeaponAbility {

    @Override
    public String getId() {
        return "round_win_streak_bonus";
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new RoundWinStreakBonusEffect(owner));
    }
}
