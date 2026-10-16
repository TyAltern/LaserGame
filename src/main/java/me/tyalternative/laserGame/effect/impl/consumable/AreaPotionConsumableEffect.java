package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Applique un potion effect à tous les joueurs adverses dans un rayon donné (CC : freeze, nausée, ralentissement...). */
public class AreaPotionConsumableEffect implements ConsumableEffect {

    private final PotionEffectType potionType;
    private final int amplifier;
    private final double radius;
    private final long durationTicks;
    private final String messageKey;

    public AreaPotionConsumableEffect(PotionEffectType potionType, int amplifier, double radius, long durationTicks, String messageKey) {
        this.potionType = potionType;
        this.amplifier = amplifier;
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.messageKey = messageKey;
    }

    @Override
    public boolean activate(ActivationContext ctx) {
        double distSqr = radius * radius;
        int affected = 0;
        for (GamePlayer other : ctx.match.getPlayers()) {
            if (other.getUuid().equals(ctx.gp.getUuid())) continue;
            if (other.isSpectator()) continue;

            Player otherPlayer = other.getPlayer();
            if (otherPlayer == null) continue;
            if (!otherPlayer.getWorld().equals(ctx.player.getWorld())) continue;
            if (otherPlayer.getLocation().distanceSquared(ctx.player.getLocation()) > distSqr) continue;

            otherPlayer.addPotionEffect(new PotionEffect(potionType, (int) durationTicks, amplifier, false, false));
            affected++;
        }
        Lang.send(ctx.player, messageKey, "count", affected, "duration", Lang.duration(durationTicks / 20));
        return true;
    }
}
