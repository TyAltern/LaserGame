package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.IgnoreDamageWhileReloadingEffect;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

import java.util.Optional;

public class ReloadShieldUpgradeEffect implements PermanentUpgradeEffect {
    @Override
    public Optional<PendingEffect> getPersistentEffect() {
        return Optional.of(new IgnoreDamageWhileReloadingEffect());
    }
}
