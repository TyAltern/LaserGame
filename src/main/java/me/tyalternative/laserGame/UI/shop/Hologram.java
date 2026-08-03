package me.tyalternative.laserGame.UI.shop;

import me.tyalternative.laserGame.LaserGame;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Hologram {
//    private final Location anchor;
//    private String id;
//
//    private HologramElement background;
//
//    private int sizeX;
//    private int sizeY;
//
//    private final List<HologramElement> elements = new ArrayList<>();
//
//    public Hologram(Location location, String id, int sizeX, int sizeY, String background, NamespacedKey font, Logger logger) {
//        this.id = id;
//        this.sizeX = sizeX;
//        this.sizeY = sizeY;
//
//        this.anchor = location.clone().add(((double) -sizeX /2) * 0.025, sizeY * 0.025,0);
//
//        this.background = new HologramElement(id+"_background",anchor, 0, sizeY-1, sizeX, sizeY, -1, background, font);
//        this.background.setButton();
//
//        logger.info("background created at " + anchor);
//
//    }
//
//    public String getId() { return id; }
//    public int getSizeX() { return sizeX; }
//    public int getSizeY() { return sizeY; }
//
//    public HologramElement getBackground() { return background; }
//
//    public void addText(String id, int positionX, int positionY,
//                        int sizeX, int sizeY, int layer, String text, NamespacedKey font) {
//        HologramElement element = new HologramElement(id, anchor, positionX, positionY, sizeX, sizeY, layer, text, font);
//        elements.add(element);
//    }
//
//    public void addButton(String id, int positionX, int positionY,
//                          int sizeX, int sizeY, int layer, String text, NamespacedKey font) {
//        HologramElement element = new HologramElement(id, anchor, positionX, positionY, sizeX, sizeY, layer, text, font);
//        element.setButton();
//        elements.add(element);
//    }
//
//    public HologramElement getElement(String id) {
//        for (HologramElement he : elements) {
//            if (he.getId().equals(id)) { return he; }
//        }
//        return null;
//    }
//
//    public void clearHologram() {
//        for (HologramElement element : elements) {
//            element.remove();
//        }
//        background.remove();
//    }

}
