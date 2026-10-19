package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

import java.util.Optional;

/** Première Frappe : le premier tir qui touche, à chaque round, inflige des dégâts supplémentaires. */
public class FirstHitBonusUpgradeEffect implements PermanentUpgradeEffect {

    private final int bonus;

    public FirstHitBonusUpgradeEffect(int bonus) {
        this.bonus = bonus;
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new PendingEffect() {
            private boolean used = false;

            @Override
            public String getId() {
                return "first_hit_bonus";
            }

            @Override
            public boolean onRoundStart() {
                used = false;
                return false;
            }

            @Override
            public boolean onShotHit(HitResolutionContext ctx) {
                if (!used) {
                    used = true;
                    ctx.livesToRemove += bonus;
                }
                return false;
            }
        });
    }
}
