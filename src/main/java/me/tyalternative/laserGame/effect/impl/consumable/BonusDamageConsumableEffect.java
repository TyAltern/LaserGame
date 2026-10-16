package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.effect.impl.effect.NextShotBonusDamageEffect;
import me.tyalternative.laserGame.lang.Lang;

public class BonusDamageConsumableEffect implements ConsumableEffect {
    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new NextShotBonusDamageEffect());
        Lang.send(ctx.player, "effect.consumable.bonus_damage_next_shot.activated");
        return true;
    }
}
