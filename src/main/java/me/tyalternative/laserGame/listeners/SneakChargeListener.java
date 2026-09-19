package me.tyalternative.laserGame.listeners;

import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.MatchState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Optional;


public class SneakChargeListener implements Listener {

    private final GameManager gameManager;

    public SneakChargeListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(player);
        if (gpOpt.isEmpty()) return;

        GamePlayer gp = gpOpt.get();
        if (gp.getMatch().getState() == MatchState.SHOP) return;
        if (gp.isSpectator()) return;

        if (gp.isSneakTimed()) return;

        if (!gp.hasSneakCharge()) {
            event.setCancelled(true);
            player.sendMessage("§cTu n'as plus assez de charge pour te cacher.");
        }
    }
}
