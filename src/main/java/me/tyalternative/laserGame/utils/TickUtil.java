package me.tyalternative.laserGame.utils;

import me.tyalternative.laserGame.LaserGame;
import org.bukkit.Bukkit;

public class TickUtil {

    public static void nextTick(Runnable action) {
        Bukkit.getScheduler().runTask(LaserGame.getInstance(), action);
    }

    public static void delay(Runnable action, int delay) {
        Bukkit.getScheduler().runTaskLater(LaserGame.getInstance(), action, delay);
    }
}
