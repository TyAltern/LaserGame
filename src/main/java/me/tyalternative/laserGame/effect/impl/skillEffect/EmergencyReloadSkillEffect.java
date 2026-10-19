package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConditionalSkillEffect;
import me.tyalternative.laserGame.lang.Lang;

/** Chargeur de Secours : rechargement instantané. */
public class EmergencyReloadSkillEffect implements ConditionalSkillEffect {

    @Override
    public boolean canActivate(ActivationContext ctx) {
        boolean needsReload = ctx.gp.getWeapon().getAmmo() < ctx.gp.getWeapon().getMaxAmmo();
        if (!needsReload || ctx.gp.getEffectiveStats().isReloadDisabled()) {
            Lang.send(ctx.player, "effect.skill.emergency_reload.unavailable");
            return false;
        }
        return true;
    }

    @Override
    public void activate(ActivationContext ctx) {
        if (ctx.gp.getWeapon().instantRefill()) {
            Lang.send(ctx.player, "effect.skill.emergency_reload.activated");
        }
    }
}
