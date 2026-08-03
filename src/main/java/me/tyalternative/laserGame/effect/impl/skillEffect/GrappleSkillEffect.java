package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import org.bukkit.util.Vector;

public class GrappleSkillEffect implements SkillEffect {

    private static final double POWER = 1.8;

    @Override
    public void activate(ActivationContext ctx) {
        Vector direction = ctx.player.getEyeLocation().getDirection().normalize();
        ctx.player.setVelocity(direction.multiply(POWER));
        ctx.player.sendMessage("§bGrappin !");
    }
}
