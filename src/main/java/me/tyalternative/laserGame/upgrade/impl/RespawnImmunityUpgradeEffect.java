package me.tyalternative.laserGame.upgrade.impl;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.impl.effect.RespawnGraceEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.lang.Lang;
import me.tyalternative.laserGame.upgrade.PermanentUpgradeEffect;
import org.bukkit.entity.Player;

import java.util.Optional;

/** Second Souffle : immunité pendant {@code seconds} secondes après chaque respawn (perte d'une vie). */
public class RespawnImmunityUpgradeEffect implements PermanentUpgradeEffect {

    private final long seconds;

    public RespawnImmunityUpgradeEffect(long seconds) {
        this.seconds = seconds;
    }

    @Override
    public Optional<PendingEffect> getPersistentEffect(GamePlayer owner) {
        RespawnGraceEffect grace = new RespawnGraceEffect(seconds * 20L);
        return Optional.of(new PendingEffect() {
            @Override
            public String getId() {
                return "upgrade_respawn_immunity";
            }

            @Override
            public boolean onRespawn() {
                grace.onRespawn();
                Player player = owner.getPlayer();
                if (player != null) {
                    Lang.send(player, "effect.upgrade.respawn_immunity.activated", "duration", Lang.duration(seconds));
                }
                return false;
            }

            @Override
            public boolean onDamageTaken(HitResolutionContext ctx) {
                return grace.onDamageTaken(ctx);
            }
        });
    }
}
