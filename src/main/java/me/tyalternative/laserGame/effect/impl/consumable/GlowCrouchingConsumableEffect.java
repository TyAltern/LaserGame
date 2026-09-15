package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Met en surbrillance tous les joueurs actuellement accroupis (peu importe la distance). */
public class GlowCrouchingConsumableEffect implements ConsumableEffect {

    private static final long DURATION_TICKS = 500; // 25s

    @Override
    public boolean activate(ActivationContext ctx) {
        int affected = 0;
        for (GamePlayer other : ctx.match.getPlayers()) {
            if (other.getUuid().equals(ctx.gp.getUuid())) continue;
            if (other.isSpectator()) continue;

            Player otherPlayer = other.getPlayer();
            if (otherPlayer == null || !otherPlayer.isSneaking()) continue;
            if (other.isDetectionImmune()) continue;

            otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) DURATION_TICKS, 0, false, false));
            affected++;
        }
        ctx.player.sendMessage("§7" + affected + " joueur(s) accroupi(s) mis en surbrillance pendant 25 secondes.");
        return true;
    }
}
