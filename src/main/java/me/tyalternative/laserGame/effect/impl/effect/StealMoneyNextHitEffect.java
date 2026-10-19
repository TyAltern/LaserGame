package me.tyalternative.laserGame.effect.impl.effect;

import me.tyalternative.laserGame.economy.CurrencySource;
import me.tyalternative.laserGame.effect.HitResolutionContext;
import me.tyalternative.laserGame.effect.PendingEffect;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StealMoneyNextHitEffect implements PendingEffect {

    private final GamePlayer owner;
    private final long endTick;
    private final int amount;

    public StealMoneyNextHitEffect(GamePlayer owner, long endTick, int amount) {
        this.owner = owner;
        this.endTick = endTick;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return "steal_money_next_hit";
    }

    @Override
    public boolean onShotHit(HitResolutionContext ctx) {
        if (Bukkit.getCurrentTick() >= endTick) return true;

        int taken = ctx.target.takeCurrency(amount);
        if (taken <= 0) return false;

        owner.addCurrency(taken, CurrencySource.REFOUND);
        Player thief = owner.getPlayer();
        Player victim = ctx.target.getPlayer();
        if (thief != null) {
            Lang.send(thief, "effect.skill.pickpocket.stolen", "amount", taken, "player", victim != null ? victim.getName() : "?");
        }
        if (victim != null) {
            Lang.send(victim, "effect.skill.pickpocket.victim", "amount", taken, "player", thief != null ? thief.getName() : "?");
        }
        return true;
    }
}
