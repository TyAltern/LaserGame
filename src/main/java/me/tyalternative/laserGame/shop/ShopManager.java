package me.tyalternative.laserGame.shop;

import me.tyalternative.laserGame.archetype.ArchetypeDefinition;
import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.skill.SkillDefinition;
import me.tyalternative.laserGame.skill.SkillManager;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeDefinition;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeManager;
import me.tyalternative.laserGame.weapon.WeaponManager;
import me.tyalternative.laserGame.weapon.WeaponType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class ShopManager {

    private static final int MAX_CONSUMABLE_SLOTS = 6;

    private final ConfigManager config;
    private final ConsumableManager consumableManager;
    private final SkillManager skillManager;
    private final ArchetypeManager archetypeManager;
    private final PermanentUpgradeManager upgradeManager;
    private final WeaponManager weaponManager;

    public ShopManager(ConfigManager config, ConsumableManager consumableManager, SkillManager skillManager,
                       ArchetypeManager archetypeManager, PermanentUpgradeManager upgradeManager,
                       WeaponManager weaponManager) {
        this.config = config;
        this.consumableManager = consumableManager;
        this.skillManager = skillManager;
        this.archetypeManager = archetypeManager;
        this.upgradeManager = upgradeManager;
        this.weaponManager = weaponManager;
    }

    public ShopSession createSession(GamePlayer gp) {
        ShopSession session = new ShopSession();
        rollConsumableSlots(gp, session,0);
        if (gp.hasFreeFirstRollAfterWin() && gp.hasWonLastRound()) {
            session.markAlConsumableSlotsFree();
        }
        rollSpecialSlots(gp, session);
        return session;
    }

    private void rollConsumableSlots(GamePlayer gp, ShopSession session, int pityLevel) {
        int slotCount = Math.min(MAX_CONSUMABLE_SLOTS, config.getShopConsumableSlots() + gp.getBonusShopSlots());

        boolean allowDuplicates = gp.allowsDuplicateConsumables();
        List<ConsumableDefinition> pool = new ArrayList<>(consumableManager.getAllDefinitions());
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < slotCount; i++) {
            Optional<ConsumableDefinition> drawn = RarityPool.draw(pool, config.getRarityWeights(), pityLevel, config.getPityWeightShiftPerReroll());
            if (drawn.isEmpty()) continue;
            ids.add(drawn.get().id());
            if (!allowDuplicates) pool.remove(drawn.get()); // FIX: TODO Seams o need a while instead of a fori.
        }
        session.setConsumableSlotIds(ids);
    }

    private void rollSpecialSlots(GamePlayer gp, ShopSession session) {
        List<SpecialSlotType> remaining = new ArrayList<>(List.of(SpecialSlotType.values()));
        SpecialSlotType excluded = remaining.remove(ThreadLocalRandom.current().nextInt(remaining.size()));
        session.setExcludedSpecialType(excluded);
        session.getSpecialSlotIds().clear();
        Collections.shuffle(remaining);

        for (SpecialSlotType type : remaining) {
            String id = rollSpecialSlotId(gp, type);
            if (id != null) {
                session.getSpecialSlotIds().put(type, id);
            }
        }
    }

    private String rollSpecialSlotId(GamePlayer gp, SpecialSlotType type) {
        return switch (type) {
            case WEAPON -> RarityPool.draw(weaponManager.getAllBuyableWeapons(), config.getRarityWeights(), 0, 0)
                    .map(WeaponType::id).orElse(null);
            case UPGRADE -> RarityPool.draw(upgradeManager.getAvailableFor(gp), config.getRarityWeights(), 0, 0)
                    .map(PermanentUpgradeDefinition::id).orElse(null);
            case SKILL -> RarityPool.draw(skillManager.getAllDefinitions(), config.getRarityWeights(), 0, 0)
                    .map(SkillDefinition::id).orElse(null);
            case ARCHETYPE -> RarityPool.draw(archetypeManager.getAllDefinitions(), config.getRarityWeights(), 0, 0)
                    .map(ArchetypeDefinition::id).orElse(null);
        };
    }

    public int getEffectivePrice(GamePlayer gp, int basePrice) {
        double discount = gp.getShopDiscount();
        if (discount <= 0.0) return basePrice;
        return Math.max(0, (int) Math.round(basePrice * (1.0 - discount)));
    }

    public int getConsumablePrice(GamePlayer gp, ShopSession session, int slotIndex, ConsumableDefinition def) {
        if (session != null && session.isConsumablesSlotFree(slotIndex)) return 0;
        return getEffectivePrice(gp, def.price());
    }

    public int getConsumableRerollCost(ShopSession session) {
        return config.getRerollBaseCost() + session.getConsumableRerollCount() * config.getRerollCostIncrement();
    }

    public boolean rerollConsumables(GamePlayer gp, ShopSession session) {
        int cost = getConsumableRerollCost(session);
        if (!gp.spendCurrency(cost)) {
            return false;
        }
        session.incrementConsumableRerollCount();
        session.clearFreeConsumablesSlots();
        rollConsumableSlots(gp, session, session.getConsumableRerollCount());
        return true;
    }

    public boolean purchaseConsumableSlot(GamePlayer gp, ShopSession session, int slotIndex) {
        List<String> ids = session.getConsumableSlotIds();
        if (slotIndex < 0 || slotIndex >= ids.size()) return false;
        String id = ids.get(slotIndex);
        if (id == null) return false;

        Optional<ConsumableDefinition> defOpt = consumableManager.getDefinition(id);
        if (defOpt.isEmpty()) return false;
        ConsumableDefinition def = defOpt.get();

        if (config.getShopDuplicatePolicy() == DuplicatePolicy.BLOCK_PURCHASE
                && !gp.allowsDuplicateConsumables()
                && gp.ownsConsumable(def.id())) return false;

        if (!gp.spendCurrency(getConsumablePrice(gp, session, slotIndex, def))) return false;

        gp.learnConsumable(def.id());
        ids.set(slotIndex, null);
        return true;
    }




    public boolean purchaseSpecialSlot(GamePlayer gp, ShopSession session, SpecialSlotType type) {
        String id = session.getSpecialSlotIds().get(type);
        if (id == null) return false;

        boolean success = switch (type) {
            case WEAPON -> purchaseWeapon(gp, id);
            case UPGRADE -> purchaseUpgrade(gp, id);
            case SKILL -> purchaseSkill(gp, id);
            case ARCHETYPE -> purchaseArchetype(gp, id);
        };

        if (success) {
            session.getSpecialSlotIds().put(type,null);
        }
        return success;
    }

    private boolean purchaseWeapon(GamePlayer gp, String id) {
        if (gp.ownsWeapon(id)) return false;
        Optional<WeaponType> typeOpt = weaponManager.getWeapon(id);
        if (typeOpt.isEmpty()) return false;
        WeaponType type = typeOpt.get();

        if (!gp.spendCurrency(type.price())) return false;
        gp.learnWeapon(type.id());
        return true;
    }

    private boolean purchaseUpgrade(GamePlayer gp, String id) {
        Optional<PermanentUpgradeDefinition> defOpt = upgradeManager.getDefinition(id);
        if (defOpt.isEmpty()) return false;
        PermanentUpgradeDefinition def = defOpt.get();
        if (gp.ownsUpgrade(def.id())) return false;
        if (!upgradeManager.isUnlocked(gp, def)) return false;

        if (!gp.spendCurrency(def.price())) return false;
        PermanentUpgradeEffect effect = upgradeManager.getEffect(def).orElse(null);
        gp.addPermanentUpgrade(def.id(), effect);
        return true;
    }

    private boolean purchaseSkill(GamePlayer gp, String id) {
        if (gp.ownsSkill(id)) return false;
        Optional<SkillDefinition> defOpt = skillManager.getDefinition(id);
        if (defOpt.isEmpty()) return false;
        SkillDefinition def = defOpt.get();

        if (!gp.spendCurrency(def.price())) return false;
        gp.learnSkill(def.id());
        return true;
    }

    private boolean purchaseArchetype(GamePlayer gp, String id) {
        if (gp.ownsArchetype(id)) return false;
        Optional<ArchetypeDefinition> defOpt = archetypeManager.getDefinition(id);
        if (defOpt.isEmpty()) return false;
        ArchetypeDefinition def = defOpt.get();

        if (!gp.spendCurrency(def.price())) return false;
        gp.learnArchetype(def.id());
        return true;
    }

    public boolean equipWeapon(GamePlayer gp, String weaponId) {
        if (weaponId == null || !gp.ownsWeapon(weaponId)) return false;
        Optional<WeaponType> typeOpt = weaponManager.getWeapon(weaponId);
        if (typeOpt.isEmpty()) return false;
        gp.setWeapon(typeOpt.get());
        return true;
    }

    public boolean equipSkill(GamePlayer gp, String skillId) {
        if (skillId != null && !gp.ownsSkill(skillId)) return false;
        gp.setEquippedSkillId(skillId);
        return true;
    }

    public boolean equipArchetype(GamePlayer gp, String archetypeId) {
        if (archetypeId == null) {
            gp.setArchetype(null, null);
            return true;
        }
        if (!gp.ownsArchetype(archetypeId)) return false;

        Optional<ArchetypeDefinition> defOpt = archetypeManager.getDefinition(archetypeId);
        if (defOpt.isEmpty()) return false;
        ArchetypeEffect effect = archetypeManager.getEffect(defOpt.get()).orElse(null);
        gp.setArchetype(defOpt.get().id(), effect);
        return true;
    }
}
