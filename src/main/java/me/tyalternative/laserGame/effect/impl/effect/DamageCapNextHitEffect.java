package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;

public class DamageCapNextHitEffect implements PendingEffect {

    private final int cap;

    public DamageCapNextHitEffect(int cap) {
        this.cap = cap;
    }

    @Override
    public String getId() {
        return "damage_cap_next_hit";
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        ctx.livesToRemove = Math.min(ctx.livesToRemove, cap);
        return true;
    }
}
