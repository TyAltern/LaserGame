package me.tyalternative.laserGame.shop;

import me.tyalternative.laserGame.archetype.ArchetypeDefinition;
import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.economy.CurrencySource;
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
        rollConsumableSlots(session,0);
        rollSpecialSlots(session);
        return session;
    }

    private void rollConsumableSlots(ShopSession session, int pityLevel) {
        List<ConsumableDefinition> pool = consumableManager.getAllDefinitions();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < config.getShopConsumableSlots(); i++) {
            RarityPool.draw(pool, config.getRarityWeights(), pityLevel, config.getPityWeightShiftPerReroll())
                    .ifPresent(def -> ids.add(def.id()));
        }
        session.setConsumableSlotIds(ids);
    }

    private void rollSpecialSlots(ShopSession session) {
        List<SpecialSlotType> remaining = new ArrayList<>(List.of(SpecialSlotType.values()));
        SpecialSlotType excluded = remaining.remove(ThreadLocalRandom.current().nextInt(remaining.size()));
        session.setExcludedSpecialType(excluded);
        session.getSpecialSlotIds().clear();
        Collections.shuffle(remaining);

        for (SpecialSlotType type : remaining) {
            String id = rollSpecialSlotId(type);
            if (id != null) {
                session.getSpecialSlotIds().put(type, id);
            }
        }
    }

    private String rollSpecialSlotId(SpecialSlotType type) {
        return switch (type) {
            case WEAPON -> RarityPool.draw(weaponManager.getAllWeapons(), config.getRarityWeights(), 0, 0)
                    .map(WeaponType::id).orElse(null);
            case UPGRADE -> RarityPool.draw(upgradeManager.getAllDefinitions(), config.getRarityWeights(), 0, 0)
                    .map(PermanentUpgradeDefinition::id).orElse(null);
            case SKILL -> RarityPool.draw(skillManager.getAllDefinitions(), config.getRarityWeights(), 0, 0)
                    .map(SkillDefinition::id).orElse(null);
            case ARCHETYPE -> RarityPool.draw(archetypeManager.getAllDefinitions(), config.getRarityWeights(), 0, 0)
                    .map(ArchetypeDefinition::id).orElse(null);
        };
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
        rollConsumableSlots(session, session.getConsumableRerollCount());
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

        if (!gp.spendCurrency(def.price())) return false;

        boolean added = gp.getConsumables().add(def.id(), config.getShopDuplicatePolicy()); // TODO : MODIFY FOR DUPLICATE UPGRADE
        if (!added) {
            gp.addCurrency(def.price(), CurrencySource.REFOUND);
            return false;
        }
        return true;
    }

    public boolean purchaseSpecialSlot(GamePlayer gp, ShopSession session, SpecialSlotType type) {
        String id = session.getSpecialSlotIds().get(type);
        if (id == null) return false;

        return switch (type) {
            case WEAPON -> purchaseWeapon(gp, id);
            case UPGRADE -> purchaseUpgrade(gp, id);
            case SKILL -> purchaseSkill(gp, id);
            case ARCHETYPE -> purchaseArchetype(gp, id);
        };
    }

    private boolean purchaseWeapon(GamePlayer gp, String id) {
        Optional<WeaponType> typeOpt = weaponManager.getWeapon(id);
        if (typeOpt.isEmpty()) return false;
        WeaponType type = typeOpt.get();

        if (!gp.spendCurrency(type.price())) return false;
        gp.setWeapon(type);
        return true;
    }

    private boolean purchaseUpgrade(GamePlayer gp, String id) {
        Optional<PermanentUpgradeDefinition> defOpt = upgradeManager.getDefinition(id);
        if (defOpt.isEmpty()) return false;
        PermanentUpgradeDefinition def = defOpt.get();
        if (gp.ownsUpgrade(def.id())) return false; // Check si item deja possédé -> TODO : A modifier plus tard pour stack amélioration

        if (!gp.spendCurrency(def.price())) return false;
        PermanentUpgradeEffect effect = upgradeManager.getEffect(def.effectId()).orElse(null);
        gp.addPermanentUpgrade(def.id(), effect);
        return true;
    }

    private boolean purchaseSkill(GamePlayer gp, String id) {
        Optional<SkillDefinition> defOpt = skillManager.getDefinition(id);
        if (defOpt.isEmpty()) return false;
        SkillDefinition def = defOpt.get();

        if (!gp.spendCurrency(def.price())) return false;
        gp.setEquippedSkillId(def.id());
        return true;
    }

    private boolean purchaseArchetype(GamePlayer gp, String id) {
        Optional<ArchetypeDefinition> defOpt = archetypeManager.getDefinition(id);
        if (defOpt.isEmpty()) return false;
        ArchetypeDefinition def = defOpt.get();

        if (!gp.spendCurrency(def.price())) return false;
        ArchetypeEffect effect = archetypeManager.getEffect(def.effectId()).orElse(null);
        gp.setArchetype(def.id(), effect);
        return true;
    }
}
