package me.tyalternative.laserGame.UI.shop;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScreenGroup {

    private final List<HologramElement> screens;
    private @Nullable HologramElement selectedScreen;

    public ScreenGroup(HologramElement... screens) {
        this.screens = List.of(screens);
    }
    public ScreenGroup(List<HologramElement> screens) {
        this.screens = screens;
    }

    public void show(String screenId) {
        for (HologramElement screen : screens) {
            screen.setVisible(screen.getId().equals(screenId));
            if (screen.getId().equals(screenId)) selectedScreen = screen;
        }
    }

    public void show(HologramElement screen) {
        for (HologramElement element : screens) {
            element.setVisible(element == screen);
        }
        selectedScreen = screen;
    }

    public void hideAll() {
        for (HologramElement screen : screens) {
            screen.setVisible(false);
        }
        selectedScreen = null;
    }

    public @Nullable HologramElement getSelectedScreen() {
        return selectedScreen;
    }

    public List<HologramElement> getScreens() { return screens; }
}
