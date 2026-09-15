package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.Match;

public class ReflectNextHitEffect implements PendingEffect {

    private final Match match;

    public ReflectNextHitEffect(Match match) {
        this.match = match;
    }

    @Override
    public String getId() {
        return "reflect_next_hit";
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        if (ctx.livesToRemove > 0 && match.getCurrentRound() != null) {
            match.getCurrentRound().applyExtraDamage(ctx.shooter, 1);
        }
        return true;
    }
}
