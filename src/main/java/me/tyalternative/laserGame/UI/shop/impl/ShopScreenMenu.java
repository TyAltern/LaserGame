package me.tyalternative.laserGame.UI.shop.impl;

import me.tyalternative.laserGame.UI.shop.*;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ShopScreenMenu  {


    private HologramElement shopScreen;

    public ShopScreenMenu(HologramElement parent) {
        this.shopScreen = createHologram(parent);
    }

    public HologramElement getHologram() { return shopScreen; }

    private HologramElement createHologram(HologramElement parent) {

        HologramElement shopScreen = new HologramElement.Builder("shop_screen", parent)
                .position(110,29).size(28,18).layer(1)
                .font(new NamespacedKey("menu","shop_screen"))
                .text("\uE2FF").hoverText("\uE3FF")
                .button()
                .build();

        HologramElement inventoryScreenButtonLink = new HologramElement.Builder("inventory_screen_button_link_from_shop", shopScreen)
                .position(81,29).size(28,18).layer(1)
                .font(new NamespacedKey("menu","menu"))
                .text("\uE000").hoverText("\uE100")
                .button()
                .build();
        HologramElement statsScreenButtonLink = new HologramElement.Builder("stats_screen_button_link_from_shop", shopScreen)
                .position(139,29).size(28,18).layer(1)
                .font(new NamespacedKey("menu","menu"))
                .text("\uE002").hoverText("\uE102")
                .button()
                .build();

        HologramElement consumableTabHeader = new HologramElement.Builder("consumable_tab_header", shopScreen)
                .position(17,48).size(86,14).layer(1)
                .text("\uE200").hoverText("\uE300")
                .button()
                .build();

        HologramElement specialItemsTabHeader = new HologramElement.Builder("special_items_tab_header", shopScreen)
                .position(109,48).size(86,14).layer(1)
                .text("\uE201").hoverText("\uE301")
                .button()
                .build();

        ScreenGroup shopGroup = new ScreenGroup(consumableTabHeader, specialItemsTabHeader);


        final boolean[] isReady = {false};
        List<HologramElement> readyButtons = new ArrayList<>();

        // Consumable Hologram

        HologramElement specialItemsTabHeaderButton = new HologramElement.Builder("special_items_tab_header_button", consumableTabHeader)
                .position(109,48).size(86,14).layer(1)
                .text("\uE001").hoverText("\uE101")
                .button()
                .onClick( HologramActions.showInGroup(shopGroup, specialItemsTabHeader))
                .build();
        HologramElement purseConsumableTabBody = new HologramElement.Builder("purse_consumable_tab_body", consumableTabHeader)
                .position(24,64).size(27,11).layer(1)
                .text("\uE00A").hoverText("\uE10A")
                .button()
                .build();
        HologramElement purseAmountConsumableTab = new HologramElement.Builder("purse_amount_consumable_tab", purseConsumableTabBody)
                .position(28,62).size(19,5).layer(2)
                .text("09734")
                .font(new NamespacedKey("menu","text"))
                .build();
        HologramElement rerollConsumableTabBody = new HologramElement.Builder("reroll_consumable_tab_body", consumableTabHeader)
                .position(16,99).size(43,32).layer(1)
                .text("\uE003").hoverText("\uE103")
                .button()
                .build();
        HologramElement rerollAmountConsumableTab = new HologramElement.Builder("reroll_amount_consumable_tab", rerollConsumableTabBody)
                .position(30,92).size(15,5).layer(2)
                .text("0025")
                .font(new NamespacedKey("menu","text"))
                .build();
        rerollConsumableTabBody.onClick((player, source, clickType) -> {
            player.sendMessage("Reroll");
            int value = Integer.parseInt(rerollAmountConsumableTab.getText());
            value = Math.min(9999, value + 25);
            rerollAmountConsumableTab.setText(String.format("%04d",value));
        });
        HologramElement readyConsumableTabBody = new HologramElement.Builder("ready_consumable_tab_body", consumableTabHeader)
                .position(19,123).size(38,20).layer(1)
                .text("\uE002").hoverText("\uE102")
                .button()
                .onClick( (player, source, clickType) -> {
                    isReady[0] = !isReady[0];

                    if (isReady[0]) {
                        for (HologramElement readyButton : readyButtons) {
                            readyButton.setText("\uE002"); source.setHoverText("\uE102");
                        }
                    } else {
                        for (HologramElement readyButton : readyButtons) {
                            readyButton.setText("\uE202"); source.setHoverText("\uE302");
                        }
                    }
                })
                .build();
        readyButtons.add(readyConsumableTabBody);

        List<HologramElement> consumableSlots = new ArrayList<>();
        for (int y = 0; y < 2; y++) {
            int posY = 87 + y * 38;
            for (int x = 0; x <3; x++) {
                int posX = 68 + x * 43;
                int index = x + y*3;
                HologramElement slot = new HologramElement.Builder("consumable_slot_" + index, consumableTabHeader)
                        .position(posX, posY).size(30,36).layer(1)
                        .text("\uE004").hoverText("\uE104")
                        .button()
                        .onClick( (player, source, clickType) -> {
                            boolean isPressed = source.getText().equals("\uE204");
                            if (isPressed) {
                                source.setText("\uE004"); source.setHoverText("\uE104");
                            } else {
                                source.setText("\uE204"); source.setHoverText("\uE304");
                            }
                        })
                        .build();

                consumableSlots.add(slot);
                HologramElement slotPriceTag = new HologramElement.Builder("slot_price_tag" + index, slot)
                        .position(posX+9,posY-2).size(11,5).layer(2)
                        .text(String.format("%03d", ThreadLocalRandom.current().nextInt(1000)))
                        .font(new NamespacedKey("menu","text"))
                        .build();
            }
        }

        // Special Items Hologram

        HologramElement consumableTabHeaderButton = new HologramElement.Builder("consumable_tab_header_button", specialItemsTabHeader)
                .position(17,48).size(86,14).layer(1)
                .text("\uE000").hoverText("\uE100")
                .button()
                .onClick( HologramActions.showInGroup(shopGroup, consumableTabHeader))
                .build();

        HologramElement purseSpecialItemsTabBody = new HologramElement.Builder("purse_special_items_tab_body", specialItemsTabHeader)
                .position(24,74).size(27,11).layer(1)
                .text("\uE00A").hoverText("\uE10A")
                .button()
                .build();
        HologramElement purseAmountSpecialItemsTab = new HologramElement.Builder("purse_amount_special_items_tab", specialItemsTabHeader)
                .position(28,72).size(19,5).layer(2)
                .text("09734")
                .font(new NamespacedKey("menu","text"))
                .build();
        HologramElement readySpecialItemsTabBody = new HologramElement.Builder("ready_special_items_tab_body", specialItemsTabHeader)
                .position(19,103).size(38,20).layer(1)
                .text("\uE002").hoverText("\uE102")
                .button()
                .onClick( (player, source, clickType) -> {
                    isReady[0] = !isReady[0];

                    if (isReady[0]) {
                        for (HologramElement readyButton : readyButtons) {
                            readyButton.setText("\uE002"); source.setHoverText("\uE102");
                        }
                    } else {
                        for (HologramElement readyButton : readyButtons) {
                            readyButton.setText("\uE202"); source.setHoverText("\uE302");
                        }
                    }
                })
                .build();

        readyButtons.add(readySpecialItemsTabBody);

        List<HologramElement> specialItemsSlots = new ArrayList<>();
        for (int x = 0; x <3; x++) {
            int posX = 68 + x * 43;
            int rarity = ThreadLocalRandom.current().nextInt(6,10);
            HologramElement specialItemsHeaderCard = new HologramElement.Builder("special_item_header_card_" + x, specialItemsTabHeader)
                    .position(posX-3, 75).size(36,13).layer(1)
                    .text(Glyph.parse("E00"+rarity)).hoverText(Glyph.parse("E10"+rarity))
                    .button()
                    .build();
            HologramElement slot = new HologramElement.Builder("special_item_slot_" + x, specialItemsTabHeader)
                    .position(posX, 114).size(30,36).layer(1)
                    .text("\uE004").hoverText("\uE104")
                    .button()
                    .onClick( (player, source, clickType) -> {
                        boolean isPressed = source.getText().equals("\uE204");
                        if (isPressed) {
                            source.setText("\uE004"); source.setHoverText("\uE104");
                        } else {
                            source.setText("\uE204"); source.setHoverText("\uE304");
                        }
                    })
                    .build();

            specialItemsSlots.add(slot);
            HologramElement slotPriceTag = new HologramElement.Builder("slot_price_tag" + x, slot)
                    .position(posX+9,112).size(11,5).layer(2)
                    .text(String.format("%03d", ThreadLocalRandom.current().nextInt(1000)))
                    .font(new NamespacedKey("menu","text"))
                    .build();

        }

        shopGroup.show(consumableTabHeader);

        return shopScreen;
    }
}
