package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Sonar : tous les adversaires encore en vie sont en surbrillance pendant quelques secondes. */
public class SonarSkillEffect implements SkillEffect {

    private final long seconds;

    public SonarSkillEffect(long seconds) {
        this.seconds = seconds;
    }

    @Override
    public void activate(ActivationContext ctx) {
        int affected = 0;
        for (GamePlayer other : ctx.match.getPlayers()) {
            if (other.getUuid().equals(ctx.gp.getUuid()) || other.isSpectator() || other.isDetectionImmune()) continue;
            Player otherPlayer = other.getPlayer();
            if (otherPlayer == null) continue;
            otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) (seconds * 20L), 0, false, false));
            affected++;
        }
        Lang.send(ctx.player, "effect.skill.sonar.activated", "count", affected, "duration", Lang.duration(seconds));
    }
}
