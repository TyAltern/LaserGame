package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.effect.impl.effect.DamageCapNextHitEffect;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.Bukkit;

public class EmergencyShieldSkillEffect implements SkillEffect {

    private static final long DURATION_TICKS = 100; // 5s

    @Override
    public void activate(ActivationContext ctx) {
        DamageCapNextHitEffect effect = new DamageCapNextHitEffect(0);
        ctx.gp.getEffects().add(effect);
        Lang.send(ctx.player, "effect.skill.emergency_shield.activated", "duration", Lang.duration(DURATION_TICKS / 20));

        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            ctx.gp.getEffects().remove(effect);
            Lang.send(ctx.player, "effect.skill.emergency_shield.expired", "duration", Lang.duration(DURATION_TICKS / 20));
        }, DURATION_TICKS);
    }
}
