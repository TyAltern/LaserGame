package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.game.GamePlayer;

/**
 * Pay to Win : le rechargement est instantané mais coûte de l'argent, proportionnellement aux munitions
 * manquantes par rapport au maximum (un rechargement complet coûte {@code full-reload-cost}). Voir LaserWeapon.
 */
public class PayToWinArchetypeEffect implements ArchetypeEffect {

    private final int fullReloadCost;

    public PayToWinArchetypeEffect(int fullReloadCost) {
        this.fullReloadCost = fullReloadCost;
    }

    @Override
    public void onEquip(GamePlayer owner) {
        owner.setReloadPaymentFullCost(fullReloadCost);
    }

    @Override
    public void onUnequip(GamePlayer owner) {
        owner.setReloadPaymentFullCost(0);
    }
}
