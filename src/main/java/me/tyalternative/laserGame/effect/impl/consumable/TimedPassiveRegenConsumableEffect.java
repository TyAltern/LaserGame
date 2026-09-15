package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.weapon.StatModifier;
import org.bukkit.Bukkit;

/** Régénère 1 munition toutes les 5s sans recharge manuelle, pendant 1 minute. */
public class TimedPassiveRegenConsumableEffect implements ConsumableEffect {

    private static final int REGEN_AMOUNT = 1;
    private static final long REGEN_INTERVAL_TICKS = 100; // 5s
    private static final long DURATION_TICKS = 1200; // 60s

    @Override
    public boolean activate(ActivationContext ctx) {
        StatModifier modifier = stats -> {
            stats.setPassiveRegenAmount(REGEN_AMOUNT);
            stats.setPassiveRegenIntervalTicks(REGEN_INTERVAL_TICKS);
        };
        ctx.gp.addStatModifier(modifier);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> ctx.gp.removeStatModifier(modifier), DURATION_TICKS);
        ctx.player.sendMessage("§7Régénère " + REGEN_AMOUNT + " munition toutes les 5 secondes pendant 1 minute.");
        return true;
    }
}
