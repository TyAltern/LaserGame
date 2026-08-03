package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;

public class StreakBonusDamageEffect implements PendingEffect {

    private static final int STREAK_REQUIRED = 2;
    private static final int BONUS_DAMAGE = 1;

    private int consecutiveHits = 0;

    @Override
    public String getId() {
        return "streak_bonus_damage";
    }

    @Override
    public boolean onShotMissed() {
        consecutiveHits = 0;
        return false;
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        consecutiveHits++;
        if (consecutiveHits >= STREAK_REQUIRED) {
            ctx.livesToRemove += BONUS_DAMAGE;
        }
        return false;
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        consecutiveHits = 0;
        return false;
    }
}
