package me.tyalternative.laserGame.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public final class TextUtil {
    public static String parse(String hex) {
        return new String(Character.toChars(Integer.parseInt(hex,16)));
    }

    private static final String onePx = "Ii:;.,!|¤'";
    private static final String twoPx = "fl t()`";
    private static final String threePx = "FLTabcdeghjknopqrsuvxyz0123456789àéè-+_*/\\÷=\"?¿{}<>[]µ";
    private static final String fourPx = "ABCDEGHJKNOPQRSUVXYZ%¶~";
    private static final String fivePx = "MWmw#@&§^$";

    public static Component buildTextComponent(String text, NamespacedKey font) {
        return Component.text(text).font(font);
    }

    public static Component buildOffset(int negative_offset, int positive_offset) {
        negative_offset = Math.max(0, negative_offset);
        positive_offset = Math.max(0, positive_offset);
        return Component.text(parse("E" + String.format("%03d",negative_offset))).font(Font.NEGATIVE).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0)).append(
                Component.text(parse("E" + String.format("%03d",positive_offset))).font(Font.POSITIVE).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0))
        );
    }

    public static Component buildTextComponent(int negative_offset, int positive_offset, String text, NamespacedKey font) {
        negative_offset = Math.max(0, negative_offset);
        positive_offset = Math.max(0, positive_offset);
        if (negative_offset == 0 && positive_offset == 0) return Component.text(text).font(font).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0));
        if (negative_offset == 0) return Component.text(parse("E" + String.format("%03d",positive_offset))).font(Font.POSITIVE).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0))
                .append(Component.text(text).font(font).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0)));
        if (positive_offset == 0) return Component.text(parse("E" + String.format("%03d",negative_offset))).font(Font.NEGATIVE).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0))
                .append(Component.text(text).font(font).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0)));

        return Component.text(parse("E" + String.format("%03d",negative_offset))).font(Font.NEGATIVE).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0)).append(
                Component.text(parse("E" + String.format("%03d",positive_offset))).font(Font.POSITIVE).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0)),
                Component.text(text).font(font).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0))
        );
    }

    public static int getStringLength(String string) {
        int counter = -1;
        for (int i = 0; i < string.length(); i++) {
            if( onePx.contains(string.substring(i,i+1))) counter += 2;
            else if( twoPx.contains(string.substring(i,i+1))) counter += 3;
            else if( threePx.contains(string.substring(i,i+1))) counter += 4;
            else if( fourPx.contains(string.substring(i,i+1))) counter += 5;
            else if( fivePx.contains(string.substring(i,i+1))) counter += 6;
        }
        return counter;
    }

    public static List<String> wrapText(String text, int maxWidthPx) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String candidate = currentLine.length() == 0
                    ? word
                    : currentLine + " " + word;

            if (getStringLength(candidate) <= maxWidthPx) {
                // Le mot rentre, on l'ajoute à la ligne courante
                currentLine = new StringBuilder(candidate);
            } else {
                // Le mot ne rentre pas : on clôt la ligne courante
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }

                // Cas où le mot seul dépasse déjà la largeur max
                if (getStringLength(word) > maxWidthPx) {
                    lines.addAll(splitLongWord(word, maxWidthPx));
                    currentLine = new StringBuilder();
                } else {
                    currentLine = new StringBuilder(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    // Découpe un mot trop long caractère par caractère (cas limite)
    private static List<String> splitLongWord(String word, int maxWidthPx) {
        List<String> parts = new ArrayList<>();
        StringBuilder part = new StringBuilder();

        for (char c : word.toCharArray()) {
            String candidate = part.toString() + c;
            if (getStringLength(candidate) > maxWidthPx && part.length() > 0) {
                parts.add(part.toString());
                part = new StringBuilder();
            }
            part.append(c);
        }

        if (part.length() > 0) {
            parts.add(part.toString());
        }

        return parts;
    }
}
