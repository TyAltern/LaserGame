package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;

public class BerserkerEffect implements PendingEffect {
    @Override
    public String getId() {
        return "berserker";
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        ctx.livesToRemove += 1;
        return false;
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        ctx.livesToRemove += 1;
        return false;
    }
}
