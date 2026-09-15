package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;

public class FireRateRampOnMissEffect implements PendingEffect {

    private static final int MAX_STACKS = 10;
    private static final double PERCENT_PER_STACK = -0.05;

    private final GamePlayer owner;
    private int missStreak = 0;
    private StatModifier currentModifier;

    public FireRateRampOnMissEffect(GamePlayer owner) {
        this.owner = owner;
    }

    @Override
    public String getId() {
        return "fire_rate_ramp_on_miss";
    }

    private void reapply() {
        if (currentModifier != null) {
            owner.removeStatModifier(currentModifier);
            currentModifier = null;
        }
        if (missStreak <= 0) return;

        double percent = Math.max(-0.7, PERCENT_PER_STACK * missStreak);
        currentModifier = stats -> stats.setShotCooldownTicks(Math.max(1, (long) Math.floor(stats.getShotCooldownTicks() * (1 + percent))));
        owner.addStatModifier(currentModifier);
    }

    @Override
    public boolean onShotMissed() {
        missStreak = Math.min(MAX_STACKS, missStreak + 1);
        reapply();
        return false;
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        missStreak = 0;
        reapply();
        return false;
    }

    @Override
    public boolean onReloadCompleted() {
        missStreak = 0;
        reapply();
        return false;
    }
}
