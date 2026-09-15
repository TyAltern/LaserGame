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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GamePlayer {

    private final UUID uuid;
    private final Plugin plugin;
    private final ConfigManager config;
    private final WeaponAbilityManager abilityManager;

    private LaserWeapon weapon;
    private EffectiveWeaponStats effectiveStats;
    private Match match;
    private final List<StatModifier> activeModifiers = new ArrayList<>();
    private final EffectRegistry effects = new EffectRegistry();
    private final ConsumableInventory consumables;
    private final List<String> consumableStorage = new ArrayList<>();
    private int selectedSlot = 0;

    private StatModifier currentAbilityStatModifier;
    private PendingEffect currentAbilityPersistentEffect;

    private int lives;
    private boolean spectator = false;
    private boolean detectionImmune = false;

    private int roundWins = 0;
    private int currency = 0;


    private int shotsFired = 0;
    private int kills = 0;
    private int eliminations = 0;
    private int reloads = 0;
    private int consumablesUsed = 0;
    private int totalMoneyEarned = 0;

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

    public void setMatch(Match match) { this.match = match; }
    public Match getMatch() { return match; }

    public void setWeapon(WeaponType newType) {
        ownedWeaponIds.add(newType.id());
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
        ability.getPersistentEffect(this).ifPresent(eff -> {
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

    private final List<StatModifier> roundScopedModifiers = new ArrayList<>();
    private final List<Runnable> roundScopedCleanups = new ArrayList<>();

    public void addRoundStatModifier(StatModifier modifier) {
        activeModifiers.add(modifier);
        roundScopedModifiers.add(modifier);
        recalculateStats();
    }

    public void addRoundScopedCleanup(Runnable cleanup) {
        roundScopedCleanups.add(cleanup);
    }

    // Vies / round

    public int getLives() { return lives; }

    public boolean removeLife(int amount) {
        lives = Math.max(0, lives - amount);
        return lives == 0;
    }

    public boolean isSpectator() { return spectator; }
    public void setSpectator(boolean spectator) { this.spectator = spectator; }

    public boolean isDetectionImmune() { return detectionImmune; }
    public void setDetectionImmune(boolean detectionImmune) { this.detectionImmune = detectionImmune; }

    public int getRoundWins() { return roundWins; }
    public void incrementRoundWins() { roundWins++; }

    public void resetForNewRound(int startingLives) {
        this.lives = startingLives;
        this.spectator = false;
        this.detectionImmune = false;
        clearBet();
        this.effects.clear();

        if (!roundScopedModifiers.isEmpty()) {
            activeModifiers.removeAll(roundScopedModifiers);
            roundScopedModifiers.clear();
            recalculateStats();
        }
        if (!roundScopedCleanups.isEmpty()) {
            for (Runnable cleanup : roundScopedCleanups) {
                try {
                    cleanup.run();
                } catch (Exception e) {
                    plugin.getLogger().warning("Erreur lors du nettoyage d'un effet de round : " + e.getMessage());
                }
            }
            roundScopedCleanups.clear();
        }
    }

    // Économie

    // Paris économiques

    public enum BetType { WIN_X4, DOUBLE_EARNINGS }

    private BetType pendingBetType;
    private int pendingBetAmount;
    private int earningsSinceBet;

    public void placeBet(BetType type, int amount) {
        this.pendingBetType = type;
        this.pendingBetAmount = amount;
        this.earningsSinceBet = 0;
    }

    public BetType getPendingBetType() { return pendingBetType; }
    public int getPendingBetAmount() { return pendingBetAmount; }
    public int getEarningsSinceBet() { return earningsSinceBet; }

    public void clearBet() {
        this.pendingBetType = null;
        this.pendingBetAmount = 0;
        this.earningsSinceBet = 0;
    }

    public int getCurrency() { return currency; }

    public void addCurrency(int amount, CurrencySource source) {
        // TODO : Point d'accroche futur pour les modificateurs type "+15% d'argent sur les kills" :
        //  remplacer par currency += modifiers.applyCurrencyBonus(amount, source);
        if ((source == CurrencySource.SHOT_HIT || source == CurrencySource.ELIMINATION)
                && effectiveStats != null && effectiveStats.getCombatCurrencyMultiplier() != 1.0) {
            amount = (int) Math.round(amount * effectiveStats.getCombatCurrencyMultiplier());
        }
        if (pendingBetType == BetType.DOUBLE_EARNINGS && (source == CurrencySource.SHOT_HIT || source == CurrencySource.ELIMINATION)) {
            earningsSinceBet += amount;
            return;
        }
        currency += amount;
        totalMoneyEarned += amount;
    }

    public void deductCurrency(int amount) {
        currency = Math.max(0, currency - amount);
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

    public void  learnConsumable(String consumableId) {
        consumableStorage.add(consumableId);
    }


    public List<String> getConsumableStorage() { return new ArrayList<>(consumableStorage); }


    public boolean ownsConsumable(String consumableId) {
        return consumableStorage.contains(consumableId) || consumables.getSlotIds().contains(consumableId);
    }


    public boolean equipConsumableFromStorage(int storageIndex, int equippedSlotIndex) {
        if (storageIndex < 0 || storageIndex >= consumableStorage.size()) return false;
        if (equippedSlotIndex < 0 || equippedSlotIndex >= consumables.size()) return false;

        String incoming = consumableStorage.remove(storageIndex);
        String outgoing = consumables.get(equippedSlotIndex).orElse(null);
        consumables.set(equippedSlotIndex, incoming);
        if (outgoing != null) consumableStorage.add(outgoing); // TODO: test and see if it is ok or if I need to replace to the incoming slot
        return true;
    }


    public boolean unequipConsumableToStorage(int equippedSlotIndex) {
        if (equippedSlotIndex < 0 || equippedSlotIndex >= consumables.size()) return false;
        Optional<String> idOpt = consumables.get(equippedSlotIndex);
        if (idOpt.isEmpty()) return false;

        consumables.clear(equippedSlotIndex);
        consumableStorage.add(idOpt.get());
        return true;
    }


    public boolean swapEquippedConsumables(int slotA, int slotB) {
        if (slotA < 0 || slotA >= consumables.size()) return false;
        if (slotB < 0 || slotB >= consumables.size()) return false;
        if (slotA == slotB) return false;

        String a = consumables.get(slotA).orElse(null);
        String b = consumables.get(slotB).orElse(null);
        consumables.set(slotA, b);
        consumables.set(slotB, a);
        return true;
    }

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

    private int suppressedSwingTick = -1;

    public void suppressSwingForTick(int tick) {
        this.suppressedSwingTick = tick;
    }

    public boolean consumeSuppressedSwingTick(int tick) {
        if (suppressedSwingTick == tick) {
            suppressedSwingTick = -1;
            return true;
        }
        return false;
    }

    // ARCHETYPE

    private String equippedArchetypeId;
    private StatModifier currentArchetypeStatModifier;
    private PendingEffect currentArchetypePersistantEffect;

    public String getEquippedArchetypeId() {return equippedArchetypeId; }

    public void setArchetype(String archetypeId, ArchetypeEffect effect) {
        if (archetypeId != null) {
            ownedArchetypeIds.add(archetypeId);
        }
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

    public Set<String> getOwnedUpgradeIds() { return Set.copyOf(ownedUpgradeIds); }

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

    private final Set<String> ownedWeaponIds = new HashSet<>();
    private final Set<String> ownedSkillIds = new HashSet<>();
    private final Set<String> ownedArchetypeIds = new HashSet<>();

    public boolean ownsWeapon(String id) { return ownedWeaponIds.contains(id); }
    public boolean ownsSkill(String id) { return ownedSkillIds.contains(id); }
    public boolean ownsArchetype(String id) { return ownedArchetypeIds.contains(id); }

    public Set<String> getOwnedWeaponIds() { return Set.copyOf(ownedWeaponIds); }
    public Set<String> getOwnedSkillIds() { return Set.copyOf(ownedSkillIds); }
    public Set<String> getOwnedArchetypeIds() { return Set.copyOf(ownedArchetypeIds); }

    public void learnWeapon(String weaponId) { ownedWeaponIds.add(weaponId); }
    public void learnSkill(String skillId) { ownedSkillIds.add(skillId); }
    public void learnArchetype(String archetypeId) { ownedArchetypeIds.add(archetypeId); }

    private int bonusShopSlots = 0;

    public int getBonusShopSlots() { return bonusShopSlots; }
    public void addBonusShopSlot(int amount) { bonusShopSlots += amount; }

    // READY (phase shop)

    private boolean ready = false;

    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }

    // SNEAK (jauge de charge) : 20s de sneak max, recharge de 1s toutes les 5s hors sneak.

    private static final int MAX_SNEAK_CHARGE_TICKS = 400; // 20s
    private static final int SNEAK_REGEN_INTERVAL_TICKS = 100; // 5s
    private static final int SNEAK_REGEN_AMOUNT_TICKS = 20; // 1s

    private int sneakChargeTicks = MAX_SNEAK_CHARGE_TICKS;
    private int sneakRegenAccumulatorTicks = 0;

    public boolean hasSneakCharge() { return sneakChargeTicks > 0; }
    public int getSneakChargeTicks() { return sneakChargeTicks; }
    public int getMaxSneakChargeTicks() { return MAX_SNEAK_CHARGE_TICKS; }

    public void tickSneakCharge(boolean sneaking, int elapsedTicks) {
        if (sneaking) {
            sneakChargeTicks = Math.max(0, sneakChargeTicks - elapsedTicks);
            sneakRegenAccumulatorTicks = 0;
            return;
        }
        if (sneakChargeTicks >= MAX_SNEAK_CHARGE_TICKS) {
            sneakRegenAccumulatorTicks = 0;
            return;
        }
        sneakRegenAccumulatorTicks += elapsedTicks;
        while (sneakRegenAccumulatorTicks >= SNEAK_REGEN_INTERVAL_TICKS && sneakChargeTicks < MAX_SNEAK_CHARGE_TICKS) {
            sneakChargeTicks = Math.min(MAX_SNEAK_CHARGE_TICKS, sneakChargeTicks + SNEAK_REGEN_AMOUNT_TICKS);
            sneakRegenAccumulatorTicks -= SNEAK_REGEN_INTERVAL_TICKS;
        }
    }

    // PIERCING (les prochains tirs traversent les joueurs touchés au lieu de s'arrêter au premier)

    private int piercingShotsRemaining = 0;

    public boolean isPiercingActive() { return piercingShotsRemaining > 0; }
    public void grantPiercingShots(int amount) { piercingShotsRemaining += amount; }
    public void consumePiercingCharge() { if (piercingShotsRemaining > 0) piercingShotsRemaining--; }
    public int getPiercingShotsRemaining() { return piercingShotsRemaining; }

    // STATISTIQUES

    public int getShotsFired() { return shotsFired; }
    public void incrementShotsFired() { shotsFired++; }

    public int getKills() { return kills; }
    public void incrementKills() { kills++; }

    public int getEliminations() { return eliminations; }
    public void incrementEliminations() { eliminations++; }

    public int getReloads() { return reloads; }
    public void incrementReloads() { reloads++; }

    public int getConsumablesUsed() { return consumablesUsed; }
    public void incrementConsumablesUsed() { consumablesUsed++; }

    public int getTotalMoneyEarned() { return totalMoneyEarned; }
}
