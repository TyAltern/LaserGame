package me.tyalternative.laserGame.UI.shop.impl;

import me.tyalternative.laserGame.UI.shop.HologramElement;
import org.bukkit.NamespacedKey;

public class InventoryScreenMenu {

    private HologramElement inventoryScreen;

    public InventoryScreenMenu(HologramElement parent) {
        this.inventoryScreen = createHologram(parent);
    }

    public HologramElement getHologram() { return inventoryScreen; }


    private HologramElement createHologram(HologramElement parent) {

        HologramElement inventoryScreen = new HologramElement.Builder("inventory_screen", parent)
                .position(81, 29).size(28, 18).layer(1)
                .font(new NamespacedKey("menu","menu"))
                .text("\uE200").hoverText("\uE300")
                .button()
                .build();

        HologramElement shopScreenButtonLink = new HologramElement.Builder("shop_screen_button_link_from_inventory", inventoryScreen)
                .position(110, 29).size(28, 18).layer(1)
                .font(new NamespacedKey("menu","shop_screen"))
                .text("\uE0FF").hoverText("\uE1FF")
                .button()
                .build();
        HologramElement statsScreenButtonLink = new HologramElement.Builder("stats_screen_button_link_from_inventory", inventoryScreen)
                .position(139, 29).size(28, 18).layer(1)
                .font(new NamespacedKey("menu","menu"))
                .text("\uE002").hoverText("\uE102")
                .button()
                .build();


        return inventoryScreen;
    }
}
