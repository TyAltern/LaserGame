package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.RemoveInvisibilityOnShotEffect;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InvisibilityConsumableEffect implements ConsumableEffect {

    private static final long DURATION_TICKS = 100; // 5s

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) DURATION_TICKS, 0, false, false));
        ctx.gp.getEffects().add(new RemoveInvisibilityOnShotEffect());
        Lang.send(ctx.player, "effect.consumable.invisibility_cancel_on_shot.activated", "duration", Lang.duration(DURATION_TICKS / 20));
        return true;
    }
}
