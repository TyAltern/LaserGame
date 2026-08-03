package me.tyalternative.laserGame.listeners;

import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.game.MatchState;
import me.tyalternative.laserGame.weapon.WeaponManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Optional;

public class PlayerInteractListener implements Listener {

    private final WeaponManager weaponManager;
    private final GameManager gameManager;

    public PlayerInteractListener(WeaponManager weaponManager, GameManager gameManager) {
        this.weaponManager = weaponManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (weaponManager.resolve(event.getItem()).isEmpty()) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }

        Optional<Match> matchOpt = gameManager.getGame(player);
        if (matchOpt.isEmpty()) return;
        Match match = matchOpt.get();
        if (match.getState() != MatchState.ROUND_IN_PROGRESS) return;

        Optional<GamePlayer> gpOpt = match.getGamePlayer(player);
        if (gpOpt.isEmpty() || gpOpt.get().isSpectator()) return;

        gpOpt.get().getWeapon().startManualReload(player);
    }
}
