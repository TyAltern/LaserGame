package me.tyalternative.laserGame.UI.shop.impl;

import me.tyalternative.laserGame.UI.shop.*;
import me.tyalternative.laserGame.utils.Font;
import me.tyalternative.laserGame.utils.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ShopScreenMenuOld {


    private HologramElement shopScreen;
    private final List<HologramElement> readyButtons = new ArrayList<>();

    public ShopScreenMenuOld(HologramElement parent) {
        this.shopScreen = createHologram(parent);
    }

    public HologramElement getHologram() { return shopScreen; }

    public List<HologramElement> getReadyButtons() { return readyButtons; }

    private HologramElement createHologram(HologramElement parent) {

        HologramElement shopScreen = new HologramElement.Builder("shop_screen", parent)
                .position(110,29).size(28,18).layer(1)
                .font(Font.SHOP)
                .text("\uE2FF").hoverText("\uE3FF")
                .button()
                .build();

        HologramElement inventoryScreenButtonLink = new HologramElement.Builder("inventory_screen_button_link_from_shop", shopScreen)
                .position(81,29).size(28,18).layer(1)
                .font(Font.INVENTORY)
                .text("\uE0FF").hoverText("\uE1FF")
                .button()
                .build();
        HologramElement statsScreenButtonLink = new HologramElement.Builder("stats_screen_button_link_from_shop", shopScreen)
                .position(139,29).size(28,18).layer(1)
                .text("\uE0FF").hoverText("\uE1FF")
                .font(Font.STATS)
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
                .position(28,62).size(19,5).layer(1)
                .text("09734")
                .font(Font.TEXT)
                .build();
        HologramElement rerollConsumableTabBody = new HologramElement.Builder("reroll_consumable_tab_body", consumableTabHeader)
                .position(16,99).size(43,32).layer(1)
                .text("\uE003").hoverText("\uE103")
                .button()
                .build();
        HologramElement rerollAmountConsumableTab = new HologramElement.Builder("reroll_amount_consumable_tab", rerollConsumableTabBody)
                .position(30,92).size(15,5).layer(1)
                .text("0025")
                .font(Font.TEXT)
                .build();
        rerollConsumableTabBody.onClick((player, source, clickType) -> {
            player.sendMessage("Reroll");
            int value = Integer.parseInt(rerollAmountConsumableTab.getText());
            value = Math.min(9999, value + 25);
            rerollAmountConsumableTab.setText(String.format("%04d",value));
        });
        HologramElement readyConsumableTabBody = new HologramElement.Builder("ready_consumable_tab_body", consumableTabHeader)
                .position(19,123).size(38,20).layer(1)
                .text("\uE000").hoverText("\uE100").font(Font.MENU)
                .button()
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
                        .text(TextUtil.parse("E00" + (index == 5 ? 5 : 4))).hoverText(TextUtil.parse("E10" + (index == 5 ? 5 : 4)))
                        .hasHint(true)
                        .button()
                        .onClick( (player, source, clickType) -> {
                            if (source.getText().equals("\uE105") || source.getText().equals("\uE005")) return;
                            boolean isPressed = source.getText().equals("\uE204");
                            if (isPressed) {
                                source.setText("\uE004"); source.setHoverText("\uE104");
                            } else {
                                source.setText("\uE204"); source.setHoverText("\uE304");
                            }
                        })
                        .build();
                if (index == 5) continue;
                consumableSlots.add(slot);
                HologramElement slotPriceTag = new HologramElement.Builder("consumable_slot_price_tag" + index, slot)
                        .position(posX+9,posY-2).size(11,5).layer(1)
                        .text(String.format("%03d", ThreadLocalRandom.current().nextInt(1000)))
                        .font(Font.TEXT)
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
        HologramElement purseAmountSpecialItemsTab = new HologramElement.Builder("purse_amount_special_items_tab", purseSpecialItemsTabBody)
                .position(28,72).size(19,5).layer(1)
                .text("09734")
                .font(Font.TEXT)
                .build();
        HologramElement readySpecialItemsTabBody = new HologramElement.Builder("ready_special_items_tab_body", specialItemsTabHeader)
                .position(19,103).size(38,20).layer(1)
                .text("\uE000").hoverText("\uE100")
                .font(Font.MENU)
                .button()
                .build();

        readyButtons.add(readySpecialItemsTabBody);

        List<HologramElement> specialItemsSlots = new ArrayList<>();
        List<String> specialItemsChoices = List.of("Arme","Profil","Aptitude","Atout");
        for (int x = 0; x <3; x++) {
            int posX = 68 + x * 43;
            int rarity = ThreadLocalRandom.current().nextInt(6,10);
            HologramElement specialItemsHeaderCard = new HologramElement.Builder("special_items_header_card_" + x, specialItemsTabHeader)
                    .position(posX-3, 75).size(36,13).layer(1)
                    .text(TextUtil.parse("E00"+rarity)).hoverText(TextUtil.parse("E10"+rarity))
                    .button()
                    .build();

            HologramElement specialItemsHeaderCardText = new HologramElement.Builder("special_items_header_card_text_" + x, specialItemsHeaderCard)
                    .position(posX,72).size(30,5).layer(1)
                    .text(specialItemsChoices.get(ThreadLocalRandom.current().nextInt(4)))
                    .font(Font.TEXT)
                    .build();
            HologramElement slot = new HologramElement.Builder("special_items_slot_" + x, specialItemsTabHeader)
                    .position(posX, 114).size(30,36).layer(1)
                    .text("\uE004").hoverText("\uE104")
                    .hasHint(true)
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
            HologramElement slotPriceTag = new HologramElement.Builder("special_item_slot_price_tag_" + x, slot)
                    .position(posX+9,112).size(11,5).layer(1)
                    .text(String.format("%03d", ThreadLocalRandom.current().nextInt(1000)))
                    .font(Font.TEXT)
                    .build();

        }

        shopGroup.show(consumableTabHeader);

        return shopScreen;
    }
}
