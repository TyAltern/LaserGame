package me.tyalternative.laserGame.archetype.impl;

import me.tyalternative.laserGame.archetype.ArchetypeEffect;
import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

/** Flamboyant : dégâts et vies en plus, mais toujours en surbrillance. */
public class FlamboyantArchetypeEffect implements ArchetypeEffect {

    private final int bonusDamage;
    private final int lifeModifier;

    public FlamboyantArchetypeEffect(int bonusDamage, int lifeModifier) {
        this.bonusDamage = bonusDamage;
        this.lifeModifier = lifeModifier;
    }

    @Override
    public void onEquip(GamePlayer owner) {
        owner.setArchetypeLives(lifeModifier, null);
    }

    @Override
    public void onUnequip(GamePlayer owner) {
        Player player = owner.getPlayer();
        if (player != null) player.removePotionEffect(PotionEffectType.GLOWING);
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new PendingEffect() {
            @Override
            public String getId() {
                return "flamboyant";
            }

            private void glow() {
                Player player = owner.getPlayer();
                if (player != null) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 0, false, false));
                }
            }

            @Override
            public boolean onRoundStart() {
                glow();
                return false;
            }

            @Override
            public boolean onRespawn() {
                glow();
                return false;
            }

            @Override
            public boolean onShotHit(HitResolutionContext ctx) {
                ctx.livesToRemove += bonusDamage;
                return false;
            }
        });
    }
}
