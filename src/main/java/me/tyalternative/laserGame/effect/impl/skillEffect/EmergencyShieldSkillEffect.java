package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.effect.impl.effect.DamageCapNextHitEffect;
import org.bukkit.Bukkit;

public class EmergencyShieldSkillEffect implements SkillEffect {

    private static final long DURATION_TICKS = 100; // 5s

    @Override
    public void activate(ActivationContext ctx) {
        DamageCapNextHitEffect effect = new DamageCapNextHitEffect(0);
        ctx.gp.getEffects().add(effect);
        ctx.player.sendMessage("§bBouclier d'Urgence activé : le prochain coup sera totalement absorbé pendant 5 secondes.");

        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            ctx.gp.getEffects().remove(effect);
            ctx.player.sendMessage("§bVotre Bouclier d'Urgence s'est désactivé, 5 secondes ont passé.");
        }, DURATION_TICKS);
    }
}
