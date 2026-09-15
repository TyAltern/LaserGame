package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * Lance une bombe fumigène qui atterrit devant le joueur : applique blindness aux joueurs à 5 blocs du
 * centre, retiré s'ils s'éloignent, pendant 20 secondes.
 */
public class SmokeBombConsumableEffect implements ConsumableEffect {

    private static final double THROW_RANGE = 20.0;
    private static final double RADIUS = 5.0;
    private static final long DURATION_TICKS = 400; // 20s
    private static final long CHECK_INTERVAL_TICKS = 10;

    @Override
    public boolean activate(ActivationContext ctx) {
        Location eye = ctx.player.getEyeLocation();
        Vector direction = eye.getDirection();

        RayTraceResult result = ctx.player.getWorld().rayTraceBlocks(eye, direction, THROW_RANGE, FluidCollisionMode.NEVER, true);
        Location center = result != null
                ? result.getHitPosition().toLocation(ctx.player.getWorld())
                : eye.clone().add(direction.multiply(THROW_RANGE));

        ctx.player.sendMessage("§7Bombe fumigène lancée.");

        double radiusSqr = RADIUS * RADIUS;
        long totalTicks = DURATION_TICKS;

        final long[] elapsed = {0};
        final org.bukkit.scheduler.BukkitTask[] taskHolder = new org.bukkit.scheduler.BukkitTask[1];
        taskHolder[0] = Bukkit.getScheduler().runTaskTimer(ctx.plugin, () -> {
            elapsed[0] += CHECK_INTERVAL_TICKS;
            if (elapsed[0] > totalTicks) {
                taskHolder[0].cancel();
                return;
            }
            for (GamePlayer other : ctx.match.getPlayers()) {
                if (other.isSpectator()) continue;
                Player otherPlayer = other.getPlayer();
                if (otherPlayer == null) continue;
                if (!otherPlayer.getWorld().equals(center.getWorld())) continue;

                if (otherPlayer.getLocation().distanceSquared(center) <= radiusSqr) {
                    otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (CHECK_INTERVAL_TICKS + 5), 0, false, false));
                }
            }
        }, 0L, CHECK_INTERVAL_TICKS);
        return true;
    }
}
