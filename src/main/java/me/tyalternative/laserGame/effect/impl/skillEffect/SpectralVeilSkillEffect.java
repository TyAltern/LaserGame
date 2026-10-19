package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.lang.Lang;

/**
 * Voile Spectral : invisible pendant quelques secondes, sans pouvoir tirer.
 * Recevoir un coup ou s'accroupir met fin à l'invisibilité. Le tir reste bloqué un court instant ensuite.
 */
public class SpectralVeilSkillEffect implements SkillEffect {

    private final long invisibleSeconds;
    private final long lockAfterSeconds;

    public SpectralVeilSkillEffect(long invisibleSeconds, long lockAfterSeconds) {
        this.invisibleSeconds = invisibleSeconds;
        this.lockAfterSeconds = lockAfterSeconds;
    }

    @Override
    public void activate(ActivationContext ctx) {
        ctx.gp.startStealth(invisibleSeconds * 20L, lockAfterSeconds * 20L);
        Lang.send(ctx.player, "effect.skill.spectral_veil.activated",
                "duration", Lang.duration(invisibleSeconds), "lock", Lang.duration(lockAfterSeconds));
    }
}
