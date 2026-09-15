package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Jump boost pour le reste du round. */
public class JumpBoostRoundConsumableEffect implements ConsumableEffect {

    private static final long DURATION_TICKS = 24000; // large marge (20 min), nettoyé de toute façon au round suivant

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, (int) DURATION_TICKS, 1, false, false));
        ctx.gp.addRoundScopedCleanup(() -> ctx.player.removePotionEffect(PotionEffectType.JUMP_BOOST));
        ctx.player.sendMessage("§7Jump boost activé pour le reste du round.");
        return true;
    }
}
