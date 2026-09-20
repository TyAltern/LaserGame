package me.tyalternative.laserGame.listeners;

import me.tyalternative.laserGame.UI.shop.HologramElement;
import me.tyalternative.laserGame.UI.shop.HologramHoverListener;
import me.tyalternative.laserGame.UI.shop.HologramScrollType;
import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.game.MatchState;
import me.tyalternative.laserGame.shop.ConsumableInventory;
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
        if ((diff >= 1 && diff < 6) || diff == -8) {
            scrollDown = true;
        } else if ((diff <= -1 && diff > -8) || diff == 8) {
            scrollDown = false;
        } else {
            scrollDown = null;
        }

        if (scrollDown == null) return;

        HologramElement hovered = HologramHoverListener.getCurrentHover(player);
        if (hovered != null) {

            event.setCancelled(true);
            hovered.executeScrollActions(player, scrollDown ? HologramScrollType.DOWN : HologramScrollType.UP);
            return;
        }

        Optional<Match> matchOpt = gameManager.getGame(player);
        if (matchOpt.isEmpty()) return;
        Match match = matchOpt.get();
        if (match.getState() != MatchState.ROUND_IN_PROGRESS && match.getState() != MatchState.SHOP) return;

        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(player);
        if (gpOpt.isEmpty()) return;
        GamePlayer gp = gpOpt.get();

        event.setCancelled(true);
        ConsumableInventory inventory = gp.getConsumables();
        if (inventory.occupiedSize() <= 1) return;
        int maxSlots = gp.getConsumables().size();
        Optional<String> nextSlot;
        int newSelected = gp.getSelectedSlot();
        do {
            newSelected = Math.floorMod(newSelected + (scrollDown ? 1 : -1), maxSlots);
            nextSlot = inventory.get(newSelected);
        } while (nextSlot.isEmpty());

        gp.setSelectedSlot(newSelected);
        match.getActionBarManager().updatePlayerActionBar(gp);
    }
}
