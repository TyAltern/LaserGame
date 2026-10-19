package me.tyalternative.laserGame.game;

import me.tyalternative.laserGame.LaserGame;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class RoundAccess {

    private RoundAccess() {}

    public static Optional<Round> of(GamePlayer gp) {
        Player player = gp.getPlayer();
        if (player == null) return Optional.empty();
        return LaserGame.getInstance().getGameManager().getGame(player).map(Match::getCurrentRound);
    }

}
