package me.tyalternative.laserGame.UI.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HologramClickEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final HologramElement element;
    private final HologramClickType clickType;
    private boolean cancelled = false;

    public HologramClickEvent(Player player, HologramElement element, HologramClickType clickType) {
        this.player = player;
        this.element = element;
        this.clickType = clickType;
    }

    public Player getPlayer() { return player; }
    public HologramElement getElement() { return element; }
    public HologramClickType getClickType() { return clickType; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
