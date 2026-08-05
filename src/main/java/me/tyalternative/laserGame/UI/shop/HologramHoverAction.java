package me.tyalternative.laserGame.UI.shop;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface HologramHoverAction {
    void execute(Player player, HologramElement source);
}
