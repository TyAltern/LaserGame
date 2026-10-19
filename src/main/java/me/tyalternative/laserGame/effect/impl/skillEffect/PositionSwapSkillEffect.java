package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConditionalSkillEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Chassé-Croisé : échange ta position avec un joueur aléatoire encore en vie. */
public class PositionSwapSkillEffect implements ConditionalSkillEffect {

    private List<Player> candidates(ActivationContext ctx) {
        List<Player> result = new ArrayList<>();
        for (GamePlayer other : ctx.match.getPlayers()) {
            if (other.getUuid().equals(ctx.gp.getUuid()) || other.isSpectator()) continue;
            Player otherPlayer = other.getPlayer();
            if (otherPlayer == null || !otherPlayer.getWorld().equals(ctx.player.getWorld())) continue;
            result.add(otherPlayer);
        }
        return result;
    }

    @Override
    public boolean canActivate(ActivationContext ctx) {
        if (candidates(ctx).isEmpty()) {
            Lang.send(ctx.player, "effect.skill.position_swap.unavailable");
            return false;
        }
        return true;
    }

    @Override
    public void activate(ActivationContext ctx) {
        List<Player> candidates = candidates(ctx);
        if (candidates.isEmpty()) return;

        Player other = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        Location mine = ctx.player.getLocation();
        Location theirs = other.getLocation();
        ctx.player.teleport(theirs);
        other.teleport(mine);

        Lang.send(ctx.player, "effect.skill.position_swap.activated", "player", other.getName());
        Lang.send(other, "effect.skill.position_swap.swapped", "player", ctx.player.getName());
    }
}
