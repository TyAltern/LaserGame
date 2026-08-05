package me.tyalternative.laserGame.UI.shop;

import java.util.List;

public class ScreenGroup {

    private final List<HologramElement> screens;

    public ScreenGroup(HologramElement... screens) {
        this.screens = List.of(screens);
    }

    public void show(String screenId) {
        for (HologramElement screen : screens) {
            screen.setVisible(screen.getId().equals(screenId));
        }
    }

    public void show(HologramElement screen) {
        for (HologramElement element : screens) {
            element.setVisible(element == screen);
        }
    }

    public void hideAll() {
        for (HologramElement screen : screens) {
            screen.setVisible(false);
        }
    }
}
