package me.tyalternative.laserGame.listeners;

import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.game.MatchState;
import me.tyalternative.laserGame.shop.ConsumableManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.Optional;

public class ConsumableUseListener implements Listener {

    private final GameManager gameManager;
    private final ConsumableManager consumableManager;

    public ConsumableUseListener(GameManager gameManager, ConsumableManager consumableManager) {
        this.gameManager = gameManager;
        this.consumableManager = consumableManager;
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {

        Player player = event.getPlayer();
        Optional<Match> matchOpt = gameManager.getGame(player);
        if (matchOpt.isEmpty()) return;

        Match match = matchOpt.get();
        if (match.getState() != MatchState.ROUND_IN_PROGRESS) return;

        event.setCancelled(true);

        Optional<GamePlayer> gpOpt = match.getGamePlayer(player);
        if (gpOpt.isEmpty() || gpOpt.get().isSpectator()) return;
        GamePlayer gp = gpOpt.get();

        consumableManager.activate(gp, player, match, gp.getSelectedSlot());
    }
}
