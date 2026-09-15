package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;

public class ExecuteNextShotEffect implements PendingEffect {

    @Override
    public String getId() {
        return "execute_next_shot";
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        int remainingAfterHit = ctx.target.getLives() - ctx.livesToRemove;
        if (remainingAfterHit == 1) {
            ctx.livesToRemove = ctx.target.getLives();
        }
        return true;
    }
}
