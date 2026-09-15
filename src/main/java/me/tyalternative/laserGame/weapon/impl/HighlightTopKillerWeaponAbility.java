package me.tyalternative.laserGame.weapon.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.HighlightTopKillerEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.WeaponAbility;

import java.util.Optional;

/** Le joueur avec le plus de kills apparaît en surbrillance. */
public class HighlightTopKillerWeaponAbility implements WeaponAbility {

    @Override
    public String getId() {
        return "highlight_top_killer";
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new HighlightTopKillerEffect(owner));
    }
}
