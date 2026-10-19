package me.tyalternative.laserGame.effect;

import me.tyalternative.laserGame.game.GamePlayer;

public interface PendingEffect {
    String getId();

    default boolean onShotFired(ShotFiredContext ctx) { return false; }
    default boolean onShotMissed() { return false; }
    default boolean onShotHit(HitResolutionContext ctx) { return false; }
    default boolean onShotHitFinal(HitResolutionContext ctx) { return false; }
    default boolean onDamageTaken(HitResolutionContext ctx) { return false; }
    default boolean onReloadCompleted() { return false; }
    default boolean onRoundStart() { return false; }
    default boolean onRespawn() { return false; }
    default boolean onLifeLost() { return false; }
    default boolean onEliminationMade(GamePlayer victim) { return false; }
    default boolean onLivesChanged() { return false; }
}
