package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.Match;
import org.bukkit.Bukkit;

public class TimedReflectEffect implements PendingEffect {

    private final Match match;
    private final long endTick;
    private final int damage;

    public TimedReflectEffect(Match match, long endTick, int damage) {
        this.match = match;
        this.endTick = endTick;
        this.damage = damage;
    }

    @Override
    public String getId() {
        return "timed_reflect";
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        if (Bukkit.getCurrentTick() >= endTick) return true;
        if (ctx.livesToRemove > 0 && ctx.shooter != null && match.getCurrentRound() != null) {
            match.getCurrentRound().applyExtraDamage(ctx.shooter, damage);
        }
        return false;
    }
}
