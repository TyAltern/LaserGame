package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.RemoveInvisibilityOnShotEffect;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InvisibilityConsumableEffect implements ConsumableEffect {

    private static final long DURATION_TICKS = 100; // 5s

    @Override
    public void activate(ActivationContext ctx) {
        ctx.player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) DURATION_TICKS, 0, false, false));
        ctx.gp.getEffects().add(new RemoveInvisibilityOnShotEffect());
        ctx.player.sendMessage("§7Invisible pendant 5 secondes (annulé si tu tires).");
    }
}
