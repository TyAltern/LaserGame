package me.tyalternative.laserGame.UI.shop.impl;

import me.tyalternative.laserGame.UI.shop.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class LaserGameMenu {


    private Hologram hologram;

    public LaserGameMenu(Location location, Player player) {
        this.hologram = createHologram(location, player);
    }

    public Hologram getHologram() { return hologram; }
    public HologramElement getRoot() { return hologram.getRoot(); }

    private Hologram createHologram(Location location, Player player) {


        Hologram hologram = new Hologram(location, "menu", 212, 138, "\uFFFF", new NamespacedKey("menu","menu"));

        InventoryScreenMenu inventoryScreenMenu = new InventoryScreenMenu(hologram.getRoot());
        ShopScreenMenu shopScreenMenu = new ShopScreenMenu(hologram.getRoot());

        ScreenGroup menuHeader = new ScreenGroup(inventoryScreenMenu.getHologram(), shopScreenMenu.getHologram());

        player.sendMessage("inventoryScreenMenu:");
        for (HologramElement child : inventoryScreenMenu.getHologram().getChildren()) {
            if (child == null) continue;
            player.sendMessage("---> " + child.getId());
            if (child.getId().equals("shop_screen_button_link_from_inventory")) {
                player.sendMessage("------> Valide");
                child.onClick(HologramActions.showInGroup(menuHeader, shopScreenMenu.getHologram()));
            }

        }
        player.sendMessage("shopScreenMenu:");
        for (HologramElement child : shopScreenMenu.getHologram().getChildren()) {
            if (child == null) continue;
            player.sendMessage("---> " + child.getId());
            if (child.getId().equals("inventory_screen_button_link_from_shop")) {
                player.sendMessage("------> Valide");
                child.onClick(HologramActions.showInGroup(menuHeader, inventoryScreenMenu.getHologram()));
            }
        }
//        shopScreenMenu.getHologram().getChild("inventory_screen_button_link_from_shop")
//                .onClick(HologramActions.showInGroup(menuHeader, inventoryScreenMenu.getHologram()));
//        shopScreenMenu.getHologram().getChild("shop_screen_button_link_from_inventory")
//                .onClick(HologramActions.showInGroup(menuHeader, shopScreenMenu.getHologram()));


        menuHeader.show(shopScreenMenu.getHologram());

        return hologram;
    }
}
