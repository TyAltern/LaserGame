package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

/** Boost d'une statistique d'arme pour le reste du round (munitions max, portée, ou cooldown de tir). */
public class RoundWeaponBoostConsumableEffect implements ConsumableEffect {

    public enum Kind { MAX_AMMO, RANGE, COOLDOWN }

    private final Kind kind;
    private final double percent; // positif = boost, négatif = réduction (ex : -0.20 pour -20% cooldown)
    private final String message;

    public RoundWeaponBoostConsumableEffect(Kind kind, double percent, String message) {
        this.kind = kind;
        this.percent = percent;
        this.message = message;
    }

    @Override
    public boolean activate(ActivationContext ctx) {
        StatModifier modifier = switch (kind) {
            case MAX_AMMO -> stats -> stats.setMaxAmmo(Math.max(1, (int) Math.floor(stats.getMaxAmmo() * (1 + percent))));
            case RANGE -> stats -> stats.setRange(Math.max(1.0, stats.getRange() * (1 + percent)));
            case COOLDOWN -> stats -> stats.setShotCooldownTicks(Math.max(1L, (long) Math.floor(stats.getShotCooldownTicks() * (1 + percent))));
        };
        ctx.gp.addRoundStatModifier(modifier);
        ctx.player.sendMessage(message);
        return true;
    }
}
