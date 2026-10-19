package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

/**
 * Marathonien : gagne {@code per-hit} de vitesse par touche (le jeu appelle "kill" une touche),
 * jusqu'à {@code cap}. Remis à zéro dès que le joueur perd une vie.
 */
public class SpeedPerHitUpgradeEffect implements PermanentUpgradeEffect {

    private final double perHit;
    private final int maxStacks;

    public SpeedPerHitUpgradeEffect(double perHit, double cap) {
        this.perHit = perHit;
        this.maxStacks = perHit <= 0 ? 0 : (int) Math.round(cap / perHit);
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new PendingEffect() {
            private int stacks = 0;
            private StatModifier current;

            @Override
            public String getId() {
                return "speed_per_hit";
            }

            private void apply() {
                if (current != null) {
                    owner.removeStatModifier(current);
                    current = null;
                }
                if (stacks > 0) {
                    double bonus = stacks * perHit;
                    current = stats -> stats.setMovementSpeedModifier(stats.getMovementSpeedModifier() + bonus);
                    owner.addStatModifier(current);
                }
                owner.refreshMovementSpeed();
            }

            @Override
            public boolean onShotHit(HitResolutionContext ctx) {
                if (stacks < maxStacks) {
                    stacks++;
                    apply();
                }
                return false;
            }

            @Override
            public boolean onLifeLost() {
                if (stacks > 0) {
                    stacks = 0;
                    apply();
                }
                return false;
            }
        });
    }
}
