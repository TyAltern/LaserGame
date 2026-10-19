package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.SkillEffect;
import me.tyalternative.laserGame.effect.impl.effect.StealMoneyNextHitEffect;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.Bukkit;

/** Pickpocket : ton prochain tir réussi, dans le temps imparti, vole de l'argent à la cible. */
public class PickpocketSkillEffect implements SkillEffect {

    private final long windowSeconds;
    private final int amount;

    public PickpocketSkillEffect(long windowSeconds, int amount) {
        this.windowSeconds = windowSeconds;
        this.amount = amount;
    }

    @Override
    public void activate(ActivationContext ctx) {
        ctx.gp.getEffects().add(new StealMoneyNextHitEffect(ctx.gp, Bukkit.getCurrentTick() + windowSeconds * 20L, amount));
        Lang.send(ctx.player, "effect.skill.pickpocket.activated", "duration", Lang.duration(windowSeconds), "amount", amount);
    }
}
