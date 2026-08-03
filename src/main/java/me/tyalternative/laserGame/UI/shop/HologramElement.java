
package me.tyalternative.laserGame.UI.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HologramElement {

    private static final Map<UUID, HologramElement> INTERACTION_REGISTRY = new HashMap<>();

    private static final int HIDDEN_LAYER_OFFSET = -5;

    public enum State {
        IDLE,
        HOVER
    }
    private final String id;
    private final Location rootAnchor;

    private HologramElement parent;
    private final List<HologramElement> children = new ArrayList<>();

    private final int positionX;
    private final int positionY;
    private final int sizeX;
    private final int sizeY;
    private final int layer;

    private String text;
    private String hoverText;
    private NamespacedKey font;

    private final boolean bubbleFocusToParent;
    private State state = State.IDLE;
    private boolean hidden = false;
    private boolean disabled = false;

    private TextDisplay textDisplay;
    private Interaction interaction;

    private final List<HologramAction> actions = new ArrayList<>();
    private long cooldownMillis;
    private final Map<UUID, Long> lastClickTimestamps = new HashMap<>();

    private HologramElement(Builder builder) {
        this.id = builder.id;
        this.rootAnchor =           builder.parent == null ? builder.anchor.clone() : null;
        this.parent =               builder.parent;
        this.positionX =            builder.positionX;
        this.positionY =            builder.positionY;
        this.sizeX =                builder.sizeX;
        this.sizeY =                builder.sizeY;
        this.layer =                builder.layer;
        this.text =                 builder.text;
        this.hoverText =            builder.hoverText;
        this.bubbleFocusToParent =  builder.bubbleFocusToParent;
        this.cooldownMillis =       builder.cooldownMillis;
        this.actions.addAll(     builder.actions);

        this.font = builder.font != null ? builder.font : resolveInheritedFontFromParent();

        createTextDisplay();

        if (builder.isButton) setButton();
        if (parent != null) parent.children.add(this);
    }

    public static class Builder {
        private final String id;
        private final HologramElement parent;
        private final Location anchor;

        private int positionX = 0;
        private int positionY = 0;
        private int sizeX = 1;
        private int sizeY = 1;
        private int layer = 0;

        private String text = "";
        private String hoverText = null;
        private NamespacedKey font;
        private boolean isButton = false;
        private boolean bubbleFocusToParent = false;

        private final List<HologramAction> actions = new ArrayList<>();
        private long cooldownMillis = 150L;

        /** Element racine : anchor = position monde de base du Hologram. */
        public Builder(String id, Location anchor) {
            this.id = id;
            this.parent = null;
            this.anchor = anchor;
        }

        /** Element enfant : position/layer relatifs au parent. */
        public Builder(String id, HologramElement parent) {
            this.id = id;
            this.parent = parent;
            this.anchor = null;
        }

        public Builder position(int x, int y)             { this.positionX = x; this.positionY = y; return this; }
        public Builder size(int x, int y)                 { this.sizeX = x; this.sizeY = y; return this; }
        public Builder layer(int layer)                   { this.layer = layer; return this; }
        public Builder text(String text)                  { this.text = text; return this; }
        public Builder hoverText(String hoverText)        { this.hoverText = hoverText; return this; }
        public Builder font(NamespacedKey font)           { this.font = font; return this; }
        public Builder button()                           { this.isButton = true; return this; }
        public Builder bubbleFocusToParent(boolean value) { this.bubbleFocusToParent = value; return this; }

        public Builder onClick(HologramAction action)     { this.actions.add(action); return this; }

        public Builder onClick(HologramClickType type, HologramAction action) {
            this.actions.add((player, source, clickType) -> {
                if (clickType == type) action.execute(player, source, clickType);
            });
            return this;
        }

        public Builder cooldown(long millis) { this.cooldownMillis = millis; return this; }

        public HologramElement build() {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("HologramElement id must not be blank");
            }
            if (sizeX <= 0 || sizeY <= 0) {
                throw new IllegalArgumentException("HologramElement size must be positive (id=" + id + ")");
            }
            if (parent == null && anchor == null) {
                throw new IllegalStateException("Root HologramElement requires an anchor Location (id=" + id + ")");
            }
            return new HologramElement(this);
        }
    }

    private Location getRootAnchor() {
        return parent == null ? rootAnchor : parent.getRootAnchor();
    }

    private int resolveAbsolutePositionX() {
        return parent == null ? positionX : positionX + parent.resolveAbsolutePositionX();
    }

    private int resolveAbsolutePositionY() {
        return parent == null ? positionY : positionY + parent.resolveAbsolutePositionY();
    }

    private int resolveAbsoluteLayer() {
        return parent == null ? layer : layer + resolveAbsoluteLayer();
    }

    private NamespacedKey resolveInheritedFontFromParent() {
        if (parent == null) return null;
        return parent.font != null ? parent.font : parent.resolveInheritedFontFromParent();
    }

    private Location resoleTextDisplayLocation() {
        double offset = sizeX % 2 == 0 ? 0.0125 : 0;
        int absX = resolveAbsolutePositionX();
        int absY = resolveAbsolutePositionY();
        int absLayer = resolveAbsoluteLayer();

        return getRootAnchor().clone().add(
                offset + (absX + (double) sizeX / 2) * 0.025,
                absY * -0.025,
                absLayer * 0.005
        );
    }

    private Location resolveInteractionLocation() {
        double offset = sizeX % 2 == 0 ? 0.0125 : 0;
        double hiddenDeltaZ = hidden ? HIDDEN_LAYER_OFFSET * 0.005 : 0;

        return resoleTextDisplayLocation()
                .add(offset, 0.05, -sizeX * 0.0125 + hiddenDeltaZ);
    }




    private void createTextDisplay() {
        try {
            Location spawnLoc = resoleTextDisplayLocation();
            textDisplay = spawnLoc.getWorld().spawn(spawnLoc, TextDisplay.class, entity -> {
                entity.text(Component.text(text).font(font));
                entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                entity.setBillboard(Display.Billboard.FIXED);
                entity.setPersistent(false);

                entity.customName(Component.text(id));
                entity.setCustomNameVisible(false);
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TextDisplay for element " + id, e);
        }
    }

    public Interaction setButton() {
        if (textDisplay == null) return null;

        if (interaction != null) {
            INTERACTION_REGISTRY.remove(interaction.getUniqueId());
            interaction.remove();
        }

        try {
            Location spawnLoc = resolveInteractionLocation();
            interaction = spawnLoc.getWorld().spawn(spawnLoc, Interaction.class, entity -> {
                entity.setInteractionWidth(sizeX * 0.025f);
                entity.setInteractionHeight(sizeY * 0.025f);
                entity.customName(Component.text(id + "_button"));
                entity.setCustomNameVisible(false);
                entity.setPersistent(false);
            });
            INTERACTION_REGISTRY.put(interaction.getUniqueId(), this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Interaction for element " + id, e);
        }

        return interaction;
    }

    public void removeButton() {
        if (interaction == null) return;
        INTERACTION_REGISTRY.remove(interaction.getUniqueId());
        interaction.remove();
        interaction = null;
    }

    private void applyDisplayText(String value) {
        if (textDisplay == null) return;
        textDisplay.text(Component.text(value).font(font));
    }

    public void setText(String newText) {
        this.text = newText;
        if (state == State.IDLE) {
            applyDisplayText(newText);
        }
    }

    public String getText() { return text; }

    public void onHoverEnter(Player player) {
        if (state == State.HOVER) return;
        state = State.HOVER;
        if (hoverText != null) applyDisplayText(hoverText);
        if (bubbleFocusToParent && player != null) parent.onHoverEnter(player);
    }

    public State getState() { return state; }

    public boolean isDisabled() { return disabled;}
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    public boolean isOnCooldown(UUID playerId) {
        Long last = lastClickTimestamps.get(playerId);
        return last != null && (System.currentTimeMillis() - last) < cooldownMillis;
    }

    public void markClicked(UUID playerId) {
        lastClickTimestamps.put(playerId, System.currentTimeMillis());
    }

    public void executeActions(Player player, HologramClickType type) {
        for (HologramAction action : actions) {
            action.execute(player, this, type);
        }
    }

    public HologramElement getParent() { return parent; }
    public HologramElement getRootElement() { return parent == null ? this : parent.getRootElement(); }
    public List<HologramElement> getChildren() { return List.copyOf(children);}

    public HologramElement getChild(String childId) {
        for (HologramElement child : children) {
            if (child.id.equals(childId)) return child;
        }
        return null;
    }

    public HologramElement findRecursive(String targetId) {
        if (id.equals(targetId)) return this;
        for (HologramElement child : children) {
            HologramElement found = child.findRecursive(targetId);
            if (found != null) return found;
        }
        return null;
    }

    public void setVisible(boolean visible) {
        this.hidden = !visible;

        if (textDisplay != null) textDisplay.setViewRange(visible ? 1.0f : 0f);
        if (interaction != null) interaction.teleport(resolveInteractionLocation());

        for (HologramElement child : children) {
            child.setVisible(visible);
        }
    }

    public boolean isHidden() { return hidden; }

    public void remove() {
        for (HologramElement child : new ArrayList<>(children)) {
            child.remove();
        }
        children.clear();

        if (textDisplay != null) {
            textDisplay.remove();
            textDisplay = null;
        }
        if (interaction != null) {
            INTERACTION_REGISTRY.remove(interaction.getUniqueId());
            interaction.remove();
            interaction = null;
        }
    }

    public String getId() { return id; }
    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getPositionX() { return positionX; }
    public int getPositionY() { return positionY; }
    public boolean hasButton() { return interaction != null; }
    public Interaction getButton() { return interaction; }

    public static HologramElement getElementForInteraction(UUID interactionId) {
        return INTERACTION_REGISTRY.get(interactionId);
    }
}
