package me.tyalternative.laserGame.arena;

import java.util.UUID;

public class Arena {

    private final UUID id = UUID.randomUUID();
    private final ArenaConfig config;

    public Arena(ArenaConfig config) {
        this.config = config;
    }

    public UUID getId() { return id; }
    public ArenaConfig getConfig() { return config; }
    public String getName() { return config.name(); }

    @Override
    public String toString() {
        return "Arena{name=" + config.name() + ", players=" + config.minPlayers()
                + "-" + config.maxPlayers() + "}";
    }
}
