package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.util.Vector;

/** Boîte à Ressort : propulse le joueur en l'air. */
public class SpringBoxSkillEffect implements SkillEffect {

    private final double power;

    public SpringBoxSkillEffect(double power) {
        this.power = power;
    }

    @Override
    public void activate(ActivationContext ctx) {
        Vector velocity = ctx.player.getVelocity();
        velocity.setY(power);
        ctx.player.setVelocity(velocity);
        Lang.send(ctx.player, "effect.skill.spring_box.activated");
    }
}
