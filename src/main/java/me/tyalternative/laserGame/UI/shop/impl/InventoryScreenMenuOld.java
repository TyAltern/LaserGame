package me.tyalternative.laserGame.UI.shop.impl;

import me.tyalternative.laserGame.UI.shop.*;
import me.tyalternative.laserGame.utils.Font;
import me.tyalternative.laserGame.utils.TextUtil;
import me.tyalternative.laserGame.utils.TickUtil;

import java.util.ArrayList;
import java.util.List;

public class InventoryScreenMenuOld {

    private HologramElement inventoryScreen;
    private final List<HologramElement> readyButtons = new ArrayList<>();
    private final List<HologramElement> categoryHeaders = new ArrayList<>();
    private final List<HologramElement> categoryChoices = new ArrayList<>();
    private HologramElement dropDownSelection;
    private boolean isDropdownUnfold;
    private ScreenGroup dropDownGroup;
    private boolean isUnfoldingAnimationPlaying = false;
    private int scrollAnimationTime = 1;

    public InventoryScreenMenuOld(HologramElement parent) {
        this.inventoryScreen = createHologram(parent);
    }

    public HologramElement getHologram() { return inventoryScreen; }

    public List<HologramElement> getReadyButtons() { return readyButtons; }

    private HologramElement createHologram(HologramElement parent) {

        HologramElement inventoryScreen = new HologramElement.Builder("inventory_screen", parent)
                .position(81, 29).size(28, 18).layer(1)
                .font(Font.INVENTORY)
                .text("\uE2FF").hoverText("\uE3FF")
                .button()
                .build();

        HologramElement shopScreenButtonLink = new HologramElement.Builder("shop_screen_button_link_from_inventory", inventoryScreen)
                .position(110, 29).size(28, 18).layer(1)
                .font(Font.SHOP)
                .text("\uE0FF").hoverText("\uE1FF")
                .button()
                .build();
        HologramElement statsScreenButtonLink = new HologramElement.Builder("stats_screen_button_link_from_inventory", inventoryScreen)
                .position(139, 29).size(28, 18).layer(1)
                .font(Font.STATS)
                .text("\uE0FF").hoverText("\uE1FF")
                .button()
                .build();


        HologramElement readyButton = new HologramElement.Builder("ready_button_inventory", inventoryScreen)
                .position(25,84).size(38,20).layer(1)
                .text("\uE000").hoverText("\uE100").font(Font.MENU).dontPropagateFont(true)
                .button()
                .build();

        readyButtons.add(readyButton);

        // Drop down Menu

        HologramElement categoryDropDownButton = new  HologramElement.Builder("category_drop_down_button", inventoryScreen)
                .position(14,50).size(61,17).layer(6)
                .build();

        HologramElement consumablesCategory = new HologramElement.Builder("consumables_category", categoryDropDownButton)
                .position(14,50).size(61,17).layer(1)
                .text("\uE010").hoverText("\uE110")
                .button()
                .build();
        categoryHeaders.add(consumablesCategory);
        HologramElement weaponsCategory = new HologramElement.Builder("weapons_category", categoryDropDownButton)
                .position(14,50).size(61,17).layer(1)
                .text("\uE011").hoverText("\uE111")
                .button()
                .build();
        categoryHeaders.add(weaponsCategory);
        HologramElement profilesCategory = new HologramElement.Builder("profiles_category", categoryDropDownButton)
                .position(14,50).size(61,17).layer(1)
                .text("\uE012").hoverText("\uE112")
                .button()
                .build();
        categoryHeaders.add(profilesCategory);
        HologramElement skillsCategory = new HologramElement.Builder("skills_category", categoryDropDownButton)
                .position(14,50).size(61,17).layer(1)
                .text("\uE013").hoverText("\uE113")
                .button()
                .build();
        categoryHeaders.add(skillsCategory);
        HologramElement upgradesCategory = new HologramElement.Builder("upgrades_category", categoryDropDownButton)
                .position(14,50).size(61,17).layer(1)
                .text("\uE014").hoverText("\uE114")
                .button()
                .build();
        categoryHeaders.add(upgradesCategory);

        dropDownGroup = new ScreenGroup(categoryHeaders);

        dropDownSelection = new HologramElement.Builder("drop_down_selection", categoryDropDownButton)
                .position(0,137).size(212,138).layer(-2)
                .hideByDefault(true)
                .button()
                .build();


        HologramElement consumablesDropDownChoice = new HologramElement.Builder("consumables_drop_down_choice", dropDownSelection)
                .position(15,50).size(59,13).layer(1) // 64
                .text("\uE020").hoverText("\uE120")
                .button()
                .build();
        categoryChoices.add(consumablesDropDownChoice);
        HologramElement weaponsDropDownChoice = new HologramElement.Builder("weapons_drop_down_choice", dropDownSelection)
                .position(15,50).size(59,13).layer(1) // 78
                .text("\uE021").hoverText("\uE121")
                .button()
                .build();
        categoryChoices.add(weaponsDropDownChoice);
        HologramElement profilesDropDownChoice = new HologramElement.Builder("profiles_drop_down_choice", dropDownSelection)
                .position(15,50).size(59,13).layer(1) // 92
                .text("\uE022").hoverText("\uE122")
                .button()
                .build();
        categoryChoices.add(profilesDropDownChoice);
        HologramElement skillsDropDownChoice = new HologramElement.Builder("skills_drop_down_choice", dropDownSelection)
                .position(15,50).size(59,13).layer(1) // 106
                .text("\uE023").hoverText("\uE123")
                .button()
                .build();
        categoryChoices.add(skillsDropDownChoice);
        HologramElement upgradesDropDownChoice = new HologramElement.Builder("upgrades_drop_down_choice", dropDownSelection)
                .position(15,50).size(59,13).layer(1) // 120
                .text("\uE024").hoverText("\uE124")
                .button()
                .build();
        categoryChoices.add(upgradesDropDownChoice);

        dropDownSelection.onClick((player, source, clickType) -> {
            foldDropDown(dropDownGroup.getSelectedScreen());
        });

        for (HologramElement categoryHeader : categoryHeaders) {
            categoryHeader.onClick((player, source, clickType) -> {
                if (isDropdownUnfold) foldDropDown(categoryHeader);
                else unfoldDropDown();
            });
        }

        int counter = 0;
        for (HologramElement categoryChoice : categoryChoices) {

            int finalCounter = counter;
            categoryChoice.onClick((player, source, clickType) -> {
                foldDropDown(categoryHeaders.get(finalCounter));
            });
            counter++;
        }

        // STORAGE

        HologramElement storageContainer = new HologramElement.Builder("storage_container", inventoryScreen)
                .position(82, 96).size(115,66)
                .button()
                .build();

        List<HologramElement> storageElements = new ArrayList<>();
        List<HologramElement> storageSliders = new ArrayList<>();

        for (int i = 0; i < 3; i++) {

            HologramElement storageSliderSelected = new HologramElement.Builder("storage_slider_" + i, storageContainer)
                    .position(189, 48 + i * 17).size(5, 16).layer(1)
                    .text("\uE202").hoverText("\uE302")
                    .bubbleScrollToParent(true)
                    .button()
                    .build();

            storageSliders.add(storageSliderSelected);

            HologramElement storageSliderBack1 = new HologramElement.Builder("storage_slider_back_" + (i + 1) % 3 + "_from_" + i, storageSliderSelected)
                    .position(189, 48 + ((i + 1) % 3) * 17).size(5, 16).layer(1)
                    .text("\uE002").hoverText("\uE102")
                    .bubbleScrollToParent(true)
                    .button()
                    .build();
            HologramElement storageSliderBack2 = new HologramElement.Builder("storage_slider_back_" + (i + 2) % 3 + "_from_" + i, storageSliderSelected)
                    .position(189, 48 + ((i + 2) % 3) * 17).size(5, 16).layer(1)
                    .text("\uE002").hoverText("\uE102")
                    .bubbleScrollToParent(true)
                    .button()
                    .build();

            for (int y = 0; y < 2; y++) {
                int posY = 57 + y * 26;
                for (int x = 0; x < 4; x++) {
                    int posX = 84 + x * 26;
                    int index = x + y * 4;
                    HologramElement slot = new HologramElement.Builder("storage_slot_" + index, storageSliderSelected)
                            .position(posX, posY).size(26, 26).layer(1)
                            .text("\uE001").hoverText("\uE101")
                            .bubbleScrollToParent(true)
                            .hasHint(true)
                            .button()
                            .onClick((player, source, clickType) -> {
                                boolean isPressed = source.getText().equals("\uE201");
                                if (isPressed) {
                                    source.setText("\uE001");
                                    source.setHoverText("\uE101");
                                } else {
                                    source.setText("\uE201");
                                    source.setHoverText("\uE301");
                                }
                            })
                            .build();
                    storageElements.add(slot);

                }
            }
        }

        ScreenGroup storageGroup = new ScreenGroup(storageSliders);

        for (HologramElement storageSlider : storageSliders) {
            for (HologramElement child : storageSlider.getChildren()) {
                if (child == null) continue;
                if (child.getId().startsWith("storage_slider_back_0_from")) {
                    child.onClick(HologramActions.showInGroup(storageGroup, "storage_slider_0"));
                }
                if (child.getId().startsWith("storage_slider_back_1_from")) {
                    child.onClick(HologramActions.showInGroup(storageGroup, "storage_slider_1"));
                }
                if (child.getId().startsWith("storage_slider_back_2_from")) {
                    child.onClick(HologramActions.showInGroup(storageGroup, "storage_slider_2"));
                }
            }
        }

        storageGroup.show(storageSliders.getFirst());
        storageContainer.onScroll((player, source, scrollType) -> {
            HologramElement selectedScreen = storageGroup.getSelectedScreen();
            if (selectedScreen == null) { storageGroup.show(storageSliders.getFirst()); return; }
            String currentId = selectedScreen.getId();
            try {
                int index = Integer.parseInt(currentId.substring(currentId.length() - 1));
                index = scrollType == HologramScrollType.UP ? (index - 1 < 0 ? 2 : index - 1) % 3 : (index + 1) % 3;
                storageGroup.show("storage_slider_" + index);
            } catch (Exception e) {
                storageGroup.show(storageSliders.getFirst());
            }
        });

        // CONSUMABLES TAB

        List<HologramElement> equippedConsumableSlots = new ArrayList<>();

        for (int x = 0; x < 5; x++) { // TODO : CHANGE TO ACCEPT ONE MORE SLOT (16 + x * 30)
            int posX = 31 + x * 30;
            HologramElement slot = new HologramElement.Builder("equipped_consumable_item_slot_" + x, consumablesCategory)
                    .position(posX, 127).size(30,30).layer(-4)
                    .text("\uE000").hoverText("\uE100").font(Font.INVENTORY)
                    .hasHint(true)
                    .button()
                    .build();

            equippedConsumableSlots.add(slot);
        }

        // WEAPONS TAB

        HologramElement equippedWeaponSlot = new HologramElement.Builder("equipped_weapon_slot", weaponsCategory)
                .position(121, 127).size(30,30).layer(-4)
                .text("\uE000").hoverText("\uE100").font(Font.INVENTORY)
                .hasHint(true)
                .button()
                .build();

        // PROFILES TAB

        HologramElement equippedProfileSlot = new HologramElement.Builder("equipped_profile_slot", profilesCategory)
                .position(121, 127).size(30,30).layer(-4)
                .text("\uE000").hoverText("\uE100").font(Font.INVENTORY)
                .hasHint(true)
                .button()
                .build();

        // SKILLS TAB

        HologramElement equippedSkillSlot = new HologramElement.Builder("equipped_skill_slot", skillsCategory)
                .position(121, 127).size(30,30).layer(-4)
                .text("\uE000").hoverText("\uE100").font(Font.INVENTORY)
                .hasHint(true)
                .button()
                .build();




        dropDownGroup.show(consumablesCategory);
        return inventoryScreen;
    }


