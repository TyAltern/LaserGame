package me.tyalternative.laserGame.utils;

import java.util.ArrayList;
import java.util.List;

public final class TextUtil {
    public static String parse(String hex) {
        return new String(Character.toChars(Integer.parseInt(hex,16)));
    }

    private static final String onePx = "Ii:;.,!|'";
    private static final String twoPx = "fl¤t()`";
    private static final String threePx = "FLTabcdeghjknopqrsuvxyz0123456789àéè-+_*/\\÷=\"?¿{}<>[]µ";
    private static final String fourPx = "ABCDEGHJKNOPQRSUVXYZ%¶~";
    private static final String fivePx = "MWmw#@&§^";

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
