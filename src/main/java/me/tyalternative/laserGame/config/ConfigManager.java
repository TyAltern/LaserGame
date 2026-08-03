package me.tyalternative.laserGame.config;

import me.tyalternative.laserGame.shop.DuplicatePolicy;
import me.tyalternative.laserGame.shop.Rarity;
import me.tyalternative.laserGame.weapon.TrailMode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Map;

public class ConfigManager {

    private final Plugin plugin;

    // Arme
    private long hudIntervalTicks;
    private boolean refillAmmoOnRespawn;

    // Partie / match
    private int startingLives;
    private long respawnDelayTicks;
    private int countdownSeconds;
    private int minPlayersFallback;
    private int roundsToWin;

    // Économie
    private int passivePerRound;
    private int shotHitReward;
    private int eliminationReward;
    private int roundWinReward;

    // Shop
    private DuplicatePolicy shopDuplicatePolicy;
    private int baseItemSlots;
    private int shopPhaseSeconds;
    private int shopConsumableSlots;
    private int rerollBaseCost;
    private int rerollCostIncrement;
    private int pityWeightShiftPerReroll;
    private final Map<Rarity, Integer> rarityWeights = new EnumMap<>(Rarity.class);

    // Trail
    private TrailMode trailMode;
    private Material trailMaterial;
    private double trailStep;
    private long trailLifetimeTicks;
    private float trailThickness;

    // Lobby (retour après partie)
    private String lobbyWorldName;
    private double lobbyX, lobbyY, lobbyZ;
    private float lobbyYaw, lobbyPitch;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        var cfg = plugin.getConfig();

        hudIntervalTicks = cfg.getLong("weapon.hud-interval-ticks", 2);
        refillAmmoOnRespawn = cfg.getBoolean("weapon.refill-ammo-on-respawn", true);

        startingLives = cfg.getInt("game.starting-lives", 3);
        respawnDelayTicks = cfg.getLong("game.respawn-delay-ticks", 60);
        countdownSeconds = cfg.getInt("game.countdown-seconds", 10);
        minPlayersFallback = cfg.getInt("game.min-players-fallback", 2);
        roundsToWin = cfg.getInt("game.rounds-to-win", 5);

        passivePerRound = cfg.getInt("economy.passive-per-round", 10);
        shotHitReward = cfg.getInt("economy.shot-hit-reward", 5);
        eliminationReward = cfg.getInt("economy.elimination-reward", 15);
        roundWinReward = cfg.getInt("economy.round-win-reward", 30);

        String policyRaw = cfg.getString("shop.duplicate-policy", "SEPARATE_SLOT");
        try {
            shopDuplicatePolicy = DuplicatePolicy.valueOf(policyRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("shop.duplicate-policy invalide ('" + policyRaw + "'), fallback sur SEPARATE_SLOT.");
            shopDuplicatePolicy = DuplicatePolicy.SEPARATE_SLOT;
        }
        baseItemSlots = cfg.getInt("shop.base-item-slots", 5);
        shopPhaseSeconds = cfg.getInt("shop.phase-duration-seconds", 20);
        shopConsumableSlots = cfg.getInt("shop.consumable-slots", 5);
        rerollBaseCost = cfg.getInt("shop.reroll-base-cost", 5);
        rerollCostIncrement = cfg.getInt("shop.reroll-cost-increment", 3);
        pityWeightShiftPerReroll = cfg.getInt("shop.pity-weight-shift-per-reroll", 2);

        rarityWeights.clear();
        ConfigurationSection weightsSection = cfg.getConfigurationSection("shop.rarity-weights");
        if (weightsSection != null) {
            for (Rarity r : Rarity.values()) {
                rarityWeights.put(r, weightsSection.getInt(r.name(), 0));
            }
        } else {
            plugin.getLogger().warning("shop.rarity-weights manquant, utilisation de poids par défaut.");
            rarityWeights.put(Rarity.COMMON, 60);
            rarityWeights.put(Rarity.UNIQUE, 25);
            rarityWeights.put(Rarity.RARE, 12);
            rarityWeights.put(Rarity.LEGENDARY, 3);
        }

        String modeRaw = cfg.getString("trail.mode", "LINE");
        try {
            trailMode = TrailMode.valueOf(modeRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("trail.mode invalide ('" + modeRaw + "'), fallback sur LINE.");
            trailMode = TrailMode.LINE;
        }
        Material tm = Material.matchMaterial(cfg.getString("trail.block-material", "RED_STAINED_GLASS"));
        trailMaterial = tm != null ? tm : Material.RED_STAINED_GLASS;
        trailStep = cfg.getDouble("trail.step", 0.5);
        trailLifetimeTicks = cfg.getLong("trail.lifetime-ticks", 6);
        trailThickness = (float) cfg.getDouble("trail.thickness", 0.15);

        lobbyWorldName = cfg.getString("lobby.world", "world");
        lobbyX = cfg.getDouble("lobby.x", 965.5);
        lobbyY = cfg.getDouble("lobby.y", 107.0);
        lobbyZ = cfg.getDouble("lobby.z", 1025.5);
        lobbyYaw = (float) cfg.getDouble("lobby.yaw", 180.0);
        lobbyPitch = (float) cfg.getDouble("lobby.pitch", 0.0);
    }

    public long getHudIntervalTicks() { return hudIntervalTicks; }
    public boolean isRefillAmmoOnRespawn() { return refillAmmoOnRespawn; }

    public int getStartingLives() { return startingLives; }
    public long getRespawnDelayTicks() { return respawnDelayTicks; }
    public int getCountdownSeconds() { return countdownSeconds; }
    public int getMinPlayersFallback() { return minPlayersFallback; }
    public int getRoundsToWin() { return roundsToWin; }

    public int getPassivePerRound() { return passivePerRound; }
    public int getShotHitReward() { return shotHitReward; }
    public int getEliminationReward() { return eliminationReward; }
    public int getRoundWinReward() { return roundWinReward; }

    public DuplicatePolicy getShopDuplicatePolicy() { return shopDuplicatePolicy; }
    public int getBaseItemSlots() { return baseItemSlots; }
    public int getShopPhaseSeconds() { return shopPhaseSeconds; }
    public int getShopConsumableSlots() { return shopConsumableSlots; }
    public int getRerollBaseCost() { return rerollBaseCost; }
    public int getRerollCostIncrement() { return rerollCostIncrement; }
    public int getPityWeightShiftPerReroll() { return pityWeightShiftPerReroll; }
    public Map<Rarity, Integer> getRarityWeights() { return rarityWeights; }

    public TrailMode getTrailMode() { return trailMode; }
    public Material getTrailMaterial() { return trailMaterial; }
    public double getTrailStep() { return trailStep; }
    public long getTrailLifetimeTicks() { return trailLifetimeTicks; }
    public float getTrailThickness() { return trailThickness; }

    public Location getLobbyLocation() {
        World world = Bukkit.getWorld(lobbyWorldName);
        if (world == null) {
            plugin.getLogger().warning("Le monde du lobby ('" + lobbyWorldName + "') n'est pas chargé.");
            return null;
        }
        return new Location(world, lobbyX, lobbyY, lobbyZ, lobbyYaw, lobbyPitch);
    }
}
