package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;

public class IgnoreLongRangeHitEffect implements PendingEffect {

    private final double minDistance;

    public IgnoreLongRangeHitEffect(double minDistance) {
        this.minDistance = minDistance;
    }

    @Override
    public String getId() {
        return "ignore_long_range_hit";
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        if (ctx.distance > minDistance) {
            ctx.livesToRemove = 0;
        }
        return true;
    }
}
