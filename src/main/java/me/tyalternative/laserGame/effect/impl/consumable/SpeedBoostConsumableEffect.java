package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

public class SpeedBoostConsumableEffect implements ConsumableEffect {

    private static final double AMOUNT = 0.10;
    private static final long DURATION_TICKS = 1200; // 60s

    @Override
    public void activate(ActivationContext ctx) {
        AttributeInstance speedAttr = ctx.player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        NamespacedKey key = new NamespacedKey(ctx.plugin, "consumable_speed_boost");
        speedAttr.removeModifier(key);
        speedAttr.addModifier(new AttributeModifier(key, AMOUNT, AttributeModifier.Operation.MULTIPLY_SCALAR_1));

        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (ctx.player.isOnline()) return;
            AttributeInstance attr = ctx.player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (attr != null) attr.removeModifier(key);
        }, DURATION_TICKS);

        ctx.player.sendMessage("§a+10% de vitesse pendant 1 minute !");
    }
}
