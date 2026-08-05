package me.tyalternative.laserGame.UI.shop;

public final class Glyph {
    public static String parse(String hex) {
        return new String(Character.toChars(Integer.parseInt(hex,16)));
    }
}
