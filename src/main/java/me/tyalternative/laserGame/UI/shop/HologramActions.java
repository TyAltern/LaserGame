package me.tyalternative.laserGame.UI.shop;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;
import java.util.function.Predicate;

public final class HologramActions {
    private HologramActions() {}

    public static HologramAction show(String elementId) {
        return (player, source, clickType) -> {
            HologramElement target = source.getRootElement().findRecursive(elementId);
            if (target != null) target.setVisible(true);
        };
    }
    public static HologramAction hide(String elementId) {
        return (player, source, clickType) -> {
            HologramElement target = source.getRootElement().findRecursive(elementId);
            if (target != null) target.setVisible(false);
        };
    }
    public static HologramAction toggle(String elementId) {
        return (player, source, clickType) -> {
            HologramElement target = source.getRootElement().findRecursive(elementId);
            if (target != null) target.setVisible(target.isHidden());
        };
    }

    public static HologramAction setText(String text) {
        return (player, source, clickType) -> {
            if (source != null) source.setText(text);
        };
    }

    public static HologramAction setText(String elementId, String text) {
        return (player, source, clickType) -> {
            HologramElement target = source.getRootElement().findRecursive(elementId);
            if (target != null) target.setText(text);
        };
    }

    public static HologramAction setHoverText(String text) {
        return (player, source, clickType) -> {
            if (source != null) source.setHoverText(text);
        };
    }

    public static HologramAction setHoverText(String elementId, String text) {
        return (player, source, clickType) -> {
            HologramElement target = source.getRootElement().findRecursive(elementId);
            if (target != null) target.setHoverText(text);
        };
    }

    public static HologramAction setDisabled(String elementId, boolean disabled) {
        return (player, source, clickType) -> {
            HologramElement target = source.getRootElement().findRecursive(elementId);
            if (target != null) target.setDisabled(disabled);
        };
    }

    public static HologramAction navigate(String targetId) {
        return (player, source, clickType) -> {
            HologramElement parent = source.getParent();
            if (parent == null) return;

            HologramElement target = parent.getChild(targetId);
            if (target == null) return;

            HologramElement currentlyVisible = parent.getChildren().stream()
                    .filter(c -> !c.isHidden())
                    .findFirst()
                    .orElse(null);
            if (currentlyVisible != null && currentlyVisible != target) {
                HologramNavigation.push(player, currentlyVisible.getId());
            }
            for (HologramElement sibling : parent.getChildren()) {
                sibling.setVisible(sibling == target);
            }
        };
    }

    public static HologramAction back() {
        return (player, source, clickType) -> {
            String previousId = HologramNavigation.pop(player);
            if (previousId == null) return;

//            HologramElement root = source.getRootElement();
//            if (root == null) return;
//
//            HologramElement previous = root.findRecursive(previousId);
//            if (previous == null) return;
//
//            previous.setVisible(true);
//            source.setVisible(false);

            // Ou bien, mais ne marche que pour les frères.

            HologramElement parent = source.getParent();
            if (parent == null) return;

            HologramElement previousSibling = parent.getChild(previousId);
            if (previousSibling == null) return;

            for (HologramElement sibling : parent.getChildren()) {
                sibling.setVisible(sibling == previousSibling);
            }
        };
    }

    public static HologramAction showInGroup(ScreenGroup group, String screenId) {
        return (player, source, clickType) -> group.show(screenId);
    }

    public static HologramAction showInGroup(ScreenGroup group, HologramElement screen) {
        return (player, source, clickType) -> group.show(screen.getId());
    }


    public static HologramAction sound(Sound sound, float volume, float pitch) {
        return (player, source, clickType) -> player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static HologramAction deniedSound() {
        return sound(Sound.ENTITY_VILLAGER_NO, 1F, 1F);
    }

    public static HologramAction close(Hologram hologram) {
        return (player, source, clickType) -> {
            HologramHoverListener.close(player);
            HologramNavigation.clear(player);
            hologram.clearHologram();
        };
    }

    public static HologramAction guard(Predicate<Player> condition, HologramAction ifTrue, HologramAction ifFalse) {
        return (player, source, clickType) -> {
            if (condition.test(player)) ifTrue.execute(player, source, clickType);
            else if(ifFalse != null) ifFalse.execute(player, source, clickType);
        };
    }

    public static HologramAction async(JavaPlugin plugin, Function<Player, Boolean> asyncCheck,
                                       HologramAction onSuccess, HologramAction onFailure) {
        return (player, source, type) -> Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean result;
            try {
                result = asyncCheck.apply(player);
            } catch (Exception e) {
                plugin.getLogger().warning("HologramActions#async check failed for " + source.getId() + ": " + e.getMessage());
                result = false;
            }
            boolean finalResult = result;
            Bukkit.getScheduler().runTask(plugin, () -> {
                HologramAction toRun = finalResult ? onSuccess : onFailure;
                if (toRun != null) toRun.execute(player, source, type);
            });
        });
    }
}
