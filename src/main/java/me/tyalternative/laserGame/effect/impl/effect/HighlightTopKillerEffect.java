package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.effect.ShotFiredContext;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HighlightTopKillerEffect implements PendingEffect {

    private static final int GLOW_TICKS = 60;

    private final GamePlayer owner;

    public HighlightTopKillerEffect(GamePlayer owner) {
        this.owner = owner;
    }

    @Override
    public String getId() {
        return "highlight_top_killer";
    }

    private void refresh() {
        Match match = owner.getMatch();
        if (match == null) return;

        GamePlayer top = null;
        for (GamePlayer gp : match.getPlayers()) {
            if (gp.isSpectator()) continue;
            if (top == null || gp.getKills() > top.getKills()) top = gp;
        }
        if (top == null || top.getKills() <= 0) return;

        Player p = top.getPlayer();
        if (p != null) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, GLOW_TICKS, 0, false, false)); // TODO : rendre brillant seulement pour le joueur utiliser GlowEntityLibrary ou autre (jsp)
        }
    }

    @Override
    public boolean onShotFired(ShotFiredContext ctx) {
        refresh();
        return false;
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        refresh();
        return false;
    }

    @Override
    public boolean onRespawn() {
        refresh();
        return false;
    }

    @Override
    public boolean onRoundStart() {
        refresh();
        return false;
    }
}
