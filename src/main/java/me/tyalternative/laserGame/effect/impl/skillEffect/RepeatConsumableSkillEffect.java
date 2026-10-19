package me.tyalternative.laserGame.effect.impl.skillEffect;

import me.tyalternative.laserGame.LaserGame;
import me.tyalternative.laserGame.effect.ActivationContext;
import me.tyalternative.laserGame.effect.ConditionalSkillEffect;
import me.tyalternative.laserGame.lang.Lang;
import me.tyalternative.laserGame.shop.ConsumableDefinition;

import java.util.HashSet;
import java.util.Set;

/**
 * Bis Repetita : réactive le dernier consommable utilisé. Une même utilisation ne peut être répétée qu'une fois ;
 * les consommables listés dans {@code excluded} (ids séparés par des virgules) ne sont pas répétables.
 */
public class RepeatConsumableSkillEffect implements ConditionalSkillEffect {

    private final Set<String> excluded = new HashSet<>();

    public RepeatConsumableSkillEffect(String excludedCsv) {
        for (String id : excludedCsv.split(",")) {
            if (!id.isBlank()) excluded.add(id.trim());
        }
    }

    @Override
    public boolean canActivate(ActivationContext ctx) {
        if (!LaserGame.getInstance().getConsumableManager().canRepeatLastConsumable(ctx.gp, excluded)) {
            Lang.send(ctx.player, "effect.skill.repeat_consumable.unavailable");
            return false;
        }
        return true;
    }

    @Override
    public void activate(ActivationContext ctx) {
        ConsumableDefinition def = ctx.gp.getLastConsumableDefinition();
        if (LaserGame.getInstance().getConsumableManager().repeatLastConsumable(ctx.gp, ctx.player, ctx.match, excluded) && def != null) {
            Lang.send(ctx.player, "effect.skill.repeat_consumable.activated", "name", Lang.color(def.displayName()));
        }
    }
}
