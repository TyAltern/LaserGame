package me.tyalternative.laserGame.UI.shop.impl;

import me.tyalternative.laserGame.UI.shop.*;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.shop.ConsumableDefinition;
import me.tyalternative.laserGame.shop.Rarity;
import me.tyalternative.laserGame.shop.ShopContext;
import me.tyalternative.laserGame.utils.Font;
import me.tyalternative.laserGame.utils.TextUtil;
import me.tyalternative.laserGame.utils.TickUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryScreenMenu {

    private static final int CATEGORY_CONSUMABLES = 0;
    private static final int CATEGORY_WEAPONS = 1;
    private static final int CATEGORY_PROFILES = 2;
    private static final int CATEGORY_SKILLS = 3;
    private static final int CATEGORY_UPGRADES = 4;

    private static final String IDLE = "\uE000";
    private static final String IDLE_HOVER = "\uE100";
    private static final String FILLED = "\uE001";
    private static final String FILLED_HOVER = "\uE101";
    private static final String SELECTED = "\uE201";
    private static final String SELECTED_HOVER = "\uE301";
    private static final String EQUIPPED_SELECTED = "\uE200";
    private static final String EQUIPPED_SELECTED_HOVER = "\uE300";

    private HologramElement inventoryScreen;
    private final List<HologramElement> readyButtons = new ArrayList<>();
    private final List<HologramElement> categoryHeaders = new ArrayList<>();
    private final List<HologramElement> categoryChoices = new ArrayList<>();
    private HologramElement dropDownSelection;
    private boolean isDropdownUnfold;
    private ScreenGroup dropDownGroup;
    private boolean isUnfoldingAnimationPlaying = false;
    private int scrollAnimationTime = 1;

    private HologramElement storageContainer;
    private final List<HologramElement> storageElements = new ArrayList<>();
    private final List<HologramElement> storageElementIcons = new ArrayList<>();
    private final List<String> storageSlotItemIds = new ArrayList<>();
    private final List<HologramElement> equippedConsumableSlots = new ArrayList<>();
    private final List<HologramElement> equippedConsumableIcons = new ArrayList<>();
    private HologramElement equippedWeaponSlot;
    private HologramElement equippedWeaponIcon;
    private HologramElement equippedProfileSlot;
    private HologramElement equippedProfileIcon;
    private HologramElement equippedSkillSlot;
    private HologramElement equippedSkillIcon;

    private GamePlayer gamePlayer;
    private ShopContext shopContext;
    private ItemHintPanel hintPanel;
    private final Runnable onChange;

    private Integer selectedStorageIndex;
    private String selectedStorageItemId;
    private boolean equippedSelectionActive;
    private HologramElement equippedSelectionSlot;

    private Integer selectedConsumableStorageIndex;
    private Integer selectedConsumableEquippedIndex;
    private final List<String> consumableStorageItemIds = new ArrayList<>();

    public InventoryScreenMenu(HologramElement parent, Runnable onChange) {
        this.onChange = onChange;
        this.inventoryScreen = createHologram(parent);
    }

    public HologramElement getHologram() { return inventoryScreen; }

    public List<HologramElement> getReadyButtons() { return readyButtons; }

    public void refresh(GamePlayer gp, ShopContext ctx, ItemHintPanel hintPanel) {
        this.gamePlayer = gp;
        this.shopContext = ctx;
        this.hintPanel = hintPanel;

        clearSelections();
        refreshEquippedSlots();
        refreshStorage();
    }

    private int currentCategoryIndex() {
        HologramElement selected = dropDownGroup.getSelectedScreen();
        return categoryHeaders.indexOf(selected);
    }

    public void clearSelections() {
        selectedStorageIndex = null;
        selectedStorageItemId = null;
        equippedSelectionActive = false;
        equippedSelectionSlot = null;
        selectedConsumableStorageIndex = null;
        selectedConsumableEquippedIndex = null;
    }

    private void refreshEquippedSlots() {
        List<String> held = gamePlayer.getConsumables().getSlotIds();
        for (int i = 0; i < equippedConsumableSlots.size(); i++) {
            HologramElement slot = equippedConsumableSlots.get(i);
            HologramElement icon = equippedConsumableIcons.get(i);
            boolean selected = selectedConsumableEquippedIndex != null && selectedConsumableEquippedIndex == i;

            String id = i < held.size() ? held.get(i) : null;
            if (id == null) {
                slot.setText(selected ? EQUIPPED_SELECTED : IDLE);
                slot.setHoverText(selected ? EQUIPPED_SELECTED_HOVER : IDLE_HOVER);
                icon.setText("");
                hintPanel.clearHint(slot.getId());
                continue;
            }
            Optional<ConsumableDefinition> defOpt = shopContext.consumableManager().getDefinition(id);
            slot.setText(selected ? EQUIPPED_SELECTED : IDLE);
            slot.setHoverText(selected ? EQUIPPED_SELECTED_HOVER : IDLE_HOVER);
            if (defOpt.isPresent()) {
                ConsumableDefinition def = defOpt.get();
                icon.setText(def.glyph() == null ? "" : def.glyph());
                hintPanel.setHint(slot.getId(), def.displayName(), def.description(), def.statModification());
            } else {
                icon.setText("");
                hintPanel.clearHint(slot.getId());
            }
        }

        String weaponId = gamePlayer.getWeaponType().id();
        equippedWeaponSlot.setText(IDLE);
        equippedWeaponSlot.setHoverText(IDLE_HOVER);
        shopContext.weaponManager().getWeapon(weaponId).ifPresentOrElse(w -> {
                    equippedWeaponIcon.setText(w.glyph() == null ? "" : w.glyph());
                    hintPanel.setHint(equippedWeaponSlot.getId(), w.displayName(), w.description(), w.statModification());
                },
                () -> {
                    equippedWeaponIcon.setText("");
                    hintPanel.clearHint(equippedWeaponSlot.getId());
                });

        boolean profileSelected = equippedSelectionActive && equippedSelectionSlot == equippedProfileSlot;
        equippedProfileSlot.setText(profileSelected ? EQUIPPED_SELECTED : IDLE);
        equippedProfileSlot.setHoverText(profileSelected ? EQUIPPED_SELECTED_HOVER : IDLE_HOVER);
        String archetypeId = gamePlayer.getEquippedArchetypeId();
        if (archetypeId == null) {
            equippedProfileIcon.setText("");
            hintPanel.clearHint(equippedProfileSlot.getId());
        } else {
            shopContext.archetypeManager().getDefinition(archetypeId).ifPresentOrElse(d -> {
                        equippedProfileIcon.setText(d.glyph() == null ? "" : d.glyph());
                        hintPanel.setHint(equippedProfileSlot.getId(), d.displayName(), d.description(), d.statModification());
                    },
                    () -> {
                        equippedProfileIcon.setText("");
                        hintPanel.clearHint(equippedProfileSlot.getId());
                    });
        }

        boolean skillSelected = equippedSelectionActive && equippedSelectionSlot == equippedSkillSlot;
        equippedSkillSlot.setText(skillSelected ? EQUIPPED_SELECTED : IDLE);
        equippedSkillSlot.setHoverText(skillSelected ? EQUIPPED_SELECTED_HOVER : IDLE_HOVER);
        String skillId = gamePlayer.getEquippedSkillId();
        if (skillId == null) {
            equippedSkillIcon.setText("");
            hintPanel.clearHint(equippedSkillSlot.getId());
        } else {
            shopContext.skillManager().getDefinition(skillId).ifPresentOrElse(d -> {
                        equippedSkillIcon.setText(d.glyph() == null ? "" : d.glyph());
                        hintPanel.setHint(equippedSkillSlot.getId(), d.displayName(), d.description(), d.statModification());
                    },
                    () -> {
                        equippedSkillIcon.setText("");
                        hintPanel.clearHint(equippedSkillSlot.getId());
                    });
        }
    }

    private record StorageItem(String id, String displayName, String glyph, String category, Rarity rarity,
                               String description, List<String> statModification) {}

    private List<StorageItem> ownedItemsForCategory(int category) {
        List<StorageItem> items = new ArrayList<>();
        switch (category) {
            case CATEGORY_CONSUMABLES -> {
                for (String id : gamePlayer.getConsumableStorage()) {
                    shopContext.consumableManager().getDefinition(id).ifPresent(def ->
                            items.add(new StorageItem(id, def.displayName(), def.glyph(), def.category().name(),
                                    def.rarity(), def.description(), def.statModification())));
                }
            }
            case CATEGORY_WEAPONS -> {
                String equippedId = gamePlayer.getWeaponType().id();
                for (String id : gamePlayer.getOwnedWeaponIds()) {
                    if (id.equals(equippedId)) continue; // déjà équipée : ne pas la doubler dans le storage
                    shopContext.weaponManager().getWeapon(id).ifPresent(w ->
                            items.add(new StorageItem(id, w.displayName(), w.glyph(), "Arme", w.rarity(),
                                    w.description(), w.statModification())));
                }
            }
            case CATEGORY_PROFILES -> {
                String equippedId = gamePlayer.getEquippedArchetypeId();
                for (String id : gamePlayer.getOwnedArchetypeIds()) {
                    if (id.equals(equippedId)) continue;
                    shopContext.archetypeManager().getDefinition(id).ifPresent(d ->
                            items.add(new StorageItem(id, d.displayName(), d.glyph(), "Profil", d.rarity(),
                                    d.description(), d.statModification())));
                }
            }
            case CATEGORY_SKILLS -> {
                String equippedId = gamePlayer.getEquippedSkillId();
                for (String id : gamePlayer.getOwnedSkillIds()) {
                    if (id.equals(equippedId)) continue;
                    shopContext.skillManager().getDefinition(id).ifPresent(d ->
                            items.add(new StorageItem(id, d.displayName(), d.glyph(), "Aptitude", d.rarity(),
                                    d.description(), d.statModification())));
                }
            }
            case CATEGORY_UPGRADES -> {
                // Toutes les améliorations possédées sont actives simultanément : pas d'équipement, juste la liste.
                for (String id : gamePlayer.getOwnedUpgradeIds()) {
                    shopContext.upgradeManager().getDefinition(id).ifPresent(d ->
                            items.add(new StorageItem(id, d.displayName(), d.glyph(), "Atout", d.rarity(),
                                    d.description(), d.statModification())));
                }
            }
            default -> { }
        }
        return items;
    }

    private boolean categorySupportEquip(int category) {
        return category == CATEGORY_WEAPONS || category == CATEGORY_PROFILES || category == CATEGORY_SKILLS || category == CATEGORY_CONSUMABLES;
    }


    private void refreshStorage() {
        int category = currentCategoryIndex();
        List<StorageItem> items = category < 0 ? List.of() : ownedItemsForCategory(category);
        boolean canEquip = categorySupportEquip(category);
        boolean isConsumables = category == CATEGORY_CONSUMABLES;

        storageSlotItemIds.clear();
        consumableStorageItemIds.clear();

        for (int i = 0; i < storageElements.size(); i++) {
            HologramElement slot = storageElements.get(i);
            HologramElement icon = storageElementIcons.get(i);

            if (i >= items.size()) {
                storageSlotItemIds.add(null);
                consumableStorageItemIds.add(null);
                slot.setText(FILLED);
                slot.setHoverText(FILLED_HOVER);
                icon.setText("");
                hintPanel.clearHint(slot.getId());
                continue;
            }

            StorageItem item = items.get(i);
            storageSlotItemIds.add(item.id());
            consumableStorageItemIds.add(item.id());

            boolean selected = canEquip && (isConsumables ?
                    selectedConsumableStorageIndex != null && selectedConsumableStorageIndex == i
                    : selectedStorageIndex != null && selectedStorageIndex == i);
            slot.setText(selected ? SELECTED : FILLED);
            slot.setHoverText(selected ? SELECTED_HOVER : FILLED_HOVER);
            icon.setText(item.glyph() == null ? "" : item.glyph());
            hintPanel.setHint(slot.getId(), item.displayName(), item.description(), item.statModification());
        }
    }

    /** Construit le texte du hint : ligne d'en-tête (catégorie/rareté) + description + modification de stats. */
    private String buildHint(String category, Rarity rarity, String description, List<String> statModification) {
        StringBuilder sb = new StringBuilder();
        sb.append(category).append(" - ").append(rarity.name());
        if (description != null && !description.isBlank()) {
            sb.append("  ").append(description);
        }
        if (statModification != null && !statModification.isEmpty()) {
            sb.append("  [").append(statModification).append("]");
        }
        return sb.toString();
    }

    private void onStorageSlotClick(int index) {
        if (gamePlayer == null) return;
        int category = currentCategoryIndex();

        if (category == CATEGORY_CONSUMABLES) {
            onConsumableStorageClicked(index);
            return;
        }

        if (equippedSelectionActive) {
            performUnequip();
            return;
        }

        if (!categorySupportEquip(category)) return;
        if (index >= storageSlotItemIds.size()) return;
        String itemId = storageSlotItemIds.get(index);
        if (itemId == null) return;

        if (selectedStorageIndex != null && selectedStorageIndex == index) {
            selectedStorageIndex = null;
            selectedStorageItemId = null;
        } else {
            selectedStorageIndex = index;
            selectedStorageItemId = itemId;
        }
        refreshStorage();
    }

    // CONSOMMABLES

    private void onConsumableStorageClicked(int storageIndex) {
        if (selectedConsumableEquippedIndex != null) {
            boolean changed = gamePlayer.unequipConsumableToStorage(selectedConsumableEquippedIndex);
            selectedConsumableEquippedIndex = null;
            if (changed && onChange != null) onChange.run();
            else { refreshEquippedSlots(); refreshStorage(); }
            return;
        }

        if (storageIndex >= consumableStorageItemIds.size() || consumableStorageItemIds.get(storageIndex) == null) return;

        if (selectedConsumableStorageIndex != null && selectedConsumableStorageIndex == storageIndex) {
            selectedConsumableStorageIndex = null; // re-click donc on retire
        } else {
            selectedConsumableStorageIndex = storageIndex;
        }
        refreshStorage();
    }

    private void onEquippedConsumableSlotClicked(int equippedIndex) {
        if (gamePlayer == null || currentCategoryIndex() != CATEGORY_CONSUMABLES) return;

        if (selectedConsumableStorageIndex != null) {
            boolean changed = gamePlayer.equipConsumableFromStorage(selectedConsumableStorageIndex, equippedIndex);
            selectedConsumableStorageIndex = null;
            if (changed && onChange != null) onChange.run();
            else { refreshEquippedSlots(); refreshStorage(); }
            return;
        }

        if (selectedConsumableEquippedIndex != null) {
            if (selectedConsumableEquippedIndex == equippedIndex) {
                selectedConsumableEquippedIndex = null;
                refreshEquippedSlots();
                return;
            }
            boolean changed = gamePlayer.swapEquippedConsumables(selectedConsumableEquippedIndex, equippedIndex);
            selectedConsumableEquippedIndex = null;
            if (changed && onChange != null) onChange.run();
            else refreshEquippedSlots();
            return;
        }

        List<String> held = gamePlayer.getConsumables().getSlotIds();
        if (equippedIndex >= held.size() || held.get(equippedIndex) == null) return;
        selectedConsumableEquippedIndex = equippedIndex;
        refreshEquippedSlots();
    }

    private void performUnequip() {
        int category = currentCategoryIndex();
        boolean changed = switch (category) {
            case CATEGORY_PROFILES -> shopContext.shopManager().equipArchetype(gamePlayer, null);
            case CATEGORY_SKILLS -> shopContext.shopManager().equipSkill(gamePlayer, null);
            default -> false;
        };
        clearSelections();
        if (changed && onChange != null) onChange.run();
        else refreshEquippedSlots();
    }

    private void onEquippedWeaponSlotClicked() {
        if (gamePlayer == null || currentCategoryIndex() != CATEGORY_WEAPONS) return;
        if (selectedStorageItemId == null) return;
        boolean ok = shopContext.shopManager().equipWeapon(gamePlayer, selectedStorageItemId);
        clearSelections();
        if (ok && onChange != null) onChange.run();
        else refreshEquippedSlots();
    }

    private void onEquippedProfileSlotClicked() {
        if (gamePlayer == null || currentCategoryIndex() != CATEGORY_PROFILES) return;
        if (selectedStorageItemId != null) {
            boolean ok = shopContext.shopManager().equipArchetype(gamePlayer, selectedStorageItemId);
            clearSelections();
            if (ok && onChange != null) onChange.run();
            else refreshEquippedSlots();
            return;
        }
        if (gamePlayer.getEquippedArchetypeId() == null) return;
        toggleEquippedSelection(equippedProfileSlot);
    }

    private void onEquippedSkillSlotClicked() {
        if (gamePlayer == null || currentCategoryIndex() != CATEGORY_SKILLS) return;
        if (selectedStorageItemId != null) {
            boolean ok = shopContext.shopManager().equipSkill(gamePlayer, selectedStorageItemId);
            clearSelections();
            if (ok && onChange != null) onChange.run();
            else refreshEquippedSlots();
            return;
        }
        if (gamePlayer.getEquippedSkillId() == null) return;
        toggleEquippedSelection(equippedSkillSlot);
    }

    private void toggleEquippedSelection(HologramElement slot) {
        if (equippedSelectionActive && equippedSelectionSlot == slot) {
            equippedSelectionActive = false;
            equippedSelectionSlot = null;
        } else {
            equippedSelectionActive = true;
            equippedSelectionSlot = slot;
        }
        refreshEquippedSlots();
    }

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

        storageContainer = new HologramElement.Builder("storage_container", inventoryScreen)
                .position(82, 96).size(115,66)
                .button()
                .build();
        storageContainer.onClick((player, source, clickType) -> {
            if (equippedSelectionActive) performUnequip();
            if (selectedConsumableEquippedIndex != null) {
                boolean changed = gamePlayer.unequipConsumableToStorage(selectedConsumableEquippedIndex);
                selectedConsumableEquippedIndex = null;
                if (changed && onChange != null) onChange.run();
                else { refreshEquippedSlots(); refreshStorage(); }
            }
        });

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
                    int indexInPage = x + y * 4;
                    // id qualifié par page (indexInPage seul collisionnerait entre les 3 pages pour les hints).
                    HologramElement slot = new HologramElement.Builder("storage_slot_" + i + "_" + indexInPage, storageSliderSelected)
                            .position(posX, posY).size(26, 26).layer(1)
                            .text(FILLED).hoverText(FILLED_HOVER)
                            .bubbleScrollToParent(true)
                            .hasHint(true)
                            .button()
                            .build();
                    storageElements.add(slot);
                    HologramElement slotIcon = new HologramElement.Builder("storage_slot_icon_" + i + "_" + indexInPage, slot)
                            .position(posX + 4, posY - 5).size(16, 16).layer(1)
                            .text("")
                            .font(Font.ITEMS)
                            .build();
                    storageElementIcons.add(slotIcon);

                    final int flatIndex = i * 8 + indexInPage;
                    slot.onClick((player, source, clickType) -> onStorageSlotClick(flatIndex));
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

        for (int x = 0; x < 5; x++) { // TODO : CHANGE TO ACCEPT ONE MORE SLOT (16 + x * 30)
            int posX = 31 + x * 30;
            HologramElement slot = new HologramElement.Builder("equipped_consumable_item_slot_" + x, consumablesCategory)
                    .position(posX, 127).size(30,30).layer(-4)
                    .text("\uE000").hoverText("\uE100").font(Font.INVENTORY)
                    .hasHint(true)
                    .button()
                    .build();

            equippedConsumableSlots.add(slot);
            final int equippedIndex = x;
            slot.onClick((player, source, clickType) -> onEquippedConsumableSlotClicked(equippedIndex));
            HologramElement slotIcon = new HologramElement.Builder("equipped_consumable_item_icon_" + x, slot)
                    .position(posX+6, 120).size(16,16).layer(1)
                    .text("")
                    .font(Font.ITEMS)
                    .build();
            equippedConsumableIcons.add(slotIcon);
        }

        // WEAPONS TAB

        equippedWeaponSlot = new HologramElement.Builder("equipped_weapon_slot", weaponsCategory)
                .position(121, 127).size(30,30).layer(-4)
                .text("\uE000").hoverText("\uE100").font(Font.INVENTORY)
                .hasHint(true)
                .button()
                .build();
        equippedWeaponSlot.onClick((player, source, clickType) -> onEquippedWeaponSlotClicked());
        equippedWeaponIcon = new HologramElement.Builder("equipped_weapon_icon", equippedWeaponSlot)
                .position(127, 120).size(16,16).layer(1)
                .text("")
                .font(Font.ITEMS)
                .build();

        // PROFILES TAB

        equippedProfileSlot = new HologramElement.Builder("equipped_profile_slot", profilesCategory)
                .position(121, 127).size(30,30).layer(-4)
                .text("\uE000").hoverText("\uE100").font(Font.INVENTORY)
                .hasHint(true)
                .button()
                .build();
        equippedProfileSlot.onClick((player, source, clickType) -> onEquippedProfileSlotClicked());
        equippedProfileIcon = new HologramElement.Builder("equipped_profile_icon", equippedProfileSlot)
                .position(127, 120).size(16,16).layer(1)
                .text("")
                .font(Font.ITEMS)
                .build();

        // SKILLS TAB

        equippedSkillSlot = new HologramElement.Builder("equipped_skill_slot", skillsCategory)
                .position(121, 127).size(30,30).layer(-4)
                .text("\uE000").hoverText("\uE100").font(Font.INVENTORY)
                .hasHint(true)
                .button()
                .build();
        equippedSkillSlot.onClick((player, source, clickType) -> onEquippedSkillSlotClicked());
        equippedSkillIcon = new HologramElement.Builder("equipped_skill_icon", equippedSkillSlot)
                .position(127, 120).size(16,16).layer(1)
                .text("")
                .font(Font.ITEMS)
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

        if (gamePlayer != null) {
            clearSelections();
            refreshStorage();
        }
    }
}