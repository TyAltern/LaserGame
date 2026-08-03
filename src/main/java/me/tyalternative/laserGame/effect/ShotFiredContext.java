package me.tyalternative.laserGame.effect;

import org.bukkit.entity.Player;

public class ShotFiredContext {
    public final Player shooter;
    public boolean consumesAmmo = true;

    public ShotFiredContext(Player shooter) {
        this.shooter = shooter;
    }
}
