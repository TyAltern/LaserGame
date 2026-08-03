package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;

public class IgnoreDamageWhileReloadingEffect implements PendingEffect {
    @Override
    public String getId() {
        return "ignore_damage_while_reloading";
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        if (ctx.target.getWeapon().isReloading()) {
            ctx.livesToRemove = 0;
        }
        return false;
    }
}
