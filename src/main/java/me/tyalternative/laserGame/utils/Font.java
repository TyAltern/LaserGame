package me.tyalternative.laserGame.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.N;

public final class Font {

    public static final NamespacedKey TEXT = new NamespacedKey("menu", "text");
    public static final NamespacedKey SHOP = new NamespacedKey("menu", "shop_screen");
    public static final NamespacedKey INVENTORY = new NamespacedKey("menu", "inventory_screen");
    public static final NamespacedKey STATS = new NamespacedKey("menu", "stats_screen");
    public static final NamespacedKey MENU = new NamespacedKey("menu", "menu");
    public static final NamespacedKey ITEMS = new NamespacedKey("minecraft", "items");

    public static final NamespacedKey HUD = new NamespacedKey("menu","hud");
    public static final NamespacedKey HUD_ITEM = new NamespacedKey("menu","hud_item");
    public static final NamespacedKey HUD_TEXT_ABILITY_COOLDOWN = new NamespacedKey("menu","hud_text_ability_cooldown");
    public static final NamespacedKey HUD_TEXT_CARD = new NamespacedKey("menu","hud_text_card");
    public static final NamespacedKey HUD_TEXT_HIGH = new NamespacedKey("menu","hud_text_high");
    public static final NamespacedKey HUD_TEXT_LOW = new NamespacedKey("menu","hud_text_low");

    public static final NamespacedKey POSITIVE = new NamespacedKey("offset","positive");
    public static final NamespacedKey NEGATIVE = new NamespacedKey("offset","negative");



    public static String findItemGlyph(Material material, boolean forHUD) {
        String unicode = switch (material) {
            case GOLDEN_HOE -> "A000";
            case COPPER_HOE -> "A001";
            case DIAMOND_HOE -> "A002";
            case IRON_HOE -> "A003";
            case STONE_HOE -> "A004";
            case WOODEN_HOE -> "A005";
            case IRON_SWORD -> "A006";
            case TNT -> "A007";
            case SPYGLASS -> "A008";
            case NETHERITE_INGOT -> "A009";
            case BOW -> "A00A";
            case BLAZE_ROD -> "A00B";
            case NETHERITE_SWORD -> "A00C";
            case NETHERITE_HOE -> "A00D";
            case CROSSBOW -> "A00E";
            case TRIDENT -> "A00F";
            case BLAZE_POWDER -> "A010";
            case IRON_CHESTPLATE -> "A011";
            case CLOCK -> "A012";
            case GLOWSTONE_DUST -> "A013";
            case SUGAR -> "A014";
            case POTION -> "A015";
            case COPPER_CHESTPLATE -> "A016";
            case TURTLE_HELMET -> "A017";
            case FIRE_CHARGE -> "A018";
            case GOLDEN_AXE -> "A019";
            case ARROW -> "A01A";
            case ROTTEN_FLESH -> "A01B";
            case SOUL_SAND -> "A01C";
            case FLINT -> "A01D";
            case BUNDLE -> "A01E";
            case SPECTRAL_ARROW -> "A01F";
            case ENDER_EYE -> "A020";
            case REDSTONE -> "A021";
            case HOPPER -> "A022";
            case GLOW_INK_SAC -> "A023";
            case RABBIT_FOOT -> "A024";
            case SLIME_BALL -> "A025";
            case BLUE_ICE -> "A026";
            case PUFFERFISH -> "A027";
            case COBWEB -> "A028";
            case GRAY_DYE -> "A029";
            case PHANTOM_MEMBRANE -> "A02A";
            case TOTEM_OF_UNDYING -> "A02B";
            case GOLD_INGOT -> "A02C";
            case EMERALD -> "A02D";
            case NETHER_STAR -> "A02E";
            case DIAMOND_AXE -> "A02F";
            case STRING -> "A030";
            case SPIDER_EYE -> "A031";
            case FISHING_ROD -> "A032";
            case FEATHER -> "A033";
            case CHEST_MINECART -> "A034";
            case SHULKER_SHELL -> "A035";
            case HONEY_BOTTLE -> "A036";
            case NETHERITE_CHESTPLATE -> "A037";
            case IRON_BOOTS -> "A038";
            default -> "FFFF";
        };
        if (forHUD) unicode = unicode.replaceFirst("A","B");
        return unicode;
    }
}
