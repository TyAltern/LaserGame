package me.tyalternative.laserGame.weapon;

import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.effect.EffectRegistry;
import me.tyalternative.laserGame.effect.ShotFiredContext;
import me.tyalternative.laserGame.game.GamePlayer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.function.Supplier;

public class LaserWeapon {

    private final Plugin plugin;
    private final ConfigManager config;
    private final WeaponType type;
    private final UUID ownerUuid;
    private final Supplier<EffectiveWeaponStats> statsSupplier;
    private final EffectRegistry effects;

    private int ammo;
    private int pendingReloadBonusAmount = 0;
    private boolean reloading = false;
    private boolean shotLocked = false;
    private long reloadProgressTicks = 0;
    private long ticksSinceLastRegen = 0;

    private BukkitTask tickTask;
    private BukkitTask shotLockTask;
    private BukkitTask shotNotPossibleAmmoUnusedTask;

    public LaserWeapon(Plugin plugin, ConfigManager config, WeaponType type, UUID ownerUuid,
                       Supplier<EffectiveWeaponStats> statsSupplier, EffectRegistry effects) {
        this.plugin = plugin;
        this.config = config;
        this.type = type;
        this.ownerUuid = ownerUuid;
        this.statsSupplier = statsSupplier;
        this.effects = effects;
        this.ammo = statsSupplier.get().getMaxAmmo();
    }

    public boolean tryShoot() {
        if (reloading || shotLocked || ammo <= 0) {
            return false;
        }

        EffectiveWeaponStats stats = statsSupplier.get();

        Player shooterPlayer = Bukkit.getPlayer(ownerUuid);
        ShotFiredContext ctx = new ShotFiredContext(shooterPlayer);
        effects.fireShotFired(ctx);

        if (ctx.consumesAmmo) {
            ammo--;
        }
        shotLocked = true;
        applyCooldown(stats.getShotCooldownTicks());

        if (shotLockTask != null) {
            shotLockTask.cancel();
        }
        shotLockTask = Bukkit.getScheduler().runTaskLater(plugin,
                () -> shotLocked = false, stats.getShotCooldownTicks());

        return true;
    }

    public void startManualReload(GamePlayer gp) {
        if (reloading) return;
        EffectiveWeaponStats stats = statsSupplier.get();
        if (stats.isReloadDisabled()) {
//            sendActionBar(gp, "§7Cette arme ne peut pas être rechargée.");
            return;
        }
        if (ammo >= stats.getMaxAmmo()) {
//            sendActionBar(gp, "§7Munitions déjà pleines.");
            return;
        }
        double usedFraction = stats.getMaxAmmo() <= 0 ? 1.0 : 1.0 - ((double) ammo / stats.getMaxAmmo());
        if (usedFraction < stats.getMinAmmoUsedFractionToReload() - 1e-9) {
            int requiredPercent = (int) Math.round(stats.getMinAmmoUsedFractionToReload() * 100);
            gp.setDoesSeeAmmoNotUsedEnoughMessage(true);
            if (shotNotPossibleAmmoUnusedTask != null) {
                shotNotPossibleAmmoUnusedTask.cancel();
                shotNotPossibleAmmoUnusedTask = null;
            }
            shotNotPossibleAmmoUnusedTask = Bukkit.getScheduler().runTaskLater(plugin, () -> gp.setDoesSeeAmmoNotUsedEnoughMessage(false), 20L);
//            sendActionBar(player, "§cIl faut avoir utilisé au moins " + requiredPercent + "% du chargeur pour recharger.");
            return;
        }
        reloading = true;
        reloadProgressTicks = 0;
    }

