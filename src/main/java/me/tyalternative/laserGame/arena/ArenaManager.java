package me.tyalternative.laserGame.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArenaManager {

    private final Plugin plugin;
    private final Map<String, Arena> arenasByName = new HashMap<>();

    public ArenaManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        arenasByName.clear();

        File arenasFolder = new File(plugin.getDataFolder(), "arenas");
        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
            return;
        }

        File[] files = arenasFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Aucun fichier d'arène trouvé dans " + arenasFolder.getPath());
            return;
        }

        for (File file : files) {
            try {
                ArenaConfig config = parseConfig(file);
                if (config == null) continue;
                if (!config.isValid()) {
                    plugin.getLogger().warning("Arène '" + file.getName() + "' invalide (vérifie spawns/spectator-spawn) - ignorée.");
                    continue;
                }
                arenasByName.put(config.name(), new Arena(config));
                plugin.getLogger().info("Arène chargée : " + config.name()
                        + " (" + config.minPlayers() + "-" + config.maxPlayers() + " joueurs)");
            } catch (Exception e) {
                plugin.getLogger().severe("Erreur lors du chargement de " + file.getName() + " : " + e.getMessage());
            }
        }
    }

    private ArenaConfig parseConfig(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        String name = yaml.getString("name");
        String worldName = yaml.getString("world");
        int minPlayers = yaml.getInt("min-players", 2);
        int maxPlayers = yaml.getInt("max-players", 8);

        if (name == null || worldName == null) {
            plugin.getLogger().warning(file.getName() + " : champs 'name' ou 'world' manquants -> ignoré.");
            return null;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning(file.getName() + " : le monde '" + worldName
                    + "' n'est pas chargé - arène ignorée.");
            return null;
        }

        List<Location> spawns = new ArrayList<>();
        List<?> rawSpawns = yaml.getList("spawns");
        if (rawSpawns != null) {
            for (Object raw : rawSpawns) {
                if (raw instanceof Map<?, ?> map) {
                    spawns.add(mapToLocation(world, map));
                }
            }
        }

        Location spectatorSpawn = readLocation(world, yaml.getConfigurationSection("spectator-spawn"));
        Location waitingRoom = readLocation(world, yaml.getConfigurationSection("waiting-room"));
        if (waitingRoom == null) {
            plugin.getLogger().warning(file.getName() + " : 'waiting-room' absent, utilisation de 'spectator-spawn' à la place.");
            waitingRoom = spectatorSpawn;
        }

        return new ArenaConfig(name, worldName, minPlayers, maxPlayers, spawns, waitingRoom, spectatorSpawn);
    }

    private Location readLocation(World world, ConfigurationSection section) {
        if (section == null) return null;
        return new Location(
                world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw", 0),
                (float) section.getDouble("pitch", 0)
        );
    }

    private Location mapToLocation(World world, Map<?, ?> map) {
        double x = toDouble(map.get("x"));
        double y = toDouble(map.get("y"));
        double z = toDouble(map.get("z"));
        float yaw = (float) toDouble(map.get("yaw"));
        float pitch = (float) toDouble(map.get("pitch"));
        return new Location(world, x, y, z, yaw, pitch);
    }

    private double toDouble(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0.0;
    }

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(arenasByName.get(name));
    }

    public List<Arena> getAllArenas() {
        return new ArrayList<>(arenasByName.values());
    }
}