package me.tyalternative.laserGame.arena;

import org.bukkit.Location;

import java.util.List;

public record ArenaConfig(
        String name,
        String worldName,
        int minPlayers,
        int maxPlayers,
        List<Location> spawns,
        Location waitingRoom,
        Location spectatorSpawn
) {
    public boolean isValid() {
        if (minPlayers < 2) return false;
        if (minPlayers > maxPlayers) return false;
        if (spawns == null || spawns.size() < maxPlayers) return false;
        if (spectatorSpawn == null) return false;

        return true;
    }
}
