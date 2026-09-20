package me.tyalternative.laserGame.UI.hud;

import me.tyalternative.laserGame.LaserGame;
import me.tyalternative.laserGame.game.GamePlayer;
import me.tyalternative.laserGame.game.Match;
import me.tyalternative.laserGame.game.Round;
import me.tyalternative.laserGame.shop.ConsumableDefinition;
import me.tyalternative.laserGame.skill.SkillDefinition;
import me.tyalternative.laserGame.utils.Font;
import me.tyalternative.laserGame.utils.TextUtil;
import me.tyalternative.laserGame.weapon.WeaponType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;

public class ActionBarManager {
    private final Match match;

    private BukkitTask updateTask;

    public ActionBarManager(Match match) {
        this.match = match;
    }

    public void start() {
        if (!LaserGame.getInstance().getConfigManager().isActionBarEnable()) return;
        updateTask = Bukkit.getScheduler().runTaskTimer(
                match.getPlugin(),
                this::update,
                0L, LaserGame.getInstance().getConfigManager().getActionBarTicksRefreshRate()
        );
    }

    public void stop() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
            updateTask = null;
        }
    }


    public void update() {
        if (!LaserGame.getInstance().getConfigManager().isActionBarEnable()) return;

        for (GamePlayer gp : match.getPlayers()) {
            Player player = gp.getPlayer();
            if (player == null) continue;
            updatePlayerActionBar(gp);
        }

    }
    public void updatePlayerActionBar(GamePlayer gp) {
        Player player = gp.getPlayer();
        Component component = buildBackground().append(
                buildConsumablesDisplay(gp),
                buildMiddlePartDisplay(gp),
                buildWeaponDisplay(gp),
                buildCapacityDisplay(gp)
        );

        player.sendActionBar(component);

    }

    private Component buildBackground() {
        return TextUtil.buildTextComponent(28,0,"\uE000",Font.HUD).append(
                TextUtil.buildTextComponent(1,0,"\uE001",Font.HUD),
                TextUtil.buildTextComponent(1,0,"\uE002",Font.HUD)
        );
    }

    private Component buildConsumablesDisplay(GamePlayer gp) {
        Component component = TextUtil.buildOffset(570,19);
        int selectedSlot = gp.getSelectedSlot();
        for (int index = 0; index < 6; index++) {
            ConsumableDefinition consumable = null;
            Optional<String> idOpt = gp.getConsumables().get(index);
            if (idOpt.isPresent()) {
                Optional<ConsumableDefinition> defOpt = LaserGame.getInstance()
                        .getConsumableManager().getDefinition(idOpt.get());
                if (defOpt.isPresent()) consumable = defOpt.get();
            }

            component = component.append(
                    TextUtil.buildTextComponent(0, 5, TextUtil.parse("F02" + (consumable == null ? 2 : (selectedSlot == index ? 1 : 0))), Font.HUD),
                    TextUtil.buildTextComponent(20, 0, (consumable != null? TextUtil.parse(Font.findItemGlyph(consumable.material(), true)): "\uFFFF"), Font.ITEMS)
            );

        }
        component = component.append(TextUtil.buildOffset(0,45));
        return component;
    }

    private Component buildMiddlePartDisplay(GamePlayer gp) {
        return buildHealthPoint(gp).append(
                buildSneakBar(gp),
                buildAmmoDisplay(gp),
                buildLocatorBarDisplay(gp),
                buildPlayerRemainingDisplay(gp),
                buildMoneyDisplay(gp)
        );
    }

    private Component buildHealthPoint(GamePlayer gp) {
        int hp = Math.min(gp.getLives(), 10);

        Component component = Component.text("");
        for (int i = 0; i < 10; i++) {
            component = component.append(
                    TextUtil.buildTextComponent((hp > i? TextUtil.parse("F000") : TextUtil.parse("F001")), Font.HUD)
            );
        }
        return component.append(TextUtil.buildOffset(2,12));
    }

    private Component buildSneakBar(GamePlayer gp) {
        Component component = Component.text("");
        if (gp.getWeapon().isReloading()) {

            component = component.append(TextUtil.buildTextComponent(gp.getWeapon().buildReloadBar(gp.getEffectiveStats()),Font.HUD));

        } else {

            long sneakTick = gp.getSneakChargeTicks();
            long maxSneakTick = LaserGame.getInstance().getConfigManager().getMaxSneakChargeTicks();
            int sneakBars = Math.toIntExact(Math.ceilDiv(sneakTick * 13, Math.max(1, maxSneakTick)));
            for (int i = 1; i < 14; i++) {
                if (i > sneakBars) component = component.append(TextUtil.buildTextComponent("\uF010", Font.HUD));
                else if (i == 1) component = component.append(TextUtil.buildTextComponent("\uF011", Font.HUD));
                else if (i == 13) component = component.append(TextUtil.buildTextComponent("\uF013", Font.HUD));
                else component = component.append(TextUtil.buildTextComponent("\uF012", Font.HUD));
            }
        }

        return component.append(TextUtil.buildOffset(0,12));
    }

    private Component buildAmmoDisplay(GamePlayer gp) {
        int maxAmmo = gp.getWeapon().getMaxAmmo();
        int decimal = (int) Math.floor(Math.log10(maxAmmo)) + 1;
        String text = "Muni: " + String.format("%0" + decimal + "d",gp.getWeapon().getAmmo())  + " / " + maxAmmo;
//        int length = TextUtil.getStringLength(text);
        int offset = (3-decimal) * 4;
        if (offset == 0) return TextUtil.buildTextComponent(1, 0,text, Font.HUD_TEXT_HIGH).append(TextUtil.buildOffset(145, 0));
        return TextUtil.buildTextComponent(0, offset, text, Font.HUD_TEXT_HIGH).append(TextUtil.buildOffset(144, offset));

    }

    private Component buildLocatorBarDisplay(GamePlayer gp) {
        Component component = Component.text("");
        if (gp.doesSeeAmmoNotUsedEnoughMessage()) {
            int requiredAmmo = gp.getWeapon().getAmmo() - (int) Math.ceil((1- gp.getEffectiveStats().getMinAmmoUsedFractionToReload()) * gp.getWeapon().getMaxAmmo());
            if (requiredAmmo > 0) {
                String text = "Utilisez " + requiredAmmo + " Muni avant de recharger";
                int length = TextUtil.getStringLength(text);
                int offset = (132 - length) / 2 - 1;
                return component.append(
                        TextUtil.buildTextComponent("\uF033", Font.HUD),
                        TextUtil.buildTextComponent(133, offset, text, Font.HUD_TEXT_RELOAD),
                        TextUtil.buildOffset(120, offset)
                );
            }
        }
         if (gp.getWeapon().isReloading()) component = component.append(TextUtil.buildTextComponent("\uF032", Font.HUD));
        else if (!gp.canSeeRadar()) component = component.append(TextUtil.buildTextComponent("\uF031", Font.HUD));
        else component = component.append(TextUtil.buildTextComponent("\uF030", Font.HUD));

        return component.append(TextUtil.buildOffset(120,0));
    }

    private Component buildPlayerRemainingDisplay(GamePlayer gp) {
        int maxPlayers = match.getPlayers().size();
        int alivePlayers = match.getAlivePlayers().size();
        int decimal = (int) Math.floor(Math.log10(maxPlayers)) + 1;
        String text = String.format("%0" + decimal + "d",alivePlayers)  + "/" + maxPlayers;
        int length = TextUtil.getStringLength(text) + 10;
        int offset = (47 - length) / 2;
        Component component = TextUtil.buildTextComponent(0, offset, "\uF040", Font.HUD).append(
                TextUtil.buildTextComponent("¤" + text, Font.HUD_TEXT_LOW)
        );
        return component.append(TextUtil.buildOffset(0, offset + 15));

    }

    private Component buildMoneyDisplay(GamePlayer gp) {
        String text = gp.getCurrency() + "$";
        int length = TextUtil.getStringLength(text);
        int offset = (47 - length) / 2;
        Component component = TextUtil.buildTextComponent(0, offset, text, Font.HUD_TEXT_LOW);
        return component.append(TextUtil.buildOffset(0, offset + 69));

    }

    private Component buildWeaponDisplay(GamePlayer gp) {

        long cooldownTime = gp.getEffectiveStats().getShotCooldownTicks();
        String cooldownSuffix = " t";
        if (cooldownTime >= 20) {
            cooldownTime = Math.floorDiv(cooldownTime, 20);
            cooldownSuffix = "s";
        }
        String cooldownText = cooldownTime + cooldownSuffix;
        int cooldownOffset = cooldownTime < 10 ? 3 : 0;

        long reloadTime = gp.getEffectiveStats().getReloadCooldownTicks();
        String reloadSuffix = " t";
        if (reloadTime >= 20) {
            reloadTime = Math.floorDiv(reloadTime, 20);
            reloadSuffix = "s";
        }
        String reloadText = reloadTime + reloadSuffix;
        int reloadOffset = reloadTime < 10 ? 2 : 0;

        WeaponType weapon = gp.getWeaponType();

        return TextUtil.buildTextComponent(0 , cooldownOffset, cooldownText, Font.HUD_TEXT_CARD).append(
                TextUtil.buildTextComponent(0,Math.max(cooldownOffset-1, 0) + 10, TextUtil.parse(Font.findItemGlyph(weapon.material(), true)), Font.ITEMS),
                TextUtil.buildTextComponent(0 , 10 + reloadOffset,reloadText, Font.HUD_TEXT_CARD),
                TextUtil.buildOffset(0,Math.max(reloadOffset+1, 0) + 26)
        );
    }

    private Component buildCapacityDisplay(GamePlayer gp) {
        String skillId = gp.getEquippedSkillId();
        SkillDefinition skill = null;
        if (skillId == null || skillId.isBlank()) skill = null;
        else {
            Optional<SkillDefinition> defOpt =LaserGame.getInstance().getSkillManager().getDefinition(skillId);
            if (defOpt.isPresent()) skill = defOpt.get();
        }

        long durationRemainingCooldown = gp.getSkillCooldownRemainingTicks();
        int seconds = (int) durationRemainingCooldown / 20 + 1;
        String displaySeconds = String.format("%02d",seconds % 60);
        String displayMinutes = String.valueOf(Math.min(99,Math.floorDiv(seconds, 60)));
        boolean ready = durationRemainingCooldown == 0;
        String timer = displayMinutes + ":" + displaySeconds;
        int offset = displayMinutes.length() == 1 ? 0 : 2;

        Component component = TextUtil.buildTextComponent(0, 0, TextUtil.parse("F02" + (skill == null ? 2: (ready ? 1 : 0))), Font.HUD).append(
                TextUtil.buildTextComponent(20, 0, (skill != null? TextUtil.parse(Font.findItemGlyph(skill.material(), true)): "\uFFFF"), Font.ITEMS),
                TextUtil.buildTextComponent(0,15 + (skill == null? 1 : (ready ? 0 : offset+1)), (skill == null? "Vide" : (ready? "Pret" : timer)), Font.HUD_TEXT_ABILITY_COOLDOWN),
                TextUtil.buildOffset(0, skill == null? 0 : (ready ? 0 : offset-1))
        );
        return component;
    }
}
