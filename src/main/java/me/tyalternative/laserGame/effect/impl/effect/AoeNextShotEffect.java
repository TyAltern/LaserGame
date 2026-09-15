package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import org.bukkit.entity.Player;

public class AoeNextShotEffect implements PendingEffect {

    private final Match match;
    private final double radius;
    private final int splashLivesToRemove;

    public AoeNextShotEffect(Match match, double radius, int splashLivesToRemove) {
        this.match = match;
        this.radius = radius;
        this.splashLivesToRemove = splashLivesToRemove;
    }

    @Override
    public String getId() {
        return "aoe_next_shot";
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        Player targetPlayer = ctx.target.getPlayer();
        if (targetPlayer == null || match.getCurrentRound() == null) return true;

        double distSqr = radius * radius;
        for (GamePlayer other : match.getPlayers()) {
            if (other.getUuid().equals(ctx.target.getUuid())) continue;
            if (other.getUuid().equals(ctx.shooter.getUuid())) continue;
            if (other.isSpectator()) continue;

            Player otherPlayer = other.getPlayer();
            if (otherPlayer == null) continue;
            if (!otherPlayer.getWorld().equals(targetPlayer.getWorld())) continue;
            if (otherPlayer.getLocation().distanceSquared(targetPlayer.getLocation()) > distSqr) continue;

            match.getCurrentRound().applyExtraDamage(other, splashLivesToRemove);
        }
        return true;
    }
}
