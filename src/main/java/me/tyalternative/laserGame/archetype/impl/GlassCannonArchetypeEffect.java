package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

/** Glass Canon : une seule vie, mais des dégâts et une vitesse accrus. */
public class GlassCannonArchetypeEffect implements ArchetypeEffect {

    private final int fixedLives;
    private final int bonusDamage;
    private final StatModifier modifier;

    public GlassCannonArchetypeEffect(int fixedLives, int bonusDamage, double speedBonus) {
        this.fixedLives = fixedLives;
        this.bonusDamage = bonusDamage;
        this.modifier = stats -> stats.setMovementSpeedModifier(stats.getMovementSpeedModifier() + speedBonus);
    }

    @Override
    public void onEquip(GamePlayer owner) {
        owner.setArchetypeLives(0, fixedLives);
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect() {
        return Optional.of(new PendingEffect() {
            @Override
            public String getId() {
                return "glass_cannon";
            }

            @Override
            public boolean onShotHit(HitResolutionContext ctx) {
                ctx.livesToRemove += bonusDamage;
                return false;
            }
        });
    }
}
