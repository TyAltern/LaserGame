
package me.tyalternative.laserGame.UI.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HologramElement {

    private static final Map<UUID, HologramElement> INTERACTION_REGISTRY = new HashMap<>();

    private static final int HIDDEN_LAYER_OFFSET = -250;
    private static final double LAYER_THICKNESS = 0.001;

    public enum State {
        IDLE,
        HOVER
    }
    private final String id;
    private final Location rootAnchor;

    private HologramElement parent;
    private final List<HologramElement> children = new ArrayList<>();

    private int positionX;
    private int positionY;
    private int sizeX;
    private int sizeY;
    private final int layer;
    private final boolean absolutePosition;
    private Vector3f scale;
    private Vector3f translation;

    private String text;
    private String hoverText;
    private NamespacedKey font;
    private boolean dontPropagateFont;

    private final boolean bubbleFocusToParent;
    private final boolean bubbleClickToParent;
    private final boolean bubbleScrollToParent;
    private boolean hasHint;
    private State state = State.IDLE;
    private boolean hidden = false;
    private boolean selfVisible = true;
    private boolean disabled = false;

    private TextDisplay textDisplay;
    private Interaction interaction;

    private final List<HologramAction> actions = new ArrayList<>();
    private final List<HologramScrollAction> scrollActions = new ArrayList<>();
    private final List<HologramHoverAction> hoverEnterActions = new ArrayList<>();
    private final List<HologramHoverAction> hoverExitActions = new ArrayList<>();
    private long cooldownMillis;
    private final Map<UUID, Long> lastClickTimestamps = new HashMap<>();

    private HologramElement(Builder builder) {
        this.id = builder.id;
        this.rootAnchor =                builder.parent == null ? builder.anchor.clone() : null;
        this.parent =                    builder.parent;
        this.positionX =                 builder.positionX;
        this.positionY =                 builder.positionY;
        this.sizeX =                     builder.sizeX;
        this.sizeY =                     builder.sizeY;
        this.layer =                     builder.layer;
        this.absolutePosition =          builder.absolutePosition;
        this.scale =                     builder.scale;
        this.translation =               builder.translation;
        this.text =                      builder.text;
        this.dontPropagateFont =         builder.dontPropagateFont;
        this.hoverText =                 builder.hoverText;
        this.bubbleFocusToParent =       builder.bubbleFocusToParent;
        this.bubbleClickToParent =       builder.bubbleClickToParent;
        this.bubbleScrollToParent =      builder.bubbleScrollToParent;
        this.hasHint =                   builder.hasHint;
        this.cooldownMillis =            builder.cooldownMillis;
        this.actions.addAll(          builder.actions);
        this.scrollActions.addAll(    builder.scrollActions);
        this.hoverEnterActions.addAll(builder.hoverEnterActions);
        this.hoverExitActions.addAll( builder.hoverExitActions);

        this.font = builder.font != null ? builder.font : resolveInheritedFontFromParent();

        Transformation transformation = new Transformation(translation, new AxisAngle4f(0,0,0,0),scale, new AxisAngle4f(0,0,0,0));
        createTextDisplay(transformation);

        if (builder.isButton) setButton();
        if (parent != null) parent.children.add(this);
        setVisible(!builder.hideByDefault);
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
        private boolean absolutePosition = true;
        private Vector3f scale = new Vector3f(1,1,1);
        private Vector3f translation = new Vector3f();

        private String text = "";
        private String hoverText = null;
        private NamespacedKey font;
        private boolean dontPropagateFont = false;
        private boolean isButton = false;
        private boolean bubbleFocusToParent = false;
        private boolean bubbleClickToParent = false;
        private boolean bubbleScrollToParent = false;
        private boolean hasHint = false;
        private boolean hideByDefault = false;

        private final List<HologramAction> actions = new ArrayList<>();
        private final List<HologramScrollAction> scrollActions = new ArrayList<>();
        private final List<HologramHoverAction> hoverEnterActions = new ArrayList<>();
        private final List<HologramHoverAction> hoverExitActions = new ArrayList<>();
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
        public Builder absolutePosition(boolean absolute) { this.absolutePosition = absolute; return this; }
        public Builder text(String text)                  { this.text = text; return this; }
        public Builder hoverText(String hoverText)        { this.hoverText = hoverText; return this; }
        public Builder font(NamespacedKey font)           { this.font = font; return this; }
        public Builder dontPropagateFont(boolean propagate) { this.dontPropagateFont = propagate; return this; }
        public Builder button()                           { this.isButton = true; return this; }
        public Builder bubbleFocusToParent(boolean value) { this.bubbleFocusToParent = value; return this; }
        public Builder bubbleClickToParent(boolean value) { this.bubbleClickToParent = value; return this; }
        public Builder bubbleScrollToParent(boolean value) { this.bubbleScrollToParent = value; return this; }
        public Builder hasHint(boolean value)             { this.hasHint = value; return this; }
        public Builder hideByDefault(boolean value)       { this.hideByDefault = value; return this; }

        public Builder scale(double x, double y, double z) { this.scale = new Vector3f((float) x, (float) y, (float) z); return this;}
        public Builder translation(double x, double y, double z) { this.translation = new Vector3f((float) x, (float) y, (float) z); return this;}

        public Builder onClick(HologramAction action)     { this.actions.add(action); return this; }

        public Builder onClick(HologramClickType type, HologramAction action) {
            this.actions.add((player, source, clickType) -> {
                if (type == HologramClickType.BOTH || clickType == type) action.execute(player, source, clickType);
            });
            return this;
        }

        public Builder onScroll(HologramScrollType type, HologramScrollAction action) {
            this.scrollActions.add((player, source, scrollType) -> {
                if (type == HologramScrollType.BOTH || scrollType == type) action.execute(player, source, scrollType);
            });
            return this;
        }

        public Builder onHoverEnter(HologramHoverAction action) { this.hoverEnterActions.add(action); return this; }
        public Builder onHoverExit(HologramHoverAction action) { this.hoverExitActions.add(action); return this; }

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
        if (absolutePosition) return positionX;
        return parent == null ? positionX : positionX + parent.resolveAbsolutePositionX();
    }

    private int resolveAbsolutePositionY() {
        if (absolutePosition) return positionY;
        return parent == null ? positionY : positionY + parent.resolveAbsolutePositionY();
    }


    private int resolveAbsoluteLayer() {
        return parent == null ? layer : layer + parent.resolveAbsoluteLayer();
    }

    private NamespacedKey resolveInheritedFontFromParent() {
        if (parent == null) return null;
        if (dontPropagateFont) return parent.resolveInheritedFontFromParent();
        return parent.font != null ? parent.font : parent.resolveInheritedFontFromParent();
    }

    private Location resoleTextDisplayLocation() {
//        double offset = sizeX % 2 == 0 ? 0.0125 : 0;
        double offset = 0;
        int absX = resolveAbsolutePositionX();
        int absY = sizeY - 1;
        if (parent != null) absY = resolveAbsolutePositionY();
        int absLayer = resolveAbsoluteLayer();

        return getRootAnchor().clone().add(
                offset + (absX + (double) sizeX / 2) * 0.025,
                absY * -0.025,
                absLayer * LAYER_THICKNESS
        );
    }

    public Location resolveInteractionLocation() {
//        double offset = sizeX % 2 == 0 ? 0.0125 : 0;
         double offset = 0.0125;
         double hiddenDeltaZ = hidden ? HIDDEN_LAYER_OFFSET * LAYER_THICKNESS : 0;
         Vector3f transformation = textDisplay.getTransformation().getTranslation();
         return resoleTextDisplayLocation().add(transformation.x, transformation.y, transformation.z)
                 .add(offset, 0.05, -sizeX * 0.0125 + hiddenDeltaZ);

    }




    private void createTextDisplay(Transformation transformation) {
        try {
            Location spawnLoc = resoleTextDisplayLocation();
            textDisplay = spawnLoc.getWorld().spawn(spawnLoc, TextDisplay.class, entity -> {
                entity.setTransformation(transformation);
                entity.text(Component.text(text).font(font));
                entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                entity.setBillboard(Display.Billboard.FIXED);
                entity.setPersistent(false);

                entity.addScoreboardTag("hologram_element_" + id);
                entity.addScoreboardTag("hologram_element");
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TextDisplay for element " + id, e);
        }
    }

    public TextDisplay getTextDisplay() { return textDisplay; }

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
                entity.addScoreboardTag("hologram_button_" + id);
                entity.addScoreboardTag("hologram_element");
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

    public Interaction getInteraction() { return interaction; }

    private void applyDisplayText(String value) {
        if (textDisplay == null) return;
        textDisplay.text(Component.text(value).font(font));
    }

    public void setText(String newText) {
        this.text = newText;
        boolean showingHoverOverlay = (state == State.HOVER && hoverText != null);
        if (!showingHoverOverlay) {
            applyDisplayText(newText);
        }
    }

    public String getText() { return text; }

    public void setHoverText(String newText) {
        this.hoverText = newText;
        boolean showingHoverOverlay = (state == State.HOVER && hoverText != null);
        if (showingHoverOverlay) {
            applyDisplayText(newText);
        }
    }

    public void setInterpolation(int durationTicks, int delayTicks) {
        if (textDisplay == null) return;
        textDisplay.setInterpolationDuration(durationTicks);
        textDisplay.setInterpolationDelay(delayTicks);
    }
    public void setInterpolationDuration( int durationTicks) {
        if (textDisplay == null) return;
        textDisplay.setInterpolationDuration(durationTicks);
    }
    public void setInterpolationDelay( int delayTicks) {
        if (textDisplay == null) return;
        textDisplay.setInterpolationDelay(delayTicks);
    }

    public void setScale(double x, double y, double z) {
        setScale(new Vector3f((float) x, (float) y, (float) z));
    }
    public void setScale(double uniform) {
        setScale(new Vector3f((float) uniform, (float) uniform, (float) uniform));
    }
    public void setScale(Vector3f scale) {
        if (textDisplay == null) return;
        Transformation current = textDisplay.getTransformation();
        Vector3f currentTranslation = current.getTranslation();
        float newY = (float) ((1 - scale.y) * (0.075) - 0.025);
        textDisplay.setTransformation(new Transformation(
                currentTranslation,
                current.getLeftRotation(),
                scale,
                current.getRightRotation()
        ));
        this.scale = scale;
        setTranslation(new Vector3f(-0.0125f, newY, currentTranslation.z));
    }
    public void setScale(double x, double y, double z, int duration) {
        setInterpolationDuration(duration);
        setScale(new Vector3f((float) x, (float) y, (float) z));
    }
    public void setScale(double uniform, int duration) {
        setInterpolationDuration(duration);
        setScale(new Vector3f((float) uniform, (float) uniform, (float) uniform));
    }
    public void setScale(Vector3f scale, int duration) {
        setInterpolationDuration(duration);
        setScale(scale);
    }

    public void setTranslation(double x, double y, double z) {
        setTranslation(new Vector3f((float) x, (float) y, (float) z));
    }
    public void setTranslation(Vector3f translation) {
        if (textDisplay == null) return;
        Transformation current = textDisplay.getTransformation();
        textDisplay.setTransformation(new Transformation(
                translation,
                current.getLeftRotation(),
                current.getScale(),
                current.getRightRotation()
        ));
        this.translation = translation;

        if (interaction != null) interaction.teleport(resolveInteractionLocation());
    }
    public void setTranslation(double x, double y, double z, int duration) {
        setInterpolationDuration(duration);
        setTranslation(new Vector3f((float) x, (float) y, (float) z));
    }
    public void setTranslation(Vector3f translation, int duration) {
        setInterpolationDuration(duration);
        setTranslation(translation);
    }

    public void setYTranslation(double y) {
        if (textDisplay == null) return;
        Transformation current = textDisplay.getTransformation();

        translation = new Vector3f(current.getTranslation().x, (float) y,current.getTranslation().z);
        textDisplay.setTransformation(new Transformation(
                translation,
                current.getLeftRotation(),
                current.getScale(),
                current.getRightRotation()
        ));

        if (interaction != null) interaction.teleport(resolveInteractionLocation());
    }
    public void setYTranslation(double y, int duration) {
        if (textDisplay == null) return;
        textDisplay.setInterpolationDuration(duration);
        textDisplay.setInterpolationDelay(0);
        Transformation current = textDisplay.getTransformation();
        translation = new Vector3f(current.getTranslation().x, (float) y,current.getTranslation().z);
        textDisplay.setTransformation(new Transformation(
                translation,
                current.getLeftRotation(),
                current.getScale(),
                current.getRightRotation()
        ));

        if (interaction != null) interaction.teleport(resolveInteractionLocation());
    }

    public void addTranslation(double x, double y, double z) {
        setTranslation(new Vector3f((float) x, (float) y, (float) z));
    }
    public void addTranslation(Vector3f translation) {
        if (textDisplay == null) return;
        Transformation current = textDisplay.getTransformation();
        Vector3f currentTranslation = current.getTranslation();
        translation = new Vector3f(currentTranslation.x + translation.x, currentTranslation.y + translation.y, currentTranslation.z + translation.z);
        textDisplay.setTransformation(new Transformation(
                translation,
                current.getLeftRotation(),
                current.getScale(),
                current.getRightRotation()
        ));
        this.translation = translation;

        if (interaction != null) interaction.teleport(resolveInteractionLocation());
    }
    public void addTranslation(double x, double y, double z, int duration) {
        setInterpolationDuration(duration);
        addTranslation(new Vector3f((float) x, (float) y, (float) z));
    }
    public void addTranslation(Vector3f translation, int duration) {
        setInterpolationDuration(duration);
        addTranslation(translation);
    }

    public Vector3f getTranslation() {
        if (textDisplay == null) return new Vector3f(0,0,0);
        return textDisplay.getTransformation().getTranslation();
    }


    public void setPosition(int x, int y) {
        if (textDisplay == null) return;
        this.positionX = x; this.positionY = y;

        textDisplay.teleport(resoleTextDisplayLocation());
    }

    public void setSize(int x, int y) {
        if (textDisplay == null) return;
        this.sizeX = x; this.sizeY = y;

        textDisplay.teleport(resoleTextDisplayLocation());
    }
    public void setRect(int posX, int posY, int sizeX, int sizeY) {
        if (textDisplay == null) return;
        this.positionX = posX; this.positionY = posY;
        this.sizeX = sizeX; this.sizeY = sizeY;

        textDisplay.teleport(resoleTextDisplayLocation());
    }

    public String getHoverText() { return hoverText; }

    public void onHoverEnter(Player player) {
        if (state == State.HOVER) return;
        state = State.HOVER;
        if (hoverText != null) applyDisplayText(hoverText);
        for (HologramHoverAction action : hoverEnterActions) {
            action.execute(player, this);
        }
        if (bubbleFocusToParent && player != null) parent.onHoverEnter(player);
    }

    public void onHoverExit(Player player) {
        if (state == State.IDLE) return;
        state = State.IDLE;
        if (hoverText != null) applyDisplayText(text);
        for (HologramHoverAction action : hoverExitActions) {
            action.execute(player, this);
        }
        if (bubbleFocusToParent && player != null) parent.onHoverExit(player);
    }

    public void onClick(HologramClickType type, HologramAction action) {
        this.actions.add((player, source, clickType) -> {
            if (type == HologramClickType.BOTH || clickType == type) action.execute(player, source, clickType);
        });

    }

    public void onClick(HologramAction action) {
        this.actions.add(action);
    }

    public void onScroll(HologramScrollType type, HologramScrollAction action) {
        this.scrollActions.add((player, source, scrollType) -> {
            if (type == HologramScrollType.BOTH || scrollType == type) action.execute(player, source, scrollType);
        });
    }

    public void onScroll(HologramScrollAction action) {
        this.scrollActions.add(action);
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

        if (bubbleClickToParent && player != null) parent.executeActions(player, type);
    }

    public void executeScrollActions(Player player, HologramScrollType type) {
        for (HologramScrollAction action : scrollActions) {
            action.execute(player, this, type);
        }
        if (bubbleScrollToParent && player != null) parent.executeScrollActions(player, type);
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

    public boolean hasHint() { return hasHint; }

    public void setVisible(boolean visible) {
        this.selfVisible = visible;
        boolean ancestorsVisible = parent == null || !parent.hidden;
        applyEffectVisibility(ancestorsVisible);
    }

    private void applyEffectVisibility(boolean ancestorsVisible) {
        boolean effective = ancestorsVisible && selfVisible;
        this.hidden = !effective;

        if (textDisplay != null) textDisplay.setViewRange(effective ? 1.0f : 0f);
        if (interaction != null) interaction.teleport(resolveInteractionLocation());

        for (HologramElement child : children) {
            child.applyEffectVisibility(effective);
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
