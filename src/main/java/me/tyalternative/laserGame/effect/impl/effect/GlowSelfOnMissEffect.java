package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GlowSelfOnMissEffect implements PendingEffect {

    private final GamePlayer owner;

    public GlowSelfOnMissEffect(GamePlayer owner) {
        this.owner = owner;
    }

    @Override
    public String getId() {
        return "glow_self_on_miss";
    }

    @Override
    public boolean onShotMissed() {
        Player p = owner.getPlayer();
        if (p != null) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 0, false, false));
        }
        return false;
    }
}
