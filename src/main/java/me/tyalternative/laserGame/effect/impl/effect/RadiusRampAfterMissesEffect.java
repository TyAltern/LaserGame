package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;

public class RadiusRampAfterMissesEffect implements PendingEffect {

    private static final int THRESHOLD = 5;
    private static final double RADIUS_MULTIPLIER = 2.0;

    private final GamePlayer owner;
    private int consecutiveMisses = 0;
    private StatModifier currentModifier;

    public RadiusRampAfterMissesEffect(GamePlayer owner) {
        this.owner = owner;
    }

    @Override
    public String getId() {
        return "radius_ramp_after_misses";
    }

    private void clear() {
        if (currentModifier != null) {
            owner.removeStatModifier(currentModifier);
            currentModifier = null;
        }
    }

    @Override
    public boolean onShotMissed() {
        consecutiveMisses++;
        if (consecutiveMisses >= THRESHOLD && currentModifier == null) {
            currentModifier = stats -> stats.setHitRadius(stats.getHitRadius() * RADIUS_MULTIPLIER);
            owner.addRoundStatModifier(currentModifier);
        }
        return false;
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        consecutiveMisses = 0;
        clear();
        return false;
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        consecutiveMisses = 0;
        clear();
        return false;
    }

    @Override
    public boolean onRoundStart() {
        consecutiveMisses = 0;
        currentModifier = null;
        return false;
    }
}
