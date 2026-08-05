package me.tyalternative.laserGame.UI.shop;

import org.bukkit.entity.Player;

import java.util.*;

public class HologramNavigation {

    private static final Map<UUID, Deque<String>> HISTORY = new HashMap<>();

    public static void push(Player player, String screenId) {
        HISTORY.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>()).push(screenId);
    }

    public static String pop(Player player) {
        Deque<String> stack = HISTORY.get(player.getUniqueId());
        return (stack == null || stack.isEmpty()) ? null : stack.pop();
    }

    public static void clear(Player player) {
        HISTORY.remove(player.getUniqueId());
    }
}
