package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

/**
 * Immortel : des vies en plus, mais -1 dégât. Si l'arme n'inflige que 1 dégât de base (donc rien à retirer),
 * le malus devient : moins rapide, tire moins vite, recharge moins vite et moins de munitions.
 */
public class ImmortalArchetypeEffect implements ArchetypeEffect {

    private final int lifeModifier;
    private final StatModifier modifier;

    public ImmortalArchetypeEffect(int lifeModifier, double slowSpeedPenalty, double slowFireFactor,
                                   double slowReloadFactor, double slowAmmoFactor) {
        this.lifeModifier = lifeModifier;
        this.modifier = stats -> {
            if (stats.getLivesPerHit() <= 1) {
                stats.setMovementSpeedModifier(stats.getMovementSpeedModifier() - slowSpeedPenalty);
                stats.setShotCooldownTicks(Math.max(1L, Math.round(stats.getShotCooldownTicks() * slowFireFactor)));
                stats.setReloadCooldownTicks(Math.max(1L, Math.round(stats.getReloadCooldownTicks() * slowReloadFactor)));
                stats.setMaxAmmo(Math.max(1, (int) Math.round(stats.getMaxAmmo() * slowAmmoFactor)));
            } else {
                stats.setLivesPerHit(stats.getLivesPerHit() - 1);
            }
        };
    }

    @Override
    public void onEquip(GamePlayer owner) {
        owner.setArchetypeLives(lifeModifier, null);
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