    private void unfoldDropDown() {

        HologramElement categoryHeader = dropDownGroup.getSelectedScreen();
        if (categoryHeader == null) return;
        int index = categoryHeaders.indexOf(categoryHeader);

        isUnfoldingAnimationPlaying = true;

        categoryHeader.setText(TextUtil.parse("E21" + index));
        categoryHeader.setHoverText(TextUtil.parse("E31" + index));

        for (int i = 4; i >= 0; i--) {
            HologramElement choice = categoryChoices.get(i);
            int delay = (4-i) * scrollAnimationTime;
            int duration = (i + 1) * scrollAnimationTime;
            if (delay == 0) choice.setYTranslation((-14 - i *14) * 0.025, duration-1);
            else {
                int finalI = i;
                TickUtil.delay(() -> choice.setYTranslation((-14 - finalI * 14) * 0.025, duration), delay);
            }
        }
        TickUtil.delay(() -> isUnfoldingAnimationPlaying = false,5 * scrollAnimationTime);

        dropDownSelection.setVisible(true);

        isDropdownUnfold = true;
    }

    private void foldDropDown(HologramElement selected) {
        if (selected == null) selected = dropDownGroup.getSelectedScreen();
        if (selected == null) return;
        int index = categoryHeaders.indexOf(selected);

        selected.setText(TextUtil.parse("E01" + index));
        selected.setHoverText(TextUtil.parse("E11" + index));

        for (int i = 4; i >= 0; i--) {
            HologramElement choice = categoryChoices.get(i);
//            int delay = (4-i) * scrollAnimationTime;
            int duration = (i + 1) * scrollAnimationTime;
            choice.setYTranslation(0, duration);
//            else {
//                TickUtil.delay(() -> choice.setYTranslation(0, duration), delay);
//            }
        }

        TickUtil.delay(() -> {
            if (!isUnfoldingAnimationPlaying) dropDownSelection.setVisible(false);
        },6*scrollAnimationTime);

        dropDownGroup.show(selected);

        isDropdownUnfold = false;
    }
}
