package me.tyalternative.laserGame.effect;

public interface PendingEffect {
    String getId();

    default boolean onShotFired(ShotFiredContext ctx) { return false; }
    default boolean onShotMissed() { return false; }
    default boolean onShotHit(HitResolutionContext ctx) { return false; }
    default boolean onDamageTaken(HitResolutionContext ctx) { return false; }
    default boolean onReloadCompleted() { return false; }
    default boolean onRoundStart() { return false; }
    default boolean onRespawn() { return false; }
}
