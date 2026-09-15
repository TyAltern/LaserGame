package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.ShotFiredContext;

public class FreeShotNextEffect implements PendingEffect {

    @Override
    public String getId() {
        return "free_shot_next";
    }

    @Override
    public boolean onShotFired(ShotFiredContext ctx) {
        ctx.consumesAmmo = false;
        return true;
    }
}
