package me.tyalternative.laserGame.listeners;

import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.game.MatchState;
import me.tyalternative.laserGame.skill.SkillManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Optional;

public class SkillUseListener implements Listener {

    private final GameManager gameManager;
    private final SkillManager skillManager;

    public SkillUseListener(GameManager gameManager, SkillManager skillManager) {
        this.gameManager = gameManager;
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {

        Player player = event.getPlayer();
        Optional<Match> matchOpt = gameManager.getGame(player);
        if (matchOpt.isEmpty()) return;

        Match match = matchOpt.get();
        if (match.getState() != MatchState.ROUND_IN_PROGRESS) return;

        event.setCancelled(true);

        Optional<GamePlayer> gpOpt = match.getGamePlayer(player);
        if (gpOpt.isEmpty() || gpOpt.get().isSpectator()) return;
        GamePlayer gp = gpOpt.get();

        skillManager.activate(gp, player, match);
    }
}
