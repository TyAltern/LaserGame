package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.RoundAccess;

import java.util.Optional;

/** Survivant : récupère des vies à chaque élimination, mais démarre avec moins de vies. */
public class SurvivorArchetypeEffect implements ArchetypeEffect {

    private final int heal;
    private final int lifeModifier;

    public SurvivorArchetypeEffect(int heal, int lifeModifier) {
        this.heal = heal;
        this.lifeModifier = lifeModifier;
    }

    @Override
    public void onEquip(GamePlayer owner) {
        owner.setArchetypeLives(lifeModifier, null);
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new PendingEffect() {
            @Override
            public String getId() {
                return "survivor";
            }

            @Override
            public boolean onEliminationMade(GamePlayer victim) {
                RoundAccess.of(owner).ifPresent(round -> round.healPlayer(owner, heal));
                return false;
            }
        });
    }
}
