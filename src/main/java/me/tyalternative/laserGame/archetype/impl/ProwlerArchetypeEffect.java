package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

/** Rôdeur : reste accroupi plus longtemps, mais attend plus longtemps entre deux tirs. */
public class ProwlerArchetypeEffect implements ArchetypeEffect {

    private final double crouchTimeBonus;
    private final StatModifier modifier;

    public ProwlerArchetypeEffect(double crouchTimeBonus, double fireDelayPenalty) {
        this.crouchTimeBonus = crouchTimeBonus;
        this.modifier = stats -> stats.setShotCooldownTicks(
                Math.max(1L, Math.round(stats.getShotCooldownTicks() * (1.0 + fireDelayPenalty))));
    }

    @Override
    public void onEquip(GamePlayer owner) {
        owner.setArchetypeSneakChargeBonus(crouchTimeBonus);
    }

    @Override
    public void onUnequip(GamePlayer owner) {
        owner.setArchetypeSneakChargeBonus(0.0);
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
