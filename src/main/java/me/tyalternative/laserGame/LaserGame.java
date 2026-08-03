package me.tyalternative.laserGame;

import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.arena.ArenaManager;
import me.tyalternative.laserGame.command.LaserCommand;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.listeners.ConsumableUseListener;
import me.tyalternative.laserGame.listeners.ItemSelectionListener;
import me.tyalternative.laserGame.listeners.PlayerAttackListener;
import me.tyalternative.laserGame.listeners.PlayerConnectionListener;
import me.tyalternative.laserGame.listeners.PlayerDamageListener;
import me.tyalternative.laserGame.listeners.PlayerInteractListener;
import me.tyalternative.laserGame.listeners.SkillUseListener;
import me.tyalternative.laserGame.shop.ConsumableManager;
import me.tyalternative.laserGame.shop.ShopManager;
import me.tyalternative.laserGame.skill.SkillManager;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeManager;
import me.tyalternative.laserGame.weapon.ShotTrailRenderer;
import me.tyalternative.laserGame.weapon.WeaponAbilityManager;
import me.tyalternative.laserGame.weapon.WeaponManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class LaserGame extends JavaPlugin {

    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private WeaponManager weaponManager;
    private WeaponAbilityManager abilityManager;
    private ConsumableManager consumableManager;
    private SkillManager skillManager;
    private ArchetypeManager archetypeManager;
    private PermanentUpgradeManager upgradeManager;
    private ShopManager shopManager;
    private GameManager gameManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        saveResourceIfMissing("arenas/test.yml");
        saveResourceIfMissing("weapons/pistol.yml");
        saveResourceIfMissing("weapons/sniper.yml");
        saveResourceIfMissing("weapons/machine_gun.yml");
        saveResourceIfMissing("weapons/regen_pistol.yml");
        saveResourceIfMissing("consumables/damage_cap.yml");
        saveResourceIfMissing("consumables/bonus_damage.yml");
        saveResourceIfMissing("consumables/instant_reload.yml");
        saveResourceIfMissing("consumables/glow_nearby.yml");
        saveResourceIfMissing("consumables/speed_boost.yml");
        saveResourceIfMissing("consumables/invisibility.yml");
        saveResourceIfMissing("skills/grapple.yml");
        saveResourceIfMissing("skills/emergency_shield.yml");
        saveResourceIfMissing("skills/speed_burst.yml");
        saveResourceIfMissing("archetypes/berserker.yml");
        saveResourceIfMissing("archetypes/nervous_trigger.yml");
        saveResourceIfMissing("archetypes/vampire.yml");
        saveResourceIfMissing("upgrades/speed_boost_permanent.yml");
        saveResourceIfMissing("upgrades/fast_reload_permanent.yml");
        saveResourceIfMissing("upgrades/extra_slot.yml");
        saveResourceIfMissing("upgrades/reload_shield.yml");

        this.configManager = new ConfigManager(this);
        configManager.load();
        this.arenaManager = new ArenaManager(this);
        arenaManager.loadAll();
        this.weaponManager = new WeaponManager(this);
        weaponManager.loadAll();
        this.abilityManager = new WeaponAbilityManager();
        this.consumableManager = new ConsumableManager(this);
        consumableManager.loadAll();
        this.skillManager = new SkillManager(this);
        skillManager.loadAll();
        this.archetypeManager = new ArchetypeManager(this);
        archetypeManager.loadAll();
        this.upgradeManager = new PermanentUpgradeManager(this);
        upgradeManager.loadAll();
        this.shopManager = new ShopManager(configManager, consumableManager, skillManager,
                archetypeManager, upgradeManager, weaponManager);

        ShotTrailRenderer trailRenderer = new ShotTrailRenderer(this, configManager);
        this.gameManager = new GameManager(this, configManager, arenaManager, weaponManager, abilityManager,
                archetypeManager, shopManager);

        getServer().getPluginManager().registerEvents(
                new PlayerInteractListener(weaponManager, gameManager), this);
        getServer().getPluginManager().registerEvents(
                new PlayerAttackListener(weaponManager, gameManager, trailRenderer), this);
        getServer().getPluginManager().registerEvents(
                new PlayerDamageListener(gameManager), this);
        getServer().getPluginManager().registerEvents(
                new PlayerConnectionListener(gameManager), this);
        getServer().getPluginManager().registerEvents(
                new ItemSelectionListener(gameManager), this);
        getServer().getPluginManager().registerEvents(
                new ConsumableUseListener(gameManager, consumableManager), this);
        getServer().getPluginManager().registerEvents(
                new SkillUseListener(gameManager, skillManager), this);

        var laserCommand = new LaserCommand(gameManager, arenaManager, weaponManager, consumableManager,
                skillManager, archetypeManager, upgradeManager, shopManager);
        getCommand("laser").setExecutor(laserCommand);

        long weaponsWithAbility = weaponManager.getAllWeapons().stream()
                .filter(w -> w.specialAbilityId() != null)
                .count();

        getLogger().info("LaserGame activé - " + arenaManager.getAllArenas().size() + " arène(s), "
                + weaponManager.getAllWeapons().size() + " arme(s) dont " + weaponsWithAbility + " avec capacité, "
                + consumableManager.getAllDefinitions().size() + " consommable(s), "
                + skillManager.getAllDefinitions().size() + " compétence(s), "
                + archetypeManager.getAllDefinitions().size() + " archétype(s), "
                + upgradeManager.getAllDefinitions().size() + " amélioration(s) chargée(s).");

//        Location locationTest = new Location(Bukkit.getWorld("world"), 961.5, 107.5, 1025.5);
//        Hologram hologram = new Hologram(locationTest, "shop", 212,138, "\uE001", new NamespacedKey("hud","shop"), getLogger());
//        hologram.addButton("consumable", 17, 48, 86, 14, 1, "\uE011",new NamespacedKey("hud","shop"));
//        hologram.addButton("special_items", 109, 48, 86, 14, 1, "\uE020",new NamespacedKey("hud","shop"));
//        hologram.addButton("slot_1", 81, 88, 30, 36, 1, "\uE032",new NamespacedKey("hud","shop"));
//        hologram.addButton("slot_2", 111, 88, 30, 36, 1, "\uE030",new NamespacedKey("hud","shop"));
//        hologram.addButton("slot_3", 141, 88, 30, 36, 1, "\uE031",new NamespacedKey("hud","shop"));
    }

    @Override
    public void onDisable() {
        getLogger().info("LaserGame désactivé.");
    }

    private void saveResourceIfMissing(String resourcePath) {
        File target = new File(getDataFolder(), resourcePath);
        if (!target.exists()) saveResource(resourcePath, false);
    }

    public ConfigManager getConfigManager() { return configManager; }
    public ArenaManager getArenaManager()   { return arenaManager; }
    public WeaponManager getWeaponManager() { return weaponManager; }
    public WeaponAbilityManager getAbilityManager() { return abilityManager; }
    public ConsumableManager getConsumableManager() { return consumableManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public ArchetypeManager getArchetypeManager() { return archetypeManager; }
    public PermanentUpgradeManager getUpgradeManager() { return upgradeManager; }
    public ShopManager getShopManager() { return shopManager; }
    public GameManager getGameManager()     { return gameManager; }
}
