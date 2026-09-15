package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;

/** Si vous êtes le premier joueur éliminé du round, vous réapparaissez avec 1 coeur au lieu d'être éliminé. */
public class ReviveOnFirstEliminationEffect implements PendingEffect {

    private boolean used = false;

    @Override
    public String getId() {
        return "revive_on_first_elimination";
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        if (!used && ctx.wouldBeFirstElimination && ctx.livesToRemove >= ctx.target.getLives()) {
            ctx.livesToRemove = ctx.target.getLives() - 1;
            used = true;
        }
        return false;
    }

    @Override
    public boolean onRoundStart() {
        used = false;
        return false;
    }
}