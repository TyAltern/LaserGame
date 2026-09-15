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

        // Unique
        register(new ReloadGateWeaponAbility());
        register(new PausableReloadWeaponAbility());
        register(new FireRateRampWeaponAbility());
        register(new MoneyBoostMissPenaltyWeaponAbility());
        register(new RadiusRampWeaponAbility());

        // Rare
        register(new ReviveOnFirstEliminationWeaponAbility());
        register(new RefundAmmoOnMissWeaponAbility());
        register(new RoundWinStreakBonusWeaponAbility());
        register(new RespawnGraceWeaponAbility());
        register(new HighlightTopKillerWeaponAbility());
        register(new GlowSelfOnMissWeaponAbility());

        // Légendaire
        register(new LuckyMagazineWeaponAbility());
    }

    private void register(WeaponAbility ability) {
        abilities.put(ability.getId(), ability);
    }

    public Optional<WeaponAbility> get(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(abilities.get(id));
    }
}
