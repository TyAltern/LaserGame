package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

/**
 * Outsider : +1 dégât si tu es (à égalité) le joueur avec le moins de vies, sinon -10 % de vitesse.
 * La comparaison se fait entre les joueurs encore en vie.
 */
public class OutsiderArchetypeEffect implements ArchetypeEffect {

    private final int bonusDamage;
    private final double speedPenalty;

    public OutsiderArchetypeEffect(int bonusDamage, double speedPenalty) {
        this.bonusDamage = bonusDamage;
        this.speedPenalty = speedPenalty;
    }

    static boolean hasFewestLives(GamePlayer owner) {
        for (GamePlayer other : owner.getAlivePlayers()) {
            if (other == owner || other.isSpectator()) continue;
            if (other.getLives() < owner.getLives()) return false;
        }
        return true;
    }

    @Override
    public Optional<StatModifier> getStatModifier(GamePlayer owner) {
        return Optional.of(stats -> {
            if (!hasFewestLives(owner)) {
                stats.setMovementSpeedModifier(stats.getMovementSpeedModifier() - speedPenalty);
            }
        });
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new PendingEffect() {
            @Override
            public String getId() {
                return "outsider";
            }

            @Override
            public boolean onShotHit(HitResolutionContext ctx) {
                if (hasFewestLives(owner)) ctx.livesToRemove += bonusDamage;
                return false;
            }

            @Override
            public boolean onLivesChanged() {
                owner.recalculateStats();
                owner.refreshMovementSpeed();
                return false;
            }
        });
    }
}
