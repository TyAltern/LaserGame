package me.tyalternative.laserGame.UI.shop.impl;

import me.tyalternative.laserGame.UI.shop.*;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.shop.*;
import me.tyalternative.laserGame.utils.Font;
import me.tyalternative.laserGame.utils.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShopScreenMenu {

    private static final String SLOT_EMPTY = "\uE005";
    private static final String SLOT_EMPTY_HOVER = "\uE105";
    private static final String SLOT_AVAILABLE = "\uE004";
    private static final String SLOT_AVAILABLE_HOVER = "\uE104";

    private HologramElement shopScreen;
    private final List<HologramElement> readyButtons = new ArrayList<>();

    private HologramElement purseAmountConsumableTab;
    private HologramElement purseAmountSpecialItemsTab;
    private HologramElement rerollAmountConsumableTab;

    private final List<HologramElement> consumableSlots = new ArrayList<>();
    private final List<HologramElement> consumableSlotPriceTags = new ArrayList<>();
    private final List<HologramElement> consumableSlotIcons = new ArrayList<>();

    private final List<HologramElement> specialItemsSlots = new ArrayList<>();
    private final List<HologramElement> specialItemsSlotPriceTags = new ArrayList<>();
    private final List<HologramElement> specialItemsSlotIcons = new ArrayList<>();
    private final List<HologramElement> specialItemsHeaderCards = new ArrayList<>();
    private final List<HologramElement> specialItemsHeaderCardTexts = new ArrayList<>();
    private final List<SpecialSlotType> specialSlotCurrentTypes = new ArrayList<>();

    private GamePlayer gamePlayer;
    private ShopContext shopContext;
    private ItemHintPanel hintPanel;
    private final Runnable onChange;

    public ShopScreenMenu(HologramElement parent, Runnable onChange) {
        this.onChange = onChange;
        this.shopScreen = createHologram(parent);
    }

    public HologramElement getHologram() { return shopScreen; }

    public List<HologramElement> getReadyButtons() { return readyButtons; }

    public void refresh(GamePlayer gp, ShopContext ctx, ItemHintPanel hintPanel) {
        this.gamePlayer = gp;
        this.shopContext = ctx;
        this.hintPanel = hintPanel;

        purseAmountConsumableTab.setText(formatMoney(gp.getCurrency()));
        purseAmountSpecialItemsTab.setText(formatMoney(gp.getCurrency()));

        ShopSession session = gp.getShopSession();
        if (session == null) {
            rerollAmountConsumableTab.setText("0000");
            refreshConsumableSlots(null);
            refreshSpecialSlots(null);
            return;
        }

        rerollAmountConsumableTab.setText(String.format("%04d", ctx.shopManager().getConsumableRerollCost(session)));
        refreshConsumableSlots(session);
        refreshSpecialSlots(session);
    }

    private void refreshConsumableSlots(ShopSession session) {
        List<String> ids = session == null ? List.of() : session.getConsumableSlotIds();

        for (int i = 0; i < consumableSlots.size(); i++) {
            HologramElement slot = consumableSlots.get(i);
            HologramElement priceTag = consumableSlotPriceTags.get(i);
            HologramElement icon = consumableSlotIcons.get(i);

            String id = i < ids.size() ? ids.get(i) : null;
            if (id == null) {
                setSlotEmpty(slot, priceTag, icon);
                continue;
            }

            Optional<ConsumableDefinition> defOpt = shopContext.consumableManager().getDefinition(id);
            if(defOpt.isEmpty()) {
                setSlotEmpty(slot, priceTag, icon);
                continue;
            }
            ConsumableDefinition def = defOpt.get();

            slot.setText(SLOT_AVAILABLE);
            slot.setHoverText(SLOT_AVAILABLE_HOVER);
            priceTag.setText(formatPrice(def.price()));
            icon.setText(def.glyph() == null ? "" : def.glyph());
            hintPanel.setHint(slot.getId(), def.displayName(), describe(def.category().name(), def.rarity(), def.price()));
        }
    }

    private void refreshSpecialSlots(ShopSession session) {
        specialSlotCurrentTypes.clear();
        if (session != null) {
            specialSlotCurrentTypes.addAll(session.getSpecialSlotIds().keySet());
        }

        for (int i = 0; i < specialItemsSlots.size(); i++) {
            HologramElement slot = specialItemsSlots.get(i);
            HologramElement priceTag = specialItemsSlotPriceTags.get(i);
            HologramElement icon = specialItemsSlotIcons.get(i);
            HologramElement headerCard = specialItemsHeaderCards.get(i);
            HologramElement headerCardText = specialItemsHeaderCardTexts.get(i);

            if (session == null || i >= specialSlotCurrentTypes.size()) {
                setSlotEmpty(slot, priceTag, icon);
                headerCardText.setText("");
                continue;
            }

            SpecialSlotType type = specialSlotCurrentTypes.get(i);
            String id = session.getSpecialSlotIds().get(type);
            headerCardText.setText(labelFor(type));

            if (id == null) {
                setSlotEmpty(slot, priceTag, icon);
                continue;
            }

            Optional<SpecialItemView> viewOpt = resolveSpecialItem(type, id);
            if (viewOpt.isEmpty()) {
                setSlotEmpty(slot, priceTag, icon);
                continue;
            }
            SpecialItemView view = viewOpt.get();

            slot.setText(SLOT_AVAILABLE);
            slot.setHoverText(SLOT_AVAILABLE_HOVER);
            priceTag.setText(formatPrice(view.price()));
            icon.setText(view.glyph() == null ? "" : view.glyph());
            int rarityGlyph = 6 + view.rarity().ordinal();
            headerCard.setText(TextUtil.parse("E00" + rarityGlyph));
            headerCard.setHoverText(TextUtil.parse("E10" + rarityGlyph));
            hintPanel.setHint(slot.getId(), view.displayName(), describe(labelFor(type), view.rarity(), view.price()));
        }
    }

    private void setSlotEmpty(HologramElement slot, HologramElement priceTag, HologramElement icon) {
        slot.setText(SLOT_EMPTY);
        slot.setHoverText(SLOT_EMPTY_HOVER);
        priceTag.setText("");
        icon.setText("");
        hintPanel.clearHint(slot.getId());
    }

    private record SpecialItemView(String displayName, Rarity rarity, int price, String glyph) {}

    private Optional<SpecialItemView> resolveSpecialItem(SpecialSlotType type, String id) {
        return switch (type) {
            case WEAPON -> shopContext.weaponManager().getWeapon(id)
                    .map(w -> new SpecialItemView(w.displayName(), w.rarity(), w.price(), w.glyph()));
            case UPGRADE -> shopContext.upgradeManager().getDefinition(id)
                    .map(d -> new SpecialItemView(d.displayName(), d.rarity(), d.price(), d.glyph()));
            case SKILL -> shopContext.skillManager().getDefinition(id)
                    .map(d -> new SpecialItemView(d.displayName(), d.rarity(), d.price(), d.glyph()));
            case ARCHETYPE -> shopContext.archetypeManager().getDefinition(id)
                    .map(d -> new SpecialItemView(d.displayName(), d.rarity(), d.price(), d.glyph()));
        };
    }

    private String labelFor(SpecialSlotType type) {
        return switch (type) {
            case WEAPON -> "Arme";
            case UPGRADE -> "Atout";
            case SKILL -> "Aptitude";
            case ARCHETYPE -> "Profil";
        };
    }

    private String describe(String category, Rarity rarity, int price) {
        return category + " - " + rarity.name() + " - " + price + "$";
    }

    private String formatPrice(int price) {
        return String.format("%03d", Math.min(999, price));
    }

    private String formatMoney(int amount) {
        return String.format("%05d", Math.min(99999, amount));
    }

    private HologramElement createHologram(HologramElement parent) {

        HologramElement shopScreen = new HologramElement.Builder("shop_screen", parent)
                .position(110, 29).size(28,18).layer(1)
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
                .position(109, 48).size(86, 14).layer(1)
                .text("\uE001").hoverText("\uE101")
                .button()
                .onClick(HologramActions.showInGroup(shopGroup, specialItemsTabHeader))
                .build();
        HologramElement purseConsumableTabBody = new HologramElement.Builder("purse_consumable_tab_body", consumableTabHeader)
                .position(24,64).size(27,11).layer(1)
                .text("\uE00A").hoverText("\uE10A")
                .button()
                .build();
        purseAmountConsumableTab = new HologramElement.Builder("purse_amount_consumable_tab", purseConsumableTabBody)
                .position(28,62).size(19,5).layer(1)
                .text("00000")
                .font(Font.TEXT)
                .build();
        HologramElement rerollConsumableTabBody = new HologramElement.Builder("reroll_consumable_tab_body", consumableTabHeader)
                .position(16,99).size(43,32).layer(1)
                .text("\uE003").hoverText("\uE103")
                .button()
                .build();
        rerollAmountConsumableTab = new HologramElement.Builder("reroll_amount_consumable_tab", rerollConsumableTabBody)
                .position(30,92).size(15,5).layer(1)
                .text("0000")
                .font(Font.TEXT)
                .build();
        rerollConsumableTabBody.onClick((player, source, clickType) -> {
            if (gamePlayer == null || shopContext == null) return;
            ShopSession session = gamePlayer.getShopSession();
            if (session == null) return;
            shopContext.shopManager().rerollConsumables(gamePlayer, session);
            if (onChange != null) onChange.run();
        });
        HologramElement readyConsumableTabBody = new HologramElement.Builder("ready_consumable_tab_body", consumableTabHeader)
                .position(19,123).size(38,20).layer(1)
                .text("\uE000").hoverText("\uE100").font(Font.MENU)
                .button()
                .build();
        readyButtons.add(readyConsumableTabBody);

        for (int y = 0; y < 2; y++) {
            int posY = 87 + y * 38;
            for (int x = 0; x < 3; x++) {
                int posX = 68 + x * 43;
                int index = x + y * 3;
                HologramElement slot = new HologramElement.Builder("consumable_slot_" + index, consumableTabHeader)
                        .position(posX, posY).size(30, 36).layer(1)
                        .text(SLOT_EMPTY).hoverText(SLOT_EMPTY_HOVER)
                        .hasHint(true)
                        .button()
                        .build();
                consumableSlots.add(slot);
                HologramElement slotPriceTag = new HologramElement.Builder("consumable_slot_price_tag" + index, slot)
                        .position(posX + 9, posY - 2).size(11, 5).layer(2)
                        .text("")
                        .font(Font.TEXT)
                        .build();
                consumableSlotPriceTags.add(slotPriceTag);
                HologramElement slotIcon = new HologramElement.Builder("consumable_slot_icon_" + index, slot)
                        .position(posX+6, posY-13).size(16, 16).layer(1)
                        .text("")
                        .font(Font.ITEMS)
                        .build();
                consumableSlotIcons.add(slotIcon);

                final int slotIndex = index;
                slot.onClick((player, source, clickType) -> {
                    if (gamePlayer == null || shopContext == null) return;
                    ShopSession session = gamePlayer.getShopSession();
                    if (session == null) return;
                    List<String> ids = session.getConsumableSlotIds();
                    if (slotIndex >= ids.size() || ids.get(slotIndex) == null) return;
                    shopContext.shopManager().purchaseConsumableSlot(gamePlayer, session, slotIndex);
                    if (onChange != null) onChange.run();
                });
            }
        }

        // Special Ietms Hologram

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
        purseAmountSpecialItemsTab = new HologramElement.Builder("purse_amount_special_items_tab", purseSpecialItemsTabBody)
                .position(28,72).size(19,5).layer(1)
                .text("00000")
                .font(Font.TEXT)
                .build();
        HologramElement readySpecialItemsTabBody = new HologramElement.Builder("ready_special_items_tab_body", specialItemsTabHeader)
                .position(19,103).size(38,20).layer(1)
                .text("\uE000").hoverText("\uE100")
                .font(Font.MENU)
                .button()
                .build();

        readyButtons.add(readySpecialItemsTabBody);

        for (int x = 0; x <3; x++) {
            int posX = 68 + x * 43;
            HologramElement specialItemsHeaderCard = new HologramElement.Builder("special_items_header_card_" + x, specialItemsTabHeader)
                    .position(posX - 3, 75).size(36, 13).layer(1)
                    .text(TextUtil.parse("E006")).hoverText(TextUtil.parse("E106"))
                    .button()
                    .build();
            specialItemsHeaderCards.add(specialItemsHeaderCard);

            HologramElement specialItemsHeaderCardText = new HologramElement.Builder("special_items_header_card_text_" + x, specialItemsHeaderCard)
                    .position(posX, 72).size(30, 5).layer(1)
                    .text("")
                    .font(Font.TEXT)
                    .build();
            specialItemsHeaderCardTexts.add(specialItemsHeaderCardText);
            HologramElement slot = new HologramElement.Builder("special_items_slot_" + x, specialItemsTabHeader)
                    .position(posX, 114).size(30, 36).layer(1)
                    .text(SLOT_EMPTY).hoverText(SLOT_EMPTY_HOVER)
                    .hasHint(true)
                    .button()
                    .build();
            specialItemsSlots.add(slot);

            HologramElement slotPriceTag = new HologramElement.Builder("special_item_slot_price_tag_" + x, slot)
                    .position(posX + 9, 112).size(11, 5).layer(2)
                    .text("")
                    .font(Font.TEXT)
                    .build();
            specialItemsSlotPriceTags.add(slotPriceTag);
            HologramElement slotIcon = new HologramElement.Builder("special_items_slot_icon_" + x, slot)
                    .position(posX+6, 101).size(16, 16).layer(1)
                    .text("")
                    .font(Font.ITEMS)
                    .build();
            specialItemsSlotIcons.add(slotIcon);

            final int slotIndex = x;
            slot.onClick((player, source, clickType) -> {
                if (gamePlayer == null || shopContext == null) return;
                ShopSession session = gamePlayer.getShopSession();
                if (session == null) return;
                if (slotIndex >= specialSlotCurrentTypes.size()) return;
                SpecialSlotType type = specialSlotCurrentTypes.get(slotIndex);
                if (type == null || session.getSpecialSlotIds().get(type) == null) return;
                shopContext.shopManager().purchaseSpecialSlot(gamePlayer, session, type);
                if (onChange != null) onChange.run();
            });
        }

        shopGroup.show(consumableTabHeader);

        return  shopScreen;
    }
}