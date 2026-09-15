package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.ShotFiredContext;
import me.tyalternative.laserGame.game.GamePlayer;

import java.util.concurrent.ThreadLocalRandom;

public class LuckyMagazineEffect implements PendingEffect {

    private static final double EMPTY_CHANCE = 0.02;
    private static final double RELOAD_CHANCE = 0.10;

    private final GamePlayer owner;

    public LuckyMagazineEffect(GamePlayer owner) {
        this.owner = owner;
    }

    @Override
    public String getId() {
        return "lucky_magazine";
    }

    @Override
    public boolean onShotFired(ShotFiredContext ctx) {
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < EMPTY_CHANCE) {
            owner.getWeapon().setAmmo(0);
        } else if (roll < EMPTY_CHANCE + RELOAD_CHANCE) {
            owner.getWeapon().instantRefill();
        }
        return false;
    }
}