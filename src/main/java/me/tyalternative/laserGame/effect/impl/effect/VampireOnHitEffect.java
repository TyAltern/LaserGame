package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;

public class VampireOnHitEffect implements PendingEffect {

    private static final int AMMO_RESTORED = 2;
    private static final int MAX_OVERFLOW = 2;

    @Override
    public String getId() {
        return "vampire_on_hit";
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        ctx.shooter.getWeapon().addAmmo(AMMO_RESTORED, MAX_OVERFLOW);
        return false;
    }
}
