package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.ShotFiredContext;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Embuscade : le premier tir tiré après {@code seconds} secondes accroupi inflige des dégâts supplémentaires.
 * Après un tir bonus, il faut de nouveau rester accroupi ce temps-là (décompté à partir du tir).
 */
public class AmbushUpgradeEffect implements PermanentUpgradeEffect {

    private final long requiredTicks;
    private final int bonus;

    public AmbushUpgradeEffect(long seconds, int bonus) {
        this.requiredTicks = seconds * 20L;
        this.bonus = bonus;
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new PendingEffect() {
            private boolean pending = false;
            private long lastBonusTick = -1;

            @Override
            public String getId() {
                return "ambush";
            }

            @Override
            public boolean onShotFired(ShotFiredContext ctx) {
                pending = false;
                Player player = owner.getPlayer();
                long sneakStart = owner.getSneakStartTick();
                if (player == null || !player.isSneaking() || sneakStart < 0) return false;

                long now = Bukkit.getCurrentTick();
                long since = Math.max(sneakStart, lastBonusTick);
                if (now - since >= requiredTicks) {
                    pending = true;
                    lastBonusTick = now;
                }
                return false;
            }

            @Override
            public boolean onShotHit(HitResolutionContext ctx) {
                if (pending) {
                    pending = false;
                    ctx.livesToRemove += bonus;
                }
                return false;
            }

            @Override
            public boolean onShotMissed() {
                pending = false;
                return false;
            }

            @Override
            public boolean onRoundStart() {
                pending = false;
                lastBonusTick = -1;
                return false;
            }
        });
    }
}
