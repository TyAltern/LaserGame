package me.tyalternative.laserGame.effect.impl.effect;


import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.ShotFiredContext;
import org.bukkit.potion.PotionEffectType;

public class RemoveInvisibilityOnShotEffect implements PendingEffect {

    @Override
    public String getId() {
        return "remove_invisibility_on_shot";
    }

    @Override
    public boolean onShotFired(ShotFiredContext ctx) {
        if (ctx.shooter != null) {
            ctx.shooter.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
        return true;
    }
}
