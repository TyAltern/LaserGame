package me.tyalternative.laserGame.listeners;

import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.game.MatchState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.Optional;

public class ItemSelectionListener implements Listener {

    private final GameManager gameManager;

    public ItemSelectionListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onScroll(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        int diff = event.getNewSlot() - event.getPreviousSlot();
        Boolean scrollDown;
        if (diff == 1 || diff == -8) {
            scrollDown = true;
        } else if (diff == -1 || diff == 8) {
            scrollDown = false;
        } else {
            scrollDown = null;
        }

        if (scrollDown == null) return;

        Optional<Match> matchOpt = gameManager.getGame(player);
        if (matchOpt.isEmpty()) return;
        Match match = matchOpt.get();
        if (match.getState() != MatchState.ROUND_IN_PROGRESS && match.getState() != MatchState.SHOP) return;

        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(player);
        if (gpOpt.isEmpty()) return;
        GamePlayer gp = gpOpt.get();

        event.setCancelled(true);

        int maxSlots = gp.getConsumables().size();
        if (maxSlots <= 0) return;

        int newSelected = Math.floorMod(gp.getSelectedSlot() + (scrollDown ? 1 : -1), maxSlots);
        gp.setSelectedSlot(newSelected);
        // TODO : rafraîchir le HUD ici
    }
}
