package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

/**
 * Permet de se déplacer à vitesse normale en étant accroupi, pour le round.
 * Utilise l'attribut SNEAKING_SPEED (défaut ~0.3, on le ramène à ~1.0).
 */
public class SneakSpeedUnlockConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        AttributeInstance sneakAttr = ctx.player.getAttribute(Attribute.SNEAKING_SPEED);
        if (sneakAttr == null) {
            ctx.player.sendMessage("§cCet effet n'est pas disponible sur ce serveur.");
            return false;
        }

        NamespacedKey key = new NamespacedKey(ctx.plugin, "consumable_sneak_speed_unlock");
        sneakAttr.removeModifier(key);
        sneakAttr.addModifier(new AttributeModifier(key, 1.0 - sneakAttr.getBaseValue(), AttributeModifier.Operation.ADD_NUMBER));

        ctx.gp.addRoundScopedCleanup(() -> {
            AttributeInstance attr = ctx.player.getAttribute(Attribute.SNEAKING_SPEED);
            if (attr != null) attr.removeModifier(key);
        });

        ctx.player.sendMessage("§7Tu peux te déplacer à vitesse normale en étant accroupi pour ce round.");
        return true;
    }
}
