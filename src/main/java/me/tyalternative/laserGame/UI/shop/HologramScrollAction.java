package me.tyalternative.laserGame.UI.shop;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface HologramScrollAction {
    void execute(Player player, HologramElement source, HologramScrollType scrollType);
}
