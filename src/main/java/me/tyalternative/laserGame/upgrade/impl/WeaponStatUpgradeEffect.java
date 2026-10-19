package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import me.tyalternative.laserGame.weapon.StatModifier;

import java.util.Optional;

/** Bonus en pourcentage sur une stat d'arme : stat = range, hit_radius ou max_ammo. */
public class WeaponStatUpgradeEffect implements PermanentUpgradeEffect {

    private final StatModifier modifier;

    public WeaponStatUpgradeEffect(String stat, double percent) {
        double factor = 1.0 + percent;
        this.modifier = switch (stat.toLowerCase()) {
            case "hit_radius" -> stats -> stats.setHitRadius(stats.getHitRadius() * factor);
            case "max_ammo" -> stats -> stats.setMaxAmmo(Math.max(stats.getMaxAmmo() + 1, (int) Math.round(stats.getMaxAmmo() * factor)));
            default -> stats -> stats.setRange(stats.getRange() * factor);
        };
    }

    @Override
    public Optional<StatModifier> getStatModifier() {
        return Optional.of(modifier);
    }
}
