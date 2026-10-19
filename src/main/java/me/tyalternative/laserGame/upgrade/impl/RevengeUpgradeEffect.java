package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Rancunier / Vindicatif : dégâts bonus contre les joueurs qui t'ont touché.
 * <ul>
 *   <li>mode "last" : uniquement le dernier joueur qui t'a touché ;</li>
 *   <li>mode "all" : tous ceux qui t'ont touché ce round.</li>
 * </ul>
 * {@code overridden-by} : id d'une amélioration qui rend celle-ci inutile (Rancunier est annulé par Vindicatif).
 */
public class RevengeUpgradeEffect implements PermanentUpgradeEffect {

    private final boolean allAttackers;
    private final int bonus;
    private final String overriddenBy;

    public RevengeUpgradeEffect(boolean allAttackers, int bonus, String overriddenBy) {
        this.allAttackers = allAttackers;
        this.bonus = bonus;
        this.overriddenBy = overriddenBy == null ? "" : overriddenBy;
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        return Optional.of(new Tracker(owner));
    }

    private final class Tracker implements PendingEffect {
        private final GamePlayer owner;
        private UUID lastAttacker;
        private final Set<UUID> attackers = new HashSet<>();

        private Tracker(GamePlayer owner) {
            this.owner = owner;
        }

        @Override
        public String getId() {
            return allAttackers ? "revenge_all" : "revenge_last";
        }

        @Override
        public boolean onDamageTaken(HitResolutionContext ctx) {
            if (ctx.shooter != null) {
                lastAttacker = ctx.shooter.getUuid();
                attackers.add(ctx.shooter.getUuid());
            }
            return false;
        }

        @Override
        public boolean onShotHit(HitResolutionContext ctx) {
            if (!overriddenBy.isBlank() && owner.ownsUpgrade(overriddenBy)) return false;

            UUID targetId = ctx.target.getUuid();
            boolean revenge = allAttackers ? attackers.contains(targetId) : targetId.equals(lastAttacker);
            if (revenge) ctx.livesToRemove += bonus;
            return false;
        }

        @Override
        public boolean onRoundStart() {
            lastAttacker = null;
            attackers.clear();
            return false;
        }
    }
}
