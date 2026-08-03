package me.tyalternative.laserGame.weapon;

import me.tyalternative.laserGame.weapon.impl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WeaponAbilityManager {

    private final Map<String, WeaponAbility> abilities = new HashMap<>();

    public WeaponAbilityManager() {
        register(new StreakDamageWeaponAbility());
        register(new MassiveMagazineWeaponAbility());
        register(new PassiveRegenWeaponAbility());
    }

    private void register(WeaponAbility ability) {
        abilities.put(ability.getId(), ability);
    }

    public Optional<WeaponAbility> get(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(abilities.get(id));
    }
}
