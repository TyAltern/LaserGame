package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;

/** Les prochains tirs traversent les joueurs touchés au lieu de s'arrêter au premier (empilable). */
public class PiercingShotsConsumableEffect implements ConsumableEffect {

    private static final int SHOTS_GRANTED = 10;

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.grantPiercingShots(SHOTS_GRANTED);
        ctx.player.sendMessage("§7Tes " + SHOTS_GRANTED + " prochains tirs traverseront les joueurs touchés.");
        return true;
    }
}
