package me.tyalternative.laserGame.effect;

import me.tyalternative.laserGame.game.GamePlayer;

public class HitResolutionContext {

    public final GamePlayer shooter;
    public final GamePlayer target;
    public int livesToRemove;

    public HitResolutionContext(GamePlayer shooter, GamePlayer target, int baseLivesToRemove) {
        this.shooter = shooter;
        this.target = target;
        this.livesToRemove = baseLivesToRemove;
    }
}
