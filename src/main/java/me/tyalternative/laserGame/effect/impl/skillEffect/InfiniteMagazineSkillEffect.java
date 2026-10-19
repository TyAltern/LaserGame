package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.effect.impl.effect.TimedFreeAmmoEffect;
import me.tyalternative.laserGame.lang.Lang;
import me.tyalternative.laserGame.weapon.StatModifier;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/** Chargeur Infini : les tirs ne consomment pas de munition et la cadence de tir augmente pendant quelques secondes. */
public class InfiniteMagazineSkillEffect implements SkillEffect {

    private final long seconds;
    private final double fireRateBonus;

    public InfiniteMagazineSkillEffect(long seconds, double fireRateBonus) {
        this.seconds = seconds;
        this.fireRateBonus = fireRateBonus;
    }

    @Override
    public void activate(ActivationContext ctx) {
        long ticks = seconds * 20L;
        ctx.gp.getEffects().add(new TimedFreeAmmoEffect(Bukkit.getCurrentTick() + ticks));

        StatModifier fasterFire = stats ->
                stats.setShotCooldownTicks(Math.max(1L, Math.round(stats.getShotCooldownTicks() / (1.0 + fireRateBonus))));
        ctx.gp.addStatModifier(fasterFire);

        BukkitTask restore = Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> ctx.gp.removeStatModifier(fasterFire), ticks);
        ctx.gp.addRoundScopedCleanup(() -> {
            restore.cancel();
            ctx.gp.removeStatModifier(fasterFire);
        });

        Lang.send(ctx.player, "effect.skill.infinite_magazine.activated", "duration", Lang.duration(seconds));
    }
}
