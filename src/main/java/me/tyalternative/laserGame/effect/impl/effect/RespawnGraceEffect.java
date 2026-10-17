package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import org.bukkit.Bukkit;

/** Si vous êtes touché moins de 15 secondes après avoir respawn, vous ne perdez pas de vie. */
public class RespawnGraceEffect implements PendingEffect {

    private static final long DEFAULT_GRACE_TICKS = 300; // 15s

    private final long graceTicks;
    private long graceEndTick = -1;

    public RespawnGraceEffect() {
        this(DEFAULT_GRACE_TICKS);
    }
    public RespawnGraceEffect(long graceTicks) {
        this.graceTicks = graceTicks;
    }

    @Override
    public String getId() {
        return "respawn_grace_period";
    }

    @Override
    public boolean onRespawn() {
        graceEndTick = Bukkit.getCurrentTick() + graceTicks;
        return false;
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        if (graceEndTick >= 0 && Bukkit.getCurrentTick() <= graceEndTick) {
            ctx.livesToRemove = 0;
            ctx.absorbed = true;
        }
        return false;
    }
}