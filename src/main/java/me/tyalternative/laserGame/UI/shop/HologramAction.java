package me.tyalternative.laserGame.UI.shop;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface HologramAction {
    void execute(Player player, HologramElement source, HologramClickType clickType);
}
