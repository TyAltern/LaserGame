package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;

/** Un tir manqué ne consomme pas de munitions (remboursée après coup, car on ne connaît le résultat qu'après le tir). */
public class RefundAmmoOnMissEffect implements PendingEffect {

    private final GamePlayer owner;

    public RefundAmmoOnMissEffect(GamePlayer owner) {
        this.owner = owner;
    }

    @Override
    public String getId() {
        return "refund_ammo_on_miss";
    }

    @Override
    public boolean onShotMissed() {
        owner.getWeapon().addAmmo(1, 0);
        return false;
    }
}
