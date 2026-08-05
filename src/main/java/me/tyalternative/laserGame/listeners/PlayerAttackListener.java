package me.tyalternative.laserGame.listeners;

import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.game.MatchState;
import me.tyalternative.laserGame.weapon.EffectiveWeaponStats;
import me.tyalternative.laserGame.weapon.ShotTrailRenderer;
import me.tyalternative.laserGame.weapon.WeaponManager;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Optional;

public class PlayerAttackListener implements Listener {

    private final Plugin plugin;
    private final WeaponManager weaponManager;
    private final GameManager gameManager;
    private final ShotTrailRenderer trailRenderer;

    public PlayerAttackListener(Plugin plugin, WeaponManager weaponManager, GameManager gameManager, ShotTrailRenderer trailRenderer) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
        this.gameManager = gameManager;
        this.trailRenderer = trailRenderer;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        if (weaponManager.resolve(player.getInventory().getItemInMainHand()).isEmpty()) return;

        Optional<Match> matchOpt = gameManager.getGame(player);
        if (matchOpt.isEmpty()) return;
        Match match = matchOpt.get();
        if (match.getState() != MatchState.ROUND_IN_PROGRESS || match.getCurrentRound() == null) return;

        Optional<GamePlayer> gpOpt = match.getGamePlayer(player);
        if (gpOpt.isEmpty() || gpOpt.get().isSpectator()) return;
        GamePlayer shooter = gpOpt.get();

        int swingTick = Bukkit.getCurrentTick();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (shooter.consumeSuppressedSwingTick(swingTick)) return; // faux swing (touche Q), pas un vrai tir

            if (!shooter.getWeapon().tryShoot()) return;
            performShot(match, shooter, player);
        });
    }

    private void performShot(Match match, GamePlayer shooter, Player player) {
        EffectiveWeaponStats stats = shooter.getEffectiveStats();
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();

        RayTraceResult result = player.getWorld().rayTrace(
                eye,
                direction,
                stats.getRange(),
                FluidCollisionMode.NEVER,
                true,
                stats.getHitRadius(),
                entity -> entity instanceof Player target
                        && !target.equals(player)
                        && match.getGamePlayer(target).isPresent()
                        && !match.getGamePlayer(target).get().isSpectator()
        );

        Location endpoint = result != null
                ? result.getHitPosition().toLocation(player.getWorld())
                : eye.clone().add(direction.multiply(stats.getRange()));

        trailRenderer.render(eye, endpoint);

        if (result != null && result.getHitEntity() instanceof Player target) {
            handleHit(match, shooter, target, stats);
        } else {
            shooter.getEffects().fireShotMissed();
        }
    }

    private void handleHit(Match match, GamePlayer shooter, Player target, EffectiveWeaponStats shooterStats) {
        Optional<GamePlayer> targetGpOpt = match.getGamePlayer(target);
        if (targetGpOpt.isEmpty()) return;

        Player shooterPlayer = shooter.getPlayer();
        if (shooterPlayer != null) {
            shooterPlayer.sendMessage("§aTouché : " + target.getName());
            shooterPlayer.playSound(shooterPlayer.getLocation(),
                    org.bukkit.Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1.5f);
        }
        target.sendMessage("§cTouché par " + (shooterPlayer != null ? shooterPlayer.getName() : "un adversaire"));

        match.getCurrentRound().onPlayerHit(shooter, targetGpOpt.get(), shooterStats.getLivesPerHit());
    }
}
