package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.lang.Lang;

/** Radar de Poche : active le radar tout de suite. Pas consommé s'il est déjà actif. */
public class PocketRadarConsumableEffect implements ConsumableEffect {

    @Override
    public boolean activate(ActivationContext ctx) {
        if (ctx.gp.canSeeRadar()) {
            Lang.send(ctx.player, "effect.consumable.pocket_radar.already");
            return false;
        }
        ctx.gp.setCanSeeRadar(true);
        Lang.send(ctx.player, "effect.consumable.pocket_radar.activated");
        return true;
    }
}
