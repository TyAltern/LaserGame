package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;

import java.util.Optional;

/** Paratonnerre : le premier coup reçu de chaque round est annulé, mais tu démarres avec moins de vies. */
public class LightningRodArchetypeEffect implements ArchetypeEffect {

    private final int lifeModifier;

    public LightningRodArchetypeEffect(int lifeModifier) {
        this.lifeModifier = lifeModifier;
    }

    @Override
    public void onEquip(GamePlayer owner) {
        owner.setArchetypeLives(lifeModifier, null);
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new PendingEffect() {
            private boolean used = false;

            @Override
            public String getId() {
                return "lightning_rod";
            }

            @Override
            public boolean onRoundStart() {
                used = false;
                return false;
            }

            @Override
            public boolean onDamageTaken(HitResolutionContext ctx) {
                if (!used && ctx.livesToRemove > 0) {
                    used = true;
                    ctx.livesToRemove = 0;
                    ctx.absorbed = true;
                }
                return false;
            }
        });
    }
}
