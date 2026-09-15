package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;

public class ReloadOverflowConsumableEffect implements ConsumableEffect {

    private static final int BONUS_AMMO = 1;

    @Override
    public boolean activate(ActivationContext ctx) {
        ctx.gp.getWeapon().grantNextReloadBonus(BONUS_AMMO);
        ctx.player.sendMessage("§7Ton prochain rechargement restaurera " + BONUS_AMMO + " munition bonus au-delà du max.");
        return true;
    }
}
