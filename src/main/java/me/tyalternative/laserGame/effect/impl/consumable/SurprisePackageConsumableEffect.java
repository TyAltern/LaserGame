package me.tyalternative.laserGame.effect.impl.consumable;

import me.tyalternative.laserGame.LaserGame;
import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.lang.Lang;
import me.tyalternative.laserGame.shop.ConsumableDefinition;
import me.tyalternative.laserGame.shop.DuplicatePolicy;
import me.tyalternative.laserGame.shop.Rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Colis Surprise : donne des consommables aléatoires de rareté Commun ou Unique, dans la limite des slots libres.
 * Les cadeaux sont distribués une fois le colis retiré de son slot (qui peut donc être réutilisé).
 */
public class SurprisePackageConsumableEffect implements ConsumableEffect {

    private static final String OWN_EFFECT_ID = "surprise_package";

    private final int count;

    public SurprisePackageConsumableEffect(int count) {
        this.count = count;
    }

    @Override
    public boolean activate(ActivationContext ctx) {
        return true;
    }

    @Override
    public void afterConsumed(ActivationContext ctx) {
        List<ConsumableDefinition> pool = new ArrayList<>();
        for (ConsumableDefinition def : LaserGame.getInstance().getConsumableManager().getAllDefinitions()) {
            if (OWN_EFFECT_ID.equals(def.effectId())) continue;
            if (def.rarity() == Rarity.COMMON || def.rarity() == Rarity.UNIQUE) pool.add(def);
        }

        int given = 0;
        if (!pool.isEmpty()) {
            for (int i = 0; i < count; i++) {
                ConsumableDefinition gift = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
                if (!ctx.gp.getConsumables().add(gift.id(), DuplicatePolicy.SEPARATE_SLOT)) break; // plus de place
                given++;
            }
        }

        if (given > 0) {
            Lang.send(ctx.player, "effect.consumable.surprise_package.activated", "count", given);
        } else {
            Lang.send(ctx.player, "effect.consumable.surprise_package.full");
        }
    }
}
