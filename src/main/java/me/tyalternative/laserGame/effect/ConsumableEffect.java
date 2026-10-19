package me.tyalternative.laserGame.effect;

public interface ConsumableEffect {
    boolean activate(ActivationContext ctx);
    default void afterConsumed(ActivationContext ctx) { }
}
