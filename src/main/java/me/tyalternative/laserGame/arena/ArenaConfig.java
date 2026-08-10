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
        Location spectatorSpawn,
        List<Location> shopRooms
) {
    public boolean isValid() {
        if (minPlayers < 2) return false;
        if (minPlayers > maxPlayers) return false;
        if (spawns == null || spawns.size() < maxPlayers || shopRooms.size() < maxPlayers) return false;
        if (spectatorSpawn == null) return false;

        return true;
    }

    public Location shopRoomFor(int playerIndex) {
        if (shopRooms == null || shopRooms.isEmpty()) return waitingRoom;
        return shopRooms.get(playerIndex % shopRooms.size());
    }
}
