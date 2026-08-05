package me.tyalternative.laserGame.UI.shop;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramHoverListener implements Listener {

    private static final double RAY_DISTANCE = 10.0;
    private static final double RAY_SIZE = 0.01;
    private static final long SCAN_PERIOD_TICKS = 1L;

    private static final Map<UUID, Hologram> OPEN_HOLOGRAMS = new HashMap<>();
    private static final Map<UUID, HologramElement> CURRENT_HOVER  = new HashMap<>();

    private static BukkitTask scanTask;

    public HologramHoverListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        if (scanTask == null) {
            scanTask = plugin.getServer().getScheduler().runTaskTimer(
                    plugin, HologramHoverListener::scanOpenPlayers, SCAN_PERIOD_TICKS, SCAN_PERIOD_TICKS
            );
        }
    }

    public static void open(Player player, Hologram hologram) {
        OPEN_HOLOGRAMS.put(player.getUniqueId(), hologram);
        scan(player);
    }

    public static void close(Player player) {
        OPEN_HOLOGRAMS.remove(player.getUniqueId());
        HologramElement previous = CURRENT_HOVER.remove(player.getUniqueId());
        if (previous != null) previous.onHoverExit(player);
    }

    public static HologramElement getCurrentHover(Player player) {
        return CURRENT_HOVER.get(player.getUniqueId());
    }

    public static void shutdown() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
        OPEN_HOLOGRAMS.clear();
        CURRENT_HOVER.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        close(event.getPlayer());
    }

    private static void scanOpenPlayers() {
        if (OPEN_HOLOGRAMS.isEmpty()) return;
        for (UUID playerId : new ArrayList<>(OPEN_HOLOGRAMS.keySet())) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) scan(player);
        }
    }

    private static void scan(Player player) {
        Location eye = player.getEyeLocation();

        RayTraceResult result = player.getWorld().rayTrace(
                eye,
                eye.getDirection(),
                RAY_DISTANCE,
                FluidCollisionMode.NEVER,
                true,
                RAY_SIZE,
                entity -> entity instanceof Interaction
        );

        HologramElement hovered = null;
        if (result != null && result.getHitEntity() instanceof Interaction interactionHit) {
            hovered = HologramElement.getElementForInteraction(interactionHit.getUniqueId());
        }

        HologramElement previous = CURRENT_HOVER.get(player.getUniqueId());
        if (hovered == previous) return;

        if (previous != null) previous.onHoverExit(player);
        if (hovered != null) hovered.onHoverEnter(player);

        CURRENT_HOVER.put(player.getUniqueId(), hovered);
    }
}
