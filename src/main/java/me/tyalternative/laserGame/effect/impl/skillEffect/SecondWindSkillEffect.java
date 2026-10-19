package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.lang.Lang;

/** Second Vent : remplit entièrement la jauge d'accroupissement. */
public class SecondWindSkillEffect implements SkillEffect {

    @Override
    public void activate(ActivationContext ctx) {
        ctx.gp.setSneakChargeTicks(ctx.gp.getMaxSneakChargeTicks());
        Lang.send(ctx.player, "effect.skill.second_wind.activated");
    }
}
