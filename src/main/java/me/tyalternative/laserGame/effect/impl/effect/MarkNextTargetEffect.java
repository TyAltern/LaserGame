package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MarkNextTargetEffect implements PendingEffect {

    private final int durationTicks;

    public MarkNextTargetEffect(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    @Override
    public String getId() {
        return "mark_next_target";
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        Player targetPlayer = ctx.target.getPlayer();
        if (targetPlayer != null && ctx.target.isDetectionImmune()) {
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, durationTicks, 0, false, false));
        }
        return true;
    }
}
