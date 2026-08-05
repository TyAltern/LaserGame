package me.tyalternative.laserGame.UI.shop;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class HologramClickListener implements Listener {

    public HologramClickListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;
        handleCLick(event.getPlayer(), interaction, HologramClickType.RIGHT);
    }

    @EventHandler
    public void onLeftClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Interaction interaction)) return;

        event.setCancelled(true);
        handleCLick(player, interaction, HologramClickType.LEFT);
    }

    private void handleCLick(Player player, Interaction interaction, HologramClickType type) {
        HologramElement element = HologramElement.getElementForInteraction(interaction.getUniqueId());
        if (element == null) return;

        if (element.isDisabled()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (element.isOnCooldown(player.getUniqueId())) return;

        HologramClickEvent clickEvent = new HologramClickEvent(player, element, type);
        Bukkit.getPluginManager().callEvent(clickEvent);
        if (clickEvent.isCancelled()) return;

        element.markClicked(player.getUniqueId());
        element.executeActions(player, type);
    }
}