    private void tick() {
        Player player = Bukkit.getPlayer(ownerUuid);
        if (player == null) return;

        EffectiveWeaponStats stats = statsSupplier.get();

        if (!reloading) {
            applyPassiveRegen(stats);
        }

        if (reloading) {
            if (!player.isBlocking()) {
                if (stats.isReloadPausable()) {
//                    sendActionBar(player, "§e" + buildReloadBar(stats) + " §7(en pause)");
                    return; // conserve reloading=true et la progression : reprend dès que le joueur re-bloque
                }
                reloading = false;
//                sendActionBar(player, "§cRechargement interrompu !");
                return;
            }
            reloadProgressTicks += config.getHudIntervalTicks();
            if (reloadProgressTicks >= stats.getReloadCooldownTicks()) {
                ammo = stats.getMaxAmmo() + pendingReloadBonusAmount;
                pendingReloadBonusAmount = 0;

                reloading = false;
                effects.fireReloadCompleted();
//                sendActionBar(player, "§aArme rechargée !");
            } else {
//                sendActionBar(player, buildReloadBar(stats));
            }
        } else {
//            sendActionBar(player, buildAmmoText(stats));
        }
    }

    private void applyPassiveRegen(EffectiveWeaponStats stats) {
        if (stats.getPassiveRegenIntervalTicks() <= 0 || ammo >= stats.getMaxAmmo()) {
            ticksSinceLastRegen = 0;
            return;
        }
        ticksSinceLastRegen += config.getHudIntervalTicks();
        if (ticksSinceLastRegen >= stats.getPassiveRegenIntervalTicks()) {
            ammo = Math.min(stats.getMaxAmmo(), ammo + stats.getPassiveRegenAmount());
            ticksSinceLastRegen = 0;
        }
    }

    private String buildAmmoText(EffectiveWeaponStats stats) {
        String hint = ammo <= 0 ? " §7(clic droit maintenu pour recharger)" : "";
        return "§f" + type.displayName() + " §f- Munitions : §b" + ammo + "§f/§b" + stats.getMaxAmmo() + hint;
    }

    public String buildReloadBar(EffectiveWeaponStats stats) {
        int totalBars = 13;
        int filled = (int) Math.round((double) reloadProgressTicks / stats.getReloadCooldownTicks() * totalBars);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < totalBars; i++) {
            bar.append(i < filled ? (i == 0 ? "\uF011" : i == totalBars - 1 ? "\uF013" : "\uF012") : "\uF010");
        }
        return bar.toString();
    }

    private void sendActionBar(Player player, String legacyText) {
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(legacyText));
    }

    private void applyCooldown(long ticks) {
        Player player = Bukkit.getPlayer(ownerUuid);
        if (player != null) {
            player.setCooldown(type.material(), (int) ticks);
        }
    }

    public void start() {
        stop();
        if (config.isRefillAmmoOnRespawn()) {
            ammo = statsSupplier.get().getMaxAmmo();
        }
        reloading = false;
        reloadProgressTicks = 0;
        ticksSinceLastRegen = 0;
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick,
                config.getHudIntervalTicks(), config.getHudIntervalTicks());
    }

    public void stop() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        if (shotLockTask != null) {
            shotLockTask.cancel();
            shotLockTask = null;
        }
        reloading = false;
        shotLocked = false;
    }

    public boolean instantRefill() {
        EffectiveWeaponStats stats = statsSupplier.get();
        if (stats.isReloadDisabled()) {
            return false;
        }
        ammo = statsSupplier.get().getMaxAmmo() + pendingReloadBonusAmount;
        pendingReloadBonusAmount = 0;
        reloading = false;
        reloadProgressTicks = 0;
        effects.fireReloadCompleted();
        return true;
    }

    public void grantNextReloadBonus(int amount) {
        pendingReloadBonusAmount += amount;
    }

    public void addAmmo(int amount, int maxOverflow) {
        EffectiveWeaponStats stats = statsSupplier.get();
        int cap = stats.getMaxAmmo() + Math.max(0, maxOverflow);
        ammo = Math.min(cap, ammo + amount);
    }

    public void setAmmo(int amount) {
        this.ammo = Math.max(0, Math.min(amount, statsSupplier.get().getMaxAmmo()));
    }

    public WeaponType getType() { return type; }
    public int getAmmo() { return ammo; }
    public int getMaxAmmo() { return statsSupplier.get().getMaxAmmo(); }
    public boolean isReloading() { return reloading; }

    public long getReloadProgressTicks() {
        return reloadProgressTicks;
    }
}
