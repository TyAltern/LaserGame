package me.tyalternative.laserGame.game;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.economy.CurrencySource;
import me.tyalternative.laserGame.effect.EffectRegistry;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.shop.ConsumableInventory;
import me.tyalternative.laserGame.shop.ShopSession;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import me.tyalternative.laserGame.weapon.EffectiveWeaponStats;
import me.tyalternative.laserGame.weapon.LaserWeapon;
import me.tyalternative.laserGame.weapon.StatModifier;
import me.tyalternative.laserGame.weapon.WeaponAbility;
import me.tyalternative.laserGame.weapon.WeaponAbilityManager;
import me.tyalternative.laserGame.weapon.WeaponType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GamePlayer {

    private final UUID uuid;
    private final Plugin plugin;
    private final ConfigManager config;
    private final WeaponAbilityManager abilityManager;

    private LaserWeapon weapon;
    private EffectiveWeaponStats effectiveStats;
    private final List<StatModifier> activeModifiers = new ArrayList<>();
    private final EffectRegistry effects = new EffectRegistry();
    private final ConsumableInventory consumables;
    private int selectedSlot = 0;

    private StatModifier currentAbilityStatModifier;
    private PendingEffect currentAbilityPersistentEffect;

    private int lives;
    private boolean spectator = false;

    private int roundWins = 0;
    private int currency = 0;

    public GamePlayer(UUID uuid, Plugin plugin, ConfigManager config, WeaponAbilityManager abilityManager,
                      WeaponType defaultWeapon, int baseItemSlots) {
        this.uuid = uuid;
        this.plugin = plugin;
        this.config = config;
        this.abilityManager = abilityManager;
        this.consumables = new ConsumableInventory(baseItemSlots);
        setWeapon(defaultWeapon);
    }

    public Player getPlayer() { return Bukkit.getPlayer(uuid); }
    public UUID getUuid() { return uuid; }
    public LaserWeapon getWeapon() { return weapon; }
    public WeaponType getWeaponType() { return weapon.getType(); }
    public EffectiveWeaponStats getEffectiveStats() { return effectiveStats; }

    public void setWeapon(WeaponType newType) {
        if (weapon != null) {
            weapon.stop();
        }
        detachCurrentAbility();

        this.effectiveStats = new EffectiveWeaponStats(newType);
        attachAbility(newType);
        this.effectiveStats.recalculate(activeModifiers);
        this.weapon = new LaserWeapon(plugin, config, newType, uuid, this::getEffectiveStats, effects);
    }

    private void detachCurrentAbility() {
        if (currentAbilityStatModifier != null) {
            activeModifiers.remove(currentAbilityStatModifier);
            currentAbilityStatModifier = null;
        }
        if (currentAbilityPersistentEffect != null) {
            effects.remove(currentAbilityPersistentEffect);
            currentAbilityPersistentEffect = null;
        }
    }

    private void attachAbility(WeaponType type) {
        if (type.specialAbilityId() == null) return;

        abilityManager.get(type.specialAbilityId()).ifPresent(this::attachAbility);
    }

    private void attachAbility(WeaponAbility ability) {
        ability.getStatModifier().ifPresent(mod -> {
            currentAbilityStatModifier = mod;
            activeModifiers.add(mod);
        });
        ability.getPersistentEffect().ifPresent(eff -> {
            currentAbilityPersistentEffect = eff;
            effects.addPermanent(eff);
        });
    }

    private void recalculateStats() {
        if (effectiveStats != null) {
            effectiveStats.recalculate(activeModifiers);
        }
    }

    public void addStatModifier(StatModifier modifier) {
        activeModifiers.add(modifier);
        recalculateStats();
    }

    public void removeStatModifier(StatModifier modifier) {
        activeModifiers.remove(modifier);
        recalculateStats();
    }

    // Vies / round

    public int getLives() { return lives; }

    public boolean removeLife(int amount) {
        lives = Math.max(0, lives - amount);
        return lives == 0;
    }

    public boolean isSpectator() { return spectator; }
    public void setSpectator(boolean spectator) { this.spectator = spectator; }

    public int getRoundWins() { return roundWins; }
    public void incrementRoundWins() { roundWins++; }

    public void resetForNewRound(int startingLives) {
        this.lives = startingLives;
        this.spectator = false;
        this.effects.clear();
    }

    // Économie

    public int getCurrency() { return currency; }

    public void addCurrency(int amount, CurrencySource source) {
        // TODO : Point d'accroche futur pour les modificateurs type "+15% d'argent sur les kills" :
        //  remplacer par currency += modifiers.applyCurrencyBonus(amount, source);
        currency += amount;
    }

    public boolean spendCurrency(int amount) {
        if (currency < amount) return false;
        currency -= amount;
        return true;
    }

    // EFFETS / CONSUMABLES

    public EffectRegistry getEffects() { return effects; }
    public ConsumableInventory getConsumables() { return consumables; }
    public int getSelectedSlot() { return selectedSlot; }
    public void setSelectedSlot(int slot) { this.selectedSlot = slot; }

    // COMPETENCE

    private String equippedSkillId;
    private long skillCooldownReadyAtMillis = 0;
    private long skillCooldownDurationTicks = 0;

    public String getEquippedSkillId() { return equippedSkillId; }

    public void setEquippedSkillId(String skillId) {
        this.equippedSkillId = skillId;
        this.skillCooldownReadyAtMillis = 0;
        this.skillCooldownDurationTicks = 0;
    }

    public boolean isSkillReady() {
        return System.currentTimeMillis() >= skillCooldownReadyAtMillis;
    }
    public void startSkillCooldown(long cooldownTicks) {
        this.skillCooldownReadyAtMillis = System.currentTimeMillis() + cooldownTicks * 50L;
        this.skillCooldownDurationTicks = cooldownTicks;
    }

    public long getSkillCooldownDurationTicks() {
        return skillCooldownDurationTicks;
    }

    public long getSkillCooldownRemainingTicks() {
        return Math.max(0, (skillCooldownReadyAtMillis - System.currentTimeMillis()) / 50L);
    }

    // ARCHETYPE

    private String equippedArchetypeId;
    private StatModifier currentArchetypeStatModifier;
    private PendingEffect currentArchetypePersistantEffect;

    public String getEquippedArchetypeId() {return equippedArchetypeId; }

    public void setArchetype(String archetypeId, ArchetypeEffect effect) {
        detachCurrentArchetype();
        this.equippedArchetypeId = archetypeId;

        if (effect != null) {
            effect.getStatModifier().ifPresent(mod -> {
                currentArchetypeStatModifier = mod;
                activeModifiers.add(mod);
            });
            effect.getPersistentEffect().ifPresent(eff -> {
                currentArchetypePersistantEffect = eff;
                effects.addPermanent(eff);
            });
        }
        recalculateStats();
    }

    private void  detachCurrentArchetype() {
        if (currentArchetypeStatModifier != null) {
            activeModifiers.remove(currentArchetypeStatModifier);
            currentArchetypeStatModifier = null;
        }
        if (currentArchetypePersistantEffect != null) {
            effects.remove(currentArchetypePersistantEffect);
            currentArchetypePersistantEffect = null;
        }
    }

    // AMELIORATION PERMANENTES

    private final Set<String> ownedUpgradeIds = new HashSet<>();

    public boolean ownsUpgrade(String upgradeId) {
        return ownedUpgradeIds.contains(upgradeId);
    }

    public boolean addPermanentUpgrade(String upgradeId, PermanentUpgradeEffect effect) {
        if (ownedUpgradeIds.contains(upgradeId)) {
            return false; // TODO : VOIR POUR AJOUTER UNE AMELIORATION QUI PERMET DE STACK LES AMELIORATIONS.
        }
        ownedUpgradeIds.add(upgradeId);

        if (effect != null) {
            effect.getStatModifier().ifPresent(activeModifiers::add);
            effect.getPersistentEffect().ifPresent(effects::addPermanent);
            effect.onPurchase(this);
        }
        recalculateStats();
        return true;
    }

    // SESSION SHOP COURANTE

    private ShopSession currentShopSession;

    public ShopSession getShopSession() {
        return currentShopSession;
    }

    public void setShopSession(ShopSession session) {
        this.currentShopSession = session;
    }
}
