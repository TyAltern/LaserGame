package me.tyalternative.laserGame.UI.shop.impl;

import me.tyalternative.laserGame.UI.shop.HologramElement;
import me.tyalternative.laserGame.utils.Font;
import me.tyalternative.laserGame.utils.TextUtil;
import me.tyalternative.laserGame.utils.TickUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemHintPanel {

    private final static int MAX_PIXEL_PER_LINE = 74;

    private HologramElement itemHintPanel;
    private HologramElement linkedHologramElement;
    private String itemName;
    private HologramElement itemNameElement;
    private String description;
    private List<String> statModification;
    private List<HologramElement> descriptionsLinesElements = new ArrayList<>();
    private HologramElement descriptionElement;


    private final Map<String, HintData> hintData = new HashMap<>();

    public ItemHintPanel(HologramElement parent) {
        createHologram(parent);
    }

    public HologramElement getHologram() { return itemHintPanel; }

    private void createHologram(HologramElement parent) {
        this.itemHintPanel = new HologramElement.Builder("item_hint_panel", parent)
                .size(86,95).layer(13)
                .text("\uD000").font(Font.MENU)
                .hideByDefault(true)
                .build();

        itemNameElement = new HologramElement.Builder("item_name", itemHintPanel)
                .size(78,5).layer(14)
                .text("").font(Font.TEXT)
                .translation(3 * 0.025, 82 * 0.025,0)
                .build();

//        descriptionElement = new HologramElement.Builder("item_description", itemHintPanel)
//                .size(74, 5).layer(14)
//                .text("").font(Font.TEXT)
//                .translation(7 * 0.025, (7)  * 0.025,0)
//
//                .build();

        for (int i = 9; i >= 0; i--) {
            descriptionsLinesElements.add(
                    new HologramElement.Builder("description_line_" + (9-i), itemHintPanel)
                            .size(74, 5).layer(14)
                            .text("").font(Font.TEXT)
                            .translation(7 * 0.025, (6 + i * 7)  * 0.025,0)
                            .build()
            );
        }



    }


    public void setHint(String slotId, String name, String description, List<String> statModification) {
        if (slotId == null) return;
        if (name == null) {
            hintData.remove(slotId);
            return;
        }
        hintData.put(slotId, new HintData(name,description == null ? "" : description,statModification));
    }


    public void clearHint(String slotId) {
        if (slotId == null) return;
        hintData.remove(slotId);
    }

    public void showHint(HologramElement slot) {

        HintData data = hintData.get(slot.getId());
        if (data == null) {

            return;
        }

        linkedHologramElement = slot;

        placeNextTo(slot);

        TickUtil.nextTick(() -> setItemName(data.displayName));
        TickUtil.nextTick(() -> setDescription(data.description, data.statModification));

        itemHintPanel.setVisible(true);
    }

    public void hideHint() {

        itemHintPanel.setVisible(false);
    }

    private void placeNextTo(HologramElement slot) {

        int position = slot.getPositionX() + slot.getSizeX()/2 > itemHintPanel.getRootElement().getSizeX()/2 ? -1 : 1;
        int x = position == -1 ? slot.getPositionX() - itemHintPanel.getSizeX()-1 : slot.getPositionX() + slot.getSizeX() + 1;
        int y = Math.min(itemHintPanel.getRootElement().getSizeY() - 5, Math.max(linkedHologramElement.getSizeY() + 5,slot.getPositionY() + slot.getSizeY()/2));

        setPosition(x,y);
    }

    private void setItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) return;

        int textSize = TextUtil.getStringLength(itemName);
        itemNameElement.setText(itemName);
        itemNameElement.setSize(textSize, 5);

        this.itemName = itemName;
    }

    private void setDescription(String description, List<String> statModification) {
        if (description == null) description = "";
        List<String> lines = TextUtil.wrapText(description, 74);
        int index = 0;
        for (int i = 9; i >= 0; i--) {
            HologramElement lineElement = descriptionsLinesElements.get(i);
            lineElement.setText("");


            if (index < statModification.size()) {
                String line = "¤ " + statModification.get(index);
                int textSize = TextUtil.getStringLength(line);
                lineElement.setText(line);
                lineElement.setSize(textSize, 5);
            } else if (i < lines.size()){
                String line = lines.get(i);
                int textSize = TextUtil.getStringLength(line);
                lineElement.setText(line);
                lineElement.setSize(textSize, 5);
            }

            index++;
        }
        for (int i = 0; i < descriptionsLinesElements.size(); i++) {
            HologramElement lineElement = descriptionsLinesElements.get(i);
            if (i >= lines.size()) {
                lineElement.setText("");
                continue;
            }
            String line = lines.get(i);
            int textSize = TextUtil.getStringLength(line);
            lineElement.setText(line);
            lineElement.setSize(textSize, 5);
        }
        this.description = description;
        this.statModification = statModification;
    }

    private void setPosition(int x, int y) {
        itemHintPanel.setPosition(x,y);
        itemNameElement.setPosition(x,y);

        for (HologramElement descriptionLineElement : descriptionsLinesElements) {
            descriptionLineElement.setPosition(x,y);
        }
//        for (HologramElement statLineElement : statsLinesElements) {
//            statLineElement.setPosition(x,y);
//        }

    }

    private class HintData{
        public String displayName;
        public String description;
        public List<String> statModification;

        public HintData(String displayName, String description, List<String> statModification) {
            this.displayName = displayName;
            this.description = description;
            this.statModification = statModification;
        }
    }
}