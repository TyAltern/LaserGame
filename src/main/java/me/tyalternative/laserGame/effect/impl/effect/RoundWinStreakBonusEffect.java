package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Si vous ne perdez aucune vie et remportez le round, boost de vitesse/munitions/cooldown stackable pour le
 * round suivant, reset dès le premier coup reçu.
 */
public class RoundWinStreakBonusEffect implements PendingEffect {

    private static final double SPEED_PER_STACK = 0.20;
    private static final double AMMO_PERCENT_PER_STACK = 0.25;
    private static final double COOLDOWN_PERCENT_PER_STACK = -0.20;
    private static final int MAX_STACKS = 5;

    private final GamePlayer owner;
    private boolean tookDamageThisRound = false;
    private int lastKnownRoundWins = -1;
    private int stacks = 0;
    private final List<StatModifier> activeStackModifiers = new ArrayList<>();

    public RoundWinStreakBonusEffect(GamePlayer owner) {
        this.owner = owner;
    }

    @Override
    public String getId() {
        return "round_win_streak_bonus";
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        tookDamageThisRound = true;
        clearStacks();
        return false;
    }

    @Override
    public boolean onRoundStart() {
        if (lastKnownRoundWins >= 0 && owner.getRoundWins() > lastKnownRoundWins && !tookDamageThisRound) {
            stacks = Math.min(MAX_STACKS, stacks + 1);
            applyStacks();
        }
        lastKnownRoundWins = owner.getRoundWins();
        tookDamageThisRound = false;
        return false;
    }

    private void applyStacks() {
        activeStackModifiers.forEach(owner::removeStatModifier);
        activeStackModifiers.clear();

        int currentStacks = stacks;
        StatModifier mod = stats -> {
            stats.setMovementSpeedModifier(stats.getMovementSpeedModifier() + SPEED_PER_STACK * currentStacks);
            stats.setMaxAmmo((int) Math.round(stats.getMaxAmmo() * (1 + AMMO_PERCENT_PER_STACK * currentStacks)));
            stats.setShotCooldownTicks(Math.max(1, (long) Math.floor(
                    stats.getShotCooldownTicks() * (1 + COOLDOWN_PERCENT_PER_STACK * currentStacks))));
        };
        owner.addStatModifier(mod);
        activeStackModifiers.add(mod);
    }

    private void clearStacks() {
        if (stacks == 0) return;
        stacks = 0;
        activeStackModifiers.forEach(owner::removeStatModifier);
        activeStackModifiers.clear();
    }
}
