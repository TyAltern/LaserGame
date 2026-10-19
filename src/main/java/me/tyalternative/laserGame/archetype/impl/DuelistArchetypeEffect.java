package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.ShotFiredContext;
import me.tyalternative.laserGame.game.GamePlayer;

import java.util.Optional;

/**
 * Duelliste : le premier tir de chaque vie inflige des dégâts supplémentaires ; tous les autres sont plafonnés
 * (quelles que soient les améliorations ou les consommables).
 * */
public class DuelistArchetypeEffect implements ArchetypeEffect {

    private final int firstShotBonus;
    private final int otherShotsMaxDamage;
    private final boolean firstHitMode;

    public DuelistArchetypeEffect(int firstShotBonus, int otherShotsMaxDamage, boolean firstHitMode) {
        this.firstShotBonus = firstShotBonus;
        this.otherShotsMaxDamage = otherShotsMaxDamage;
        this.firstHitMode = firstHitMode;
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new PendingEffect() {
            private boolean armed = true;     // le prochain tir (ou la prochaine touche) est le premier de cette vie
            private boolean pending = false;  // le tir en cours est le premier
            private boolean currentHitIsFirst = false;

            @Override
            public String getId() {
                return "duelist";
            }

            @Override
            public boolean onRoundStart() {
                armed = true;
                pending = false;
                return false;
            }

            @Override
            public boolean onRespawn() {
                armed = true;
                pending = false;
                return false;
            }

            @Override
            public boolean onShotFired(ShotFiredContext ctx) {
                pending = false;
                if (!firstHitMode && armed) {
                    pending = true;
                    armed = false;
                }
                return false;
            }

            @Override
            public boolean onShotMissed() {
                pending = false;
                return false;
            }

            @Override
            public boolean onShotHit(HitResolutionContext ctx) {
                if (firstHitMode && armed) {
                    pending = true;
                    armed = false;
                }
                currentHitIsFirst = pending;
                pending = false;
                if (currentHitIsFirst) {
                    ctx.livesToRemove += firstShotBonus;
                }
                return false;
            }

            @Override
            public boolean onShotHitFinal(HitResolutionContext ctx) {
                if (!currentHitIsFirst) {
                    ctx.livesToRemove = Math.min(ctx.livesToRemove, otherShotsMaxDamage);
                }
                currentHitIsFirst = false;
                return false;
            }
        });
    }
}
