package me.tyalternative.laserGame.listeners;

import org.bukkit.Bukkit;
import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.MatchState;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;


public class SneakChargeListener implements Listener {

    private final GameManager gameManager;

    public SneakChargeListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        Optional<GamePlayer> gpOpt = gameManager.getGamePlayer(player);
        if (gpOpt.isEmpty()) return;

        GamePlayer gp = gpOpt.get();
        gp.setSneakStartTick(event.isSneaking() ? Bukkit.getCurrentTick() : -1);
        if (!event.isSneaking()) return;

        if (gp.getMatch().getState() == MatchState.SHOP) return;
        if (gp.isSpectator()) return;

        if (gp.isSneakTimed()) return;

        if (!gp.hasSneakCharge()) {
            event.setCancelled(true);
            gp.setSneakStartTick(-1);
            Lang.send(player, "combat.sneak-no-charge-hide");
        }
    }
}
