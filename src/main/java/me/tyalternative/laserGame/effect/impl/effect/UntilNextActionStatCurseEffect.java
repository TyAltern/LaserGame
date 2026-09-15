package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.List;

public class UntilNextActionStatCurseEffect implements PendingEffect {

    private final String id;
    private final GamePlayer target;
    private final List<StatModifier> modifiers;

    public UntilNextActionStatCurseEffect(String id, GamePlayer target, List<StatModifier> modifiers) {
        this.id = id;
        this.target = target;
        this.modifiers = modifiers;
        modifiers.forEach(target::addStatModifier);
    }

    @Override
    public String getId() {
        return id;
    }

    private boolean clear() {
        modifiers.forEach(target::removeStatModifier);
        return true;
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        return clear();
    }

    @Override
    public boolean onDamageTaken(HitResolutionContext ctx) {
        return clear();
    }
}