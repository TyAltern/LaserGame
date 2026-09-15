package me.tyalternative.laserGame.effect;

import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ActivationContext {

    public final GamePlayer gp;
    public final Player player;
    public final Match match;
    public final Plugin plugin;
    public final int price;

    public ActivationContext(GamePlayer gp, Player player, Match match, Plugin plugin, int price) {
        this.gp = gp;
        this.player = player;
        this.match = match;
        this.plugin = plugin;
        this.price = price;
    }
}
