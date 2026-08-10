package me.tyalternative.laserGame.UI.shop.impl;

import me.tyalternative.laserGame.UI.shop.*;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.shop.ShopContext;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LaserGameMenu {


    private final Hologram hologram;
    private final GamePlayer gamePlayer;
    private final ShopContext shopContext;
    private HologramElement menuTimerDisplay;

    private InventoryScreenMenu inventoryScreenMenu;
    private ShopScreenMenu shopScreenMenu;
    private StatsScreenMenu statsScreenMenu;

    /**
     * @param onReadyToggle appelé avec le nouvel état "prêt" du joueur à chaque clic sur un bouton Ready
     *                      (câblé par Match vers Match#setPlayerReady).
     */
    public LaserGameMenu(Location location, GamePlayer gamePlayer, ShopContext shopContext,
                         int shopPhaseSeconds, List<GamePlayer> matchPlayers, Consumer<Boolean> onReadyToggle) {
        this.gamePlayer = gamePlayer;
        this.shopContext = shopContext;
        this.hologram = createHologram(location, shopPhaseSeconds, matchPlayers, onReadyToggle);
    }

    public Hologram getHologram() { return hologram; }
    public HologramElement getRoot() { return hologram.getRoot(); }
    /** Rafraîchit les données réelles affichées (prix, possession, équipement...). À appeler après tout achat/équipement. */

    public void refresh() {
        shopScreenMenu.refresh(gamePlayer, shopContext, hologram.getItemHintPanel());
        inventoryScreenMenu.refresh(gamePlayer, shopContext, hologram.getItemHintPanel());
    }


    public void setRemainingSeconds(int seconds) {
        menuTimerDisplay.setText(formatTimer(seconds));
    }


    public void refreshStats(List<GamePlayer> matchPlayers) {
        statsScreenMenu.refresh(gamePlayer, matchPlayers);
    }


    private Hologram createHologram(Location location, int shopPhaseSeconds, List<GamePlayer> matchPlayers, Consumer<Boolean> onReadyToggle) {


        Hologram hologram = new Hologram(location, "menu", 212, 138, "\uFFFF", Font.MENU);

        HologramElement menuTextTitle = new HologramElement.Builder("menu_text_title", hologram.getRoot())
                .position(13,31).size(63,19).layer(2)
                .font(Font.MENU)
                .text("\uE002")
                .build();

        menuTimerDisplay = new HologramElement.Builder("menu_timer_display", hologram.getRoot())
                .position(177,23).size(17,5).layer(2).font(Font.TEXT)
                .text(formatTimer(shopPhaseSeconds))
                .build();


        this.inventoryScreenMenu = new InventoryScreenMenu(hologram.getRoot(), this::refresh);
        this.shopScreenMenu = new ShopScreenMenu(hologram.getRoot(), this::refresh);
        this.statsScreenMenu = new StatsScreenMenu(hologram.getRoot());

        hologram.setItemHintPanel(new ItemHintPanel(hologram.getRoot()));

        ScreenGroup menuHeader = new ScreenGroup(inventoryScreenMenu.getHologram(), shopScreenMenu.getHologram(), statsScreenMenu.getHologram());

        for (HologramElement child : inventoryScreenMenu.getHologram().getChildren()) {
            if (child == null) continue;
            if (child.getId().equals("shop_screen_button_link_from_inventory")) {
                child.onClick((player1, source, clickType) -> {
                    menuHeader.show(shopScreenMenu.getHologram());
                    menuTextTitle.setText("\uE002");
                });
            }
            if (child.getId().equals("stats_screen_button_link_from_inventory")) {
                child.onClick((player1, source, clickType) -> {
                    menuHeader.show(statsScreenMenu.getHologram());
                    menuTextTitle.setText("\uE003");
                    hologram.getRoot().setText("\uEFFF");
                });
            }

        }

        for (HologramElement child : shopScreenMenu.getHologram().getChildren()) {
            if (child == null) continue;
            if (child.getId().equals("inventory_screen_button_link_from_shop")) {
                child.onClick((player1, source, clickType) -> {
                    menuHeader.show(inventoryScreenMenu.getHologram());
                    menuTextTitle.setText("\uE001");
                });
            }
            if (child.getId().equals("stats_screen_button_link_from_shop")) {
                child.onClick((player1, source, clickType) -> {
                    menuHeader.show(statsScreenMenu.getHologram());
                    menuTextTitle.setText("\uE003");
                    hologram.getRoot().setText("\uEFFF");
                });
            }
        }

        for (HologramElement child : statsScreenMenu.getHologram().getChildren()) {
            if (child == null) continue;
            if (child.getId().equals("inventory_screen_button_link_from_stats")) {
                child.onClick((player1, source, clickType) -> {
                    menuHeader.show(inventoryScreenMenu.getHologram());
                    menuTextTitle.setText("\uE001");
                    hologram.getRoot().setText("\uFFFF");
                });
            }
            if (child.getId().equals("shop_screen_button_link_from_stats")) {
                child.onClick((player1, source, clickType) -> {
                    menuHeader.show(shopScreenMenu.getHologram());
                    menuTextTitle.setText("\uE002");
                    hologram.getRoot().setText("\uFFFF");
                });
            }
        }

        List<HologramElement> readyButtons = new ArrayList<>();
        readyButtons.addAll(inventoryScreenMenu.getReadyButtons());
        readyButtons.addAll(shopScreenMenu.getReadyButtons());
        readyButtons.addAll(statsScreenMenu.getReadyButtons());
        List<Boolean> isReady = new ArrayList<>(0);
        isReady.add(false);

        for (HologramElement readyButton : readyButtons) {
            readyButton.onClick((player1, source, clickType) -> {
                boolean ready = isReady.getFirst();
                if (ready) {
                    for (HologramElement button : readyButtons) {
                        button.setText("\uE000"); button.setHoverText("\uE100");
                    }
                } else {
                    for (HologramElement button : readyButtons) {
                        button.setText("\uE200"); button.setHoverText("\uE300");
                    }
                }
                isReady.set(0, !ready);
                if (onReadyToggle != null) onReadyToggle.accept(!ready);
            });
        }


        menuHeader.show(shopScreenMenu.getHologram());

        shopScreenMenu.refresh(gamePlayer, shopContext, hologram.getItemHintPanel());
        inventoryScreenMenu.refresh(gamePlayer, shopContext, hologram.getItemHintPanel());
        statsScreenMenu.refresh(gamePlayer, matchPlayers);

        return hologram;
    }


    private static String formatTimer(int totalSeconds) {
        int minutes = Math.max(0, totalSeconds) / 60;
        int seconds = Math.max(0, totalSeconds) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
