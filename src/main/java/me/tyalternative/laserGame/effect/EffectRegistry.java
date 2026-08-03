package me.tyalternative.laserGame.effect;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class EffectRegistry {

    private final List<PendingEffect> effects = new CopyOnWriteArrayList<>();
    private final Set<PendingEffect> permanent = Collections.newSetFromMap(new IdentityHashMap<>());

    public void add(PendingEffect effect) {
        effects.add(effect);
    }

    public void addPermanent(PendingEffect effect) {
        effects.add(effect);
        permanent.add(effect);
    }

    public void remove(PendingEffect effect) {
        effects.remove(effect);
        permanent.remove(effect);
    }

    public void fireShotFired(ShotFiredContext ctx) {
        for (PendingEffect effect : effects) {
            if (effect.onShotFired(ctx) && !permanent.contains(effect)) {
                effects.remove(effect);
            }
        }
    }

    public void fireShotMissed() {
        for (PendingEffect effect : effects) {
            if (effect.onShotMissed() && !permanent.contains(effect)) {
                effects.remove(effect);
            }
        }
    }

    public void fireShotHit(HitResolutionContext ctx) {
        for (PendingEffect effect : effects) {
            if (effect.onShotHit(ctx) && !permanent.contains(effect)) {
                effects.remove(effect);
            }
        }
    }

    public void fireDamageTaken(HitResolutionContext ctx) {
        for (PendingEffect effect : effects) {
            if (effect.onDamageTaken(ctx) && !permanent.contains(effect)) {
                effects.remove(effect);
            }
        }
    }

    public List<PendingEffect> getActive() {
        return effects;
    }

    public void clear() {
        effects.removeIf(e -> !permanent.contains(e));
    }
}
