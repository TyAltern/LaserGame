package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;

public class NextShotBonusDamageEffect implements PendingEffect {

    @Override
    public String getId() {
        return "next_shot_bonus_damage";
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        ctx.livesToRemove += 1;
        return true;
    }
}
