package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

public class SpeedBoostConsumableEffect implements ConsumableEffect {

    private static final double AMOUNT = 0.10;
    private static final long DURATION_TICKS = 1200; // 60s

    @Override
    public boolean activate(ActivationContext ctx) {
        AttributeInstance speedAttr = ctx.player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr == null) return false;

        NamespacedKey key = new NamespacedKey(ctx.plugin, "consumable_speed_boost");
        speedAttr.removeModifier(key);
        speedAttr.addModifier(new AttributeModifier(key, AMOUNT, AttributeModifier.Operation.MULTIPLY_SCALAR_1));

        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (!ctx.player.isOnline()) return;
            AttributeInstance attr = ctx.player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (attr != null) attr.removeModifier(key);
        }, DURATION_TICKS);

        Lang.send(ctx.player, "effect.consumable.speed_boost.activated", "percent", (int) Math.round(AMOUNT * 100), "duration", Lang.duration(DURATION_TICKS / 20));
        return true;
    }
}
