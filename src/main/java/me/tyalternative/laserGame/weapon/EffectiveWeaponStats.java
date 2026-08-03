package me.tyalternative.laserGame.weapon;

import java.util.List;

public class EffectiveWeaponStats {

    private final WeaponType base;

    private int maxAmmo;
    private long shotCooldownTicks;
    private long reloadCooldownTicks;
    private double range;
    private double hitRadius;
    private int livesPerHit;
    private double movementSpeedModifier;
    private boolean reloadDisabled;
    private int passiveRegenAmount;
    private long passiveRegenIntervalTicks;

    public EffectiveWeaponStats(WeaponType base) {
        this.base = base;
        recalculate(List.of());
    }

    public void recalculate(List<StatModifier> activeModifiers) {
        maxAmmo = base.maxAmmo();
        shotCooldownTicks = base.shotCooldownTicks();
        reloadCooldownTicks = base.reloadCooldownTicks();
        range = base.range();
        hitRadius = base.hitRadius();
        livesPerHit = base.livesPerHit();
        movementSpeedModifier = base.movementSpeedModifier();
        reloadDisabled = false;
        passiveRegenAmount = 0;
        passiveRegenIntervalTicks = 0;

        for (StatModifier mod : activeModifiers) {
            mod.apply(this);
        }
    }

    public WeaponType getBase() { return base; }

    public int getMaxAmmo()                                                  { return maxAmmo; }
    public void setMaxAmmo(int maxAmmo)                                      { this.maxAmmo = maxAmmo; }

    public long getShotCooldownTicks()                                       { return shotCooldownTicks; }
    public void setShotCooldownTicks(long shotCooldownTicks)                 { this.shotCooldownTicks = shotCooldownTicks; }

    public long getReloadCooldownTicks()                                     { return reloadCooldownTicks; }
    public void setReloadCooldownTicks(long reloadCooldownTicks)             { this.reloadCooldownTicks = reloadCooldownTicks; }

    public double getRange()                                                 { return range; }
    public void setRange(double range)                                       { this.range = range; }

    public double getHitRadius()                                             { return hitRadius; }
    public void setHitRadius(double hitRadius)                               { this.hitRadius = hitRadius; }

    public int getLivesPerHit()                                              { return livesPerHit; }
    public void setLivesPerHit(int livesPerHit)                              { this.livesPerHit = livesPerHit; }

    public double getMovementSpeedModifier()                                 { return movementSpeedModifier; }
    public void setMovementSpeedModifier(double movementSpeedModifier)       { this.movementSpeedModifier = movementSpeedModifier; }

    public boolean isReloadDisabled()                                        { return reloadDisabled; }
    public void setReloadDisabled(boolean reloadDisabled)                    { this.reloadDisabled = reloadDisabled; }

    public int getPassiveRegenAmount()                                       { return passiveRegenAmount; }
    public void setPassiveRegenAmount(int passiveRegenAmount)                { this.passiveRegenAmount = passiveRegenAmount; }

    public long getPassiveRegenIntervalTicks()                               { return passiveRegenIntervalTicks; }
    public void setPassiveRegenIntervalTicks(long passiveRegenIntervalTicks) { this.passiveRegenIntervalTicks = passiveRegenIntervalTicks; }
}
