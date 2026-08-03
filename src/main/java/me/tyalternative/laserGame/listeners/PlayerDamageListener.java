package me.tyalternative.laserGame.listeners;

import me.tyalternative.laserGame.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class PlayerDamageListener implements Listener {

    private final GameManager gameManager;

    public PlayerDamageListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (gameManager.getGame(player).isEmpty()) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (gameManager.getGame(player).isEmpty()) return;

        event.setCancelled(true);
    }

}
