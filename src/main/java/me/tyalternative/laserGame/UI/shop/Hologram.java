package me.tyalternative.laserGame.UI.shop;

import me.tyalternative.laserGame.LaserGame;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Hologram {

    private final String id;
    private final int sizeX;
    private final int sizeY;
    private final HologramElement root;

    public Hologram(Location location, String id, int sizeX, int sizeY, String background, NamespacedKey font) {
        this.id = id;
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        Location anchor = location.clone().add(((double) -sizeX / 2) * 0.025, sizeY * 0.025, 0);
//        Location anchor = location.clone();

        this.root = new HologramElement.Builder(id + "_background", anchor)
                .position(0, 0)
                .size(sizeX,sizeY)
                .layer(0)
                .text(background)
                .font(font)
                .button()
                .build();
    }

    public String getId() { return id; }
    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }

    public HologramElement getRoot() { return root; }

    public HologramElement getElement(String elementId) {
        return root.findRecursive(elementId);
    }

    public void clearHologram() {
        root.remove();
    }
}
