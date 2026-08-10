package me.tyalternative.laserGame.shop;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.archetype.ArchetypeManager;
import me.tyalternative.laserGame.skill.SkillManager;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeManager;
import me.tyalternative.laserGame.weapon.WeaponManager;

public record ShopContext(
        ShopManager shopManager,
        ConsumableManager consumableManager,
        WeaponManager weaponManager,
        ArchetypeManager archetypeManager,
        SkillManager skillManager,
        PermanentUpgradeManager upgradeManager
) {
}
