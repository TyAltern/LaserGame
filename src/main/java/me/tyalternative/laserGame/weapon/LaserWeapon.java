package me.tyalternative.laserGame.weapon;

import me.tyalternative.laserGame.config.ConfigManager;
import me.tyalternative.laserGame.effect.EffectRegistry;
import me.tyalternative.laserGame.effect.ShotFiredContext;
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
    private boolean reloading = false;
    private boolean shotLocked = false;
    private long reloadProgressTicks = 0;
    private long ticksSinceLastRegen = 0;

    private BukkitTask tickTask;
    private BukkitTask shotLockTask;

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

    public void startManualReload(Player player) {
        if (reloading) return;
        EffectiveWeaponStats stats = statsSupplier.get();
        if (stats.isReloadDisabled()) {
            sendActionBar(player, "§7Cette arme ne peut pas être rechargée.");
            return;
        }
        if (ammo >= stats.getMaxAmmo()) {
            sendActionBar(player, "§7Munitions déjà pleines.");
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
                reloading = false;
                sendActionBar(player, "§cRechargement interrompu !");
                return;
            }
            reloadProgressTicks += config.getHudIntervalTicks();
            if (reloadProgressTicks >= stats.getReloadCooldownTicks()) {
                ammo = stats.getMaxAmmo();
                reloading = false;
                sendActionBar(player, "§aArme rechargée !");
            } else {
                sendActionBar(player, buildReloadBar(stats));
            }
        } else {
            sendActionBar(player, buildAmmoText(stats));
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

    private String buildReloadBar(EffectiveWeaponStats stats) {
        int totalBars = 20;
        int filled = (int) Math.round((double) reloadProgressTicks / stats.getReloadCooldownTicks() * totalBars);
        StringBuilder bar = new StringBuilder("§eRechargement ");
        for (int i = 0; i < totalBars; i++) {
            bar.append(i < filled ? "§a|" : "§8|");
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
        ammo = statsSupplier.get().getMaxAmmo();
        reloading = false;
        reloadProgressTicks = 0;
        return true;
    }

    public void addAmmo(int amount, int maxOverflow) {
        EffectiveWeaponStats stats = statsSupplier.get();
        int cap = stats.getMaxAmmo() + Math.max(0, maxOverflow);
        ammo = Math.min(cap, ammo + amount);
    }

    public WeaponType getType() { return type; }
    public int getAmmo() { return ammo; }
    public boolean isReloading() { return reloading; }
}
