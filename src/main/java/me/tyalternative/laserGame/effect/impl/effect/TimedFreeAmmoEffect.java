package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.ShotFiredContext;
import org.bukkit.Bukkit;

public class TimedFreeAmmoEffect implements PendingEffect {

    private final long endTick;

    public TimedFreeAmmoEffect(long endTick) {
        this.endTick = endTick;
    }

    @Override
    public String getId() {
        return "timed_free_ammo";
    }

    @Override
    public boolean onShotFired(ShotFiredContext ctx) {
        if (Bukkit.getCurrentTick() >= endTick) return true;
        ctx.consumesAmmo = false;
        return false;
    }
}
