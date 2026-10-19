package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.effect.impl.effect.TimedReflectEffect;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.Bukkit;

/** Miroir : pendant quelques secondes, chaque coup reçu inflige des dégâts au tireur. */
public class MirrorSkillEffect implements SkillEffect {

    private final long seconds;
    private final int damage;

    public MirrorSkillEffect(long seconds, int damage) {
        this.seconds = seconds;
        this.damage = damage;
    }

    @Override
    public void activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new TimedReflectEffect(ctx.match, Bukkit.getCurrentTick() + seconds * 20L, damage));
        Lang.send(ctx.player, "effect.skill.mirror.activated", "duration", Lang.duration(seconds));
    }
}
