package me.tyalternative.laserGame.effect;

import me.tyalternative.laserGame.game.GamePlayer;

public class HitResolutionContext {

    public final GamePlayer shooter;
    public final GamePlayer target;
    public int livesToRemove;
    public final double distance;
    public boolean wouldBeFirstElimination = false;

    public HitResolutionContext(GamePlayer shooter, GamePlayer target, int baseLivesToRemove, double distance) {
        this.shooter = shooter;
        this.target = target;
        this.livesToRemove = baseLivesToRemove;
        this.distance = distance;
    }
}
