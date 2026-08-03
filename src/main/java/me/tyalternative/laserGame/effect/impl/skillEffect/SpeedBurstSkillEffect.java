package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

public class SpeedBurstSkillEffect implements SkillEffect{

    private static final double AMOUNT = 0.60;
    private static final long DURATION_TICKS = 40; // 2s

    @Override
    public void activate(ActivationContext ctx) {
        AttributeInstance speedAttr = ctx.player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        NamespacedKey key = new NamespacedKey(ctx.plugin, "skill_speed_burst");
        speedAttr.removeModifier(key);
        speedAttr.addModifier(new AttributeModifier(key, AMOUNT, AttributeModifier.Operation.MULTIPLY_SCALAR_1));

        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (!ctx.player.isOnline()) return;
            AttributeInstance attr = ctx.player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (attr != null) {
                attr.removeModifier(key);
            }
        }, DURATION_TICKS);

        ctx.player.sendMessage("§bSprint explosif !");
    }
}
