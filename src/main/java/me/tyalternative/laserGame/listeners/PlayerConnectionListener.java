package me.tyalternative.laserGame.listeners;

import me.tyalternative.laserGame.game.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final GameManager gameManager;

    public PlayerConnectionListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        gameManager.handleDisconnect(event.getPlayer());
    }
}
