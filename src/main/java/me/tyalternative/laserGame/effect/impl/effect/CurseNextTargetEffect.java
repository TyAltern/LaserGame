package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.List;

public class CurseNextTargetEffect implements PendingEffect {

    public enum Kind { TROLL_WEAPON, DOUBLE_COOLDOWN, AMMO_LOSS }

    private final Kind kind;

    public CurseNextTargetEffect(Kind kind) {
        this.kind = kind;
    }

    @Override
    public String getId() {
        return "curse_next_target_" + kind.name().toLowerCase();
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        switch (kind) {
            case TROLL_WEAPON -> ctx.target.getEffects().add(new UntilNextActionStatCurseEffect(
                    "troll_weapon_curse", ctx.target, List.of(
                    (StatModifier) stats -> stats.setRange(Math.max(1.0, stats.getRange() * 0.3)),
                    (StatModifier) stats -> stats.setShotCooldownTicks(Math.max(1, stats.getShotCooldownTicks() * 4))
            )));
            case DOUBLE_COOLDOWN -> ctx.target.getEffects().add(new UntilNextActionStatCurseEffect(
                    "double_cooldown_curse", ctx.target, List.of(
                    (StatModifier) stats -> stats.setShotCooldownTicks(Math.max(1, stats.getShotCooldownTicks() * 2))
            )));
            case AMMO_LOSS -> ctx.target.addRoundStatModifier(
                    stats -> stats.setMaxAmmo(Math.max(1, (int) Math.floor(stats.getMaxAmmo() * 0.6))));
        }
        return true;
    }
}
