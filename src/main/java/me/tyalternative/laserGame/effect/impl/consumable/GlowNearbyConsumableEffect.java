package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GlowNearbyConsumableEffect implements ConsumableEffect {

    private static final double RADIUS = 50.0;
    private static final long DURATION_TICKS = 300; // 15s

    @Override
    public void activate(ActivationContext ctx) {
        int affected = 0;
        double distSqr = RADIUS*RADIUS;
        for (GamePlayer other : ctx.match.getPlayers()) {
            if (other.getUuid().equals(ctx.gp.getUuid())) continue;
            if (other.isSpectator()) continue;

            Player otherPlayer = other.getPlayer();
            if (otherPlayer == null) continue;
            if (!otherPlayer.getWorld().equals(ctx.player.getWorld())) continue;
            if (otherPlayer.getLocation().distanceSquared(ctx.player.getLocation()) > distSqr) continue;

            otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) DURATION_TICKS, 0, false, false));
            affected++;
        }
        ctx.player.sendMessage("§7" + affected + " joueur(s) mis en surbrillance pendant 15 secondes.");
    }
}
