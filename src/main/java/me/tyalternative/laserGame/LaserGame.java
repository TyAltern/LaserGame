package me.tyalternative.laserGame;

import me.tyalternative.laserGame.UI.hud.ActionBarManager;
import me.tyalternative.laserGame.UI.shop.*;
import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.arena.ArenaManager;
import me.tyalternative.laserGame.command.LaserCommand;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.game.GameManager;
import me.tyalternative.laserGame.lang.Lang;
import me.tyalternative.laserGame.listeners.*;
import me.tyalternative.laserGame.shop.ConsumableManager;
import me.tyalternative.laserGame.shop.ShopManager;
import me.tyalternative.laserGame.skill.SkillManager;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeManager;
import me.tyalternative.laserGame.weapon.ShotTrailRenderer;
import me.tyalternative.laserGame.weapon.WeaponAbilityManager;
import me.tyalternative.laserGame.weapon.WeaponManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class LaserGame extends JavaPlugin {

    private static LaserGame instance;
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
        instance = this;

        saveDefaultConfig();
        saveDefaultResources();
        Lang.load(this, getConfig().getString("language", Lang.DEFAULT_LANGUAGE));

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
                archetypeManager, shopManager, consumableManager, skillManager, upgradeManager);

        getServer().getPluginManager().registerEvents(
                new PlayerInteractListener(weaponManager, gameManager), this);
        getServer().getPluginManager().registerEvents(
                new PlayerAttackListener(this, weaponManager, gameManager, trailRenderer), this);
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
        getServer().getPluginManager().registerEvents(
                new SneakChargeListener(gameManager), this);

        var laserCommand = new LaserCommand(this,configManager, gameManager, arenaManager, weaponManager, consumableManager,
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

        new HologramClickListener(this);
        new HologramHoverListener(this);
    }

    @Override
    public void onDisable() {
        HologramHoverListener.shutdown();
        getLogger().info("LaserGame désactivé.");
    }

    private static final List<String> DATA_FOLDERS =
            List.of("lang/", "arenas/", "weapons/", "consumables/", "skills/", "archetypes/", "upgrades/");

    private void saveDefaultResources() {
        try (JarFile jar = new JarFile(getFile())) {
            jar.stream()
                    .filter(entry -> !entry.isDirectory())
                    .map(JarEntry::getName)
                    .filter(name -> name.endsWith(".yml"))
                    .filter(name -> DATA_FOLDERS.stream().anyMatch(name::startsWith))
                    .forEach(this::saveResourceIfMissing);
        } catch (IOException e) {
            getLogger().severe("Impossible d'extraire les ressources par défaut : " + e.getMessage());
        }
    }

    private void saveResourceIfMissing(String resourcePath) {
        File target = new File(getDataFolder(), resourcePath);
        if (!target.exists()) saveResource(resourcePath, false);
    }


    public static LaserGame getInstance() { return instance; }
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
