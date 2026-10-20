package me.tyalternative.laserGame.game;

import me.tyalternative.laserGame.effect.ConsumableEffect;
import me.tyalternative.laserGame.shop.ConsumableDefinition;
import me.tyalternative.laserGame.weapon.WeaponAttributeKeys;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

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

    public void recalculateStats() {
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

    public void addRoundWins(int amount) { roundWins += amount; }

    public void resetForNewRound(int startingLives) {
        this.lives = computeStartingLives(startingLives);
        this.lostLifeThisRound = false;
        cancelStealth();
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
        if (outgoing != null) consumableStorage.add(outgoing);
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
    private ArchetypeEffect currentArchetypeEffect;

    public String getEquippedArchetypeId() {return equippedArchetypeId; }

    public void setArchetype(String archetypeId, ArchetypeEffect effect) {
        if (archetypeId != null) {
            ownedArchetypeIds.add(archetypeId);
        }
        detachCurrentArchetype();
        this.equippedArchetypeId = archetypeId;

        if (effect != null) {
            currentArchetypeEffect = effect;
            effect.getStatModifier(this).ifPresent(mod -> {
                currentArchetypeStatModifier = mod;
                activeModifiers.add(mod);
            });
            effect.getPersistentEffect(this).ifPresent(eff -> {
                currentArchetypePersistantEffect = eff;
                effects.addPermanent(eff);
            });
            effect.onEquip(this);
        }
        recalculateStats();
    }

    private void  detachCurrentArchetype() {
        if (currentArchetypeEffect != null) {
            currentArchetypeEffect.onUnequip(this);
            currentArchetypeEffect = null;
        }

        archetypeLifeModifier = 0;
        archetypeFixedLives = null;
        archetypeSneakChargeBonus = 0.0;
        reloadPaymentFullCost = 0;
        lives = computeStartingLives(config.getStartingLives());

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
            return false;
        }
        ownedUpgradeIds.add(upgradeId);

        if (effect != null) {
            effect.getStatModifier().ifPresent(activeModifiers::add);
            effect.getPersistentEffect(this).ifPresent(effects::addPermanent);
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

    //  VIES, SOINS, ARCHETYPES

    private int archetypeLifeModifier = 0;
    private Integer archetypeFixedLives = null;
    private double archetypeSneakChargeBonus = 0.0;
    private int reloadPaymentFullCost = 0;

    public int computeStartingLives(int configStartingLives) {
        int base = archetypeFixedLives != null ? archetypeFixedLives : configStartingLives + archetypeLifeModifier;
        return Math.clamp(base + bonusStartingLives, 1, 10);
    }

    public int getMaxLives() {
        return computeStartingLives(config.getStartingLives());
//        return archetypeFixedLives != null ? archetypeFixedLives + bonusStartingLives : 10;
    }

    public void setArchetypeLives(int modifier, Integer fixedLives) {
        this.archetypeLifeModifier = modifier;
        this.archetypeFixedLives = fixedLives;
        this.lives = getMaxLives();
    }

    public int heal(int amount) {
        int before = lives;
        lives = Math.min(getMaxLives(), lives + amount); // TODO : FIX THIS MAGIC NUMBER AND ALLOWED A PLAYER TO GO UP TO 10 HP MAX BUT NOT IF RESTRAIN BY A ARCHETYPE
        return lives - before;
    }

    public void setArchetypeSneakChargeBonus(double bonus) { this.archetypeSneakChargeBonus = bonus; }

    public int getReloadPaymentFullCOst() { return reloadPaymentFullCost; }
    public void setReloadPaymentFullCost(int cost) { this.reloadPaymentFullCost = cost; }

    private Collection<GamePlayer> alivePlayersView = List.of();

    public Collection<GamePlayer> getAlivePlayers() { return alivePlayersView; }
    public void setAlivePlayersView(Collection<GamePlayer> alivePlayers) { this.alivePlayersView = alivePlayers; }

    public int takeCurrency(int max) {
        int taken = Math.clamp(currency, 0, max);
        currency -= taken;
        return taken;
    }

    //  DERNIER CONSOMMABLE (Bis Repetita)

    private ConsumableDefinition lastConsumableDefinition;
    private ConsumableEffect lastConsumableEffect;
    private boolean lastConsumableRepeated = false;

    public void setLastConsumable(ConsumableDefinition def, ConsumableEffect effect) {
        this.lastConsumableDefinition = def;
        this.lastConsumableEffect = effect;
        this.lastConsumableRepeated = false;
    }

    public ConsumableDefinition getLastConsumableDefinition() { return lastConsumableDefinition; }
    public ConsumableEffect getLastConsumableEffect() { return lastConsumableEffect; }
    public boolean isLastConsumableRepeated() { return lastConsumableRepeated; }
    public void markLastConsumableRepeated() { this.lastConsumableRepeated = true; }

    // FURTIVITE (Voile Spectral)

    private boolean stealthActive = false;
    private long stealthLockAfterTicks = 0;
    private long shootBlockedUntilTick = 0;
    private BukkitTask stealthTask;

    public boolean isShootingBlocked() {
        return stealthActive || Bukkit.getCurrentTick() < shootBlockedUntilTick;
    }

    public boolean isStealthActive() { return stealthActive; }

    public void startStealth(long durationTicks, long lockAfterTicks) {
        cancelStealth();
        Player online = getPlayer();
        if (online == null) return;
        stealthActive = true;
        stealthLockAfterTicks = lockAfterTicks;
        online.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                (int) durationTicks + 20, 0, false, false));
        stealthTask = Bukkit.getScheduler().runTaskLater(plugin, this::endStealth, durationTicks);
    }

    public void endStealth() {
        if (!stealthActive) return;
        stealthActive = false;
        if (stealthTask != null) {
            stealthTask.cancel();
            stealthTask = null;
        }
        Player online = getPlayer();
        if (online != null) online.removePotionEffect(PotionEffectType.INVISIBILITY);
        shootBlockedUntilTick = Bukkit.getCurrentTick() + stealthLockAfterTicks;
    }

    public void cancelStealth() {
        boolean wasActive = stealthActive;
        stealthActive = false;
        if (stealthTask != null) {
            stealthTask.cancel();
            stealthTask = null;
        }
        shootBlockedUntilTick = 0;
        if (wasActive) {
            Player online = getPlayer();
            if (online != null) online.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    // BONUS DES AMELIORATIONS

    private double skillCooldownReduction = 0.0;

    public void raiseSkillCooldownReduction(double reduction) {
        skillCooldownReduction = Math.clamp(reduction, skillCooldownReduction, 0.95);
    }

    public double getSkillCooldownMultiplier() { return 1.0 - skillCooldownReduction; }

    private int eliminationRewardBonus = 0;
    private int hitRewardBonus = 0;
    private int firstBloodRewardBonus = 0;

    public int getEliminationRewardBonus() { return eliminationRewardBonus; }
    public void addEliminationRewardBonus(int amount) { eliminationRewardBonus += amount; }
    public int getHitRewardBonus() { return hitRewardBonus; }
    public void addHitRewardBonus(int amount) { hitRewardBonus += amount; }
    public int getFirstBloodRewardBonus() { return firstBloodRewardBonus; }
    public void addFirstBloodRewardBonus(int amount) { firstBloodRewardBonus += amount; }

    private long radarDelayReductionTicks = 0;

    public void raiseRadarDelayReduction(long ticks) {
        radarDelayReductionTicks = Math.max(radarDelayReductionTicks, ticks);
    }

    public long getRadarDelayTicks(long baseTicks) {
        return Math.max(0L, baseTicks - radarDelayReductionTicks);
    }

    // Boutique
    private boolean allowDuplicateConsumables = false;
    private double shopDiscount = 0.0;
    private boolean freeFirstRollAfterWin = false;
    private boolean wonLastRound = false;

    public boolean allowsDuplicateConsumables() { return allowDuplicateConsumables; }
    public void setAllowDuplicateConsumables(boolean allow) { this.allowDuplicateConsumables = allow; }

    public double getShopDiscount() { return shopDiscount; }
    public void raiseShopDiscount(double discount) { shopDiscount = Math.max(shopDiscount, Math.min(1.0, discount)); }

    public boolean hasFreeFirstRollAfterWin() { return freeFirstRollAfterWin; }
    public void setFreeFirstRollAfterWin(boolean value) { this.freeFirstRollAfterWin = value; }
    public boolean hasWonLastRound() { return wonLastRound; }
    public void setWonLastRound(boolean wonLastRound) { this.wonLastRound = wonLastRound; }

    // Vies et points de victoire
    private int bonusStartingLives = 0;
    private int flawlessBonusWins = 0;
    private boolean lostLifeThisRound = false;

    public void addBonusStartingLives(int amount) {
        bonusStartingLives += amount;
        lives += amount;
    }

    public int getFlawlessBonusWins() { return flawlessBonusWins; }
    public void addFlawlessBonusWins(int amount) { flawlessBonusWins += amount; }
    public boolean hasLostLifeThisRound() { return lostLifeThisRound; }
    public void markLifeLost() { this.lostLifeThisRound = true; }

    // Accroupi
    private double sneakChargeBonus = 0.0;
    private long sneakStartTick = -1;

    public void raiseSneakChargeBonus(double bonus) { sneakChargeBonus = Math.max(sneakChargeBonus, bonus); }

    public long getMaxSneakChargeTicks() {
        return Math.round(config.getMaxSneakChargeTicks() * (1.0 + sneakChargeBonus + archetypeSneakChargeBonus));
    }

    /** Tick serveur où le joueur s'est accroupi (-1 s'il ne l'est pas). */
    public long getSneakStartTick() { return sneakStartTick; }
    public void setSneakStartTick(long tick) { this.sneakStartTick = tick; }

    /** Réapplique la vitesse de déplacement issue des stats (utile quand un effet la modifie en cours de round). */
    public void refreshMovementSpeed() {
        Player online = getPlayer();
        if (online == null || spectator || effectiveStats == null) return;
        AttributeInstance speedAttr = online.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        NamespacedKey key = WeaponAttributeKeys.speedModifier(plugin);
        speedAttr.removeModifier(key);
        double modifier = effectiveStats.getMovementSpeedModifier();
        if (modifier != 0.0) {
            speedAttr.addModifier(new AttributeModifier(key, modifier, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }
    }

    // READY (phase shop)

    private boolean ready = false;

    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }

    // SNEAK (jauge de charge) : 20s de sneak max, recharge de 1s toutes les 5s hors sneak.

    private boolean sneakTimed = false;
    private long sneakChargeTicks = 0;
    private long sneakRegenAccumulatorTicks = 0;
    private AttributeModifier sneakSpeedModifier;

    public AttributeModifier getSneakSpeedModifier() { return sneakSpeedModifier; }
    public void setSneakSpeedModifier(AttributeModifier sneakSpeedModifier) { this.sneakSpeedModifier = sneakSpeedModifier; }

    public boolean isSneakTimed() { return sneakTimed; }
    public void setSneakTimed(boolean sneakTimed) { this.sneakTimed = sneakTimed; }

    public boolean hasSneakCharge() { return sneakChargeTicks > 0 || !sneakTimed; }

    public long getSneakChargeTicks() { return sneakChargeTicks; }
    public void setSneakChargeTicks(long sneakChargeTicks) { this.sneakChargeTicks = sneakChargeTicks; }

    public void tickSneakCharge(boolean sneaking, long elapsedTicks) {
        if (!isSneakTimed()) return;
        if (sneaking) {
            sneakChargeTicks = Math.max(0, sneakChargeTicks - elapsedTicks);
            sneakRegenAccumulatorTicks = 0;
            return;
        }
        if (sneakChargeTicks >= getMaxSneakChargeTicks()) {
            sneakRegenAccumulatorTicks = 0;
            return;
        }
        sneakRegenAccumulatorTicks += elapsedTicks;
        while (sneakRegenAccumulatorTicks >= config.getSneakRegenIntervalTicks() && sneakChargeTicks < getMaxSneakChargeTicks()) {
            sneakChargeTicks = Math.min(getMaxSneakChargeTicks(), sneakChargeTicks + config.getSneakRegenAmountTicks());
            sneakRegenAccumulatorTicks -= config.getSneakRegenIntervalTicks();
        }
    }

    // PIERCING (les prochains tirs traversent les joueurs touchés au lieu de s'arrêter au premier)

    private int piercingShotsRemaining = 0;

    public boolean isPiercingActive() { return piercingShotsRemaining > 0; }
    public void grantPiercingShots(int amount) { piercingShotsRemaining += amount; }
    public void consumePiercingCharge() { if (piercingShotsRemaining > 0) piercingShotsRemaining--; }
    public int getPiercingShotsRemaining() { return piercingShotsRemaining; }

    // RADAR DISPLAYS

    private boolean canSeeRadar = false;
    private boolean doesSeeAmmoNotUsedEnoughMessage = false;

    public boolean canSeeRadar() { return canSeeRadar; }
    public void setCanSeeRadar(boolean canSeeRadar) { this.canSeeRadar = canSeeRadar; }

    public boolean doesSeeAmmoNotUsedEnoughMessage() { return doesSeeAmmoNotUsedEnoughMessage; }
    public void setDoesSeeAmmoNotUsedEnoughMessage(boolean see) { this.doesSeeAmmoNotUsedEnoughMessage = see; }

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
