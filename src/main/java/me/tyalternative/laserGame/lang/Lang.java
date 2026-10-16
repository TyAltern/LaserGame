package me.tyalternative.laserGame.lang;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Systeme de traduction / de textes modifiables.
 *
 * <p>Les textes sont lus dans {@code plugins/LaserGame/lang/<langue>.yml}. Toute clé absente du fichier choisi
 * est complétée par le fichier par défaut du JAR ({@value #DEFAULT_LANGUAGE}), ce qui permet de ne
 * surcharger qu'une partie des messages et de ne rien casser lors d'une mise à jour du plugin.</p>
 *
 * <h3>Codes couleur</h3>
 * <p>{@code &a}, {@code &l}... et les couleurs hexadécimales {@code &#RRGGBB} sont supportés dans les textes
 * du fichier de langue. Les valeurs injectées par les placeholders ne sont jamais interprétées comme des codes.</p>
 *
 * <h3>Placeholders</h3>
 * <ul>
 *   <li>{@code {nom}} : remplacé par la valeur fournie.</li>
 *   <li>{@code {nom|défaut}} : valeur par défaut si le placeholder n'est pas fourni.</li>
 *   <li>{@code {n|singulier|pluriel}} : accord selon un nombre (singulier si n vaut 1).</li>
 *   <li>{@code {n|zéro|singulier|pluriel}} : idem avec une forme dédiée à 0.</li>
 *   <li>Les formes d'un accord peuvent elles-mêmes contenir des placeholders : {@code {n|aucun joueur|{n} joueur|{n} joueurs}}.</li>
 *   <li>{@code {prefix}} : texte de la clé {@code general.prefix}, disponible partout.</li>
 * </ul>
 *
 * <h3>Utilisation</h3>
 * <pre>
 * Lang.send(player, "round.first-blood", "reward", 10);
 * String s = Lang.get("match.countdown", "seconds", 5);
 * </pre>
 * <p>Un texte vide ({@code ""}) désactive le message : {@link #send} n'envoie alors rien.</p>
 */
public final class Lang {

    public static final String DEFAULT_LANGUAGE = "fr_FR";

    private static final Pattern HEX_COLOR = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final String COLOR_CHARS = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    private static Plugin plugin;
    private static String language = DEFAULT_LANGUAGE;
    private static Map<String, Object> messages = new HashMap<>();
    private static Map<String, Object> defaults = new HashMap<>();
    private static final Set<String> warnedMissing = new HashSet<>();

    private Lang() {}

    // ------------------------------------------------------------------ chargement

    public static void load(Plugin owner, String requistedLanguage) {
        plugin = owner;
        language = (requistedLanguage == null || requistedLanguage.isBlank()) ? DEFAULT_LANGUAGE : requistedLanguage.trim();
        warnedMissing.clear();

        defaults = readFromJar(DEFAULT_LANGUAGE);

        Map<String, Object> loaded = new HashMap<>();
        File file = new File(owner.getDataFolder(), "lang/" + language + ".yml");
        if (file.exists()) {
            loaded = flatten(YamlConfiguration.loadConfiguration(file));
        } else {
            owner.getLogger().warning("Fichier de langue introuvable : lang/" + language + ".yml - utilisation de "
                    + DEFAULT_LANGUAGE + ".");
        }
        messages = loaded;

        List<String> missing = new ArrayList<>();
        for (String key : defaults.keySet()) {
            if (!messages.containsKey(key)) missing.add(key);
        }
        if (!missing.isEmpty() && file.exists() && !language.equals(DEFAULT_LANGUAGE)) {
            owner.getLogger().info("lang/" + language + ".yml : " + missing.size()
                    + " clé(s) absente(s), valeur par défaut utilisée (ex : " + missing.getFirst() + ").");
        }
        owner.getLogger().info("Langue chargée : " + language + " (" + messages.size() + " texte(s)).");
    }

    public static String getLanguage() { return language; }

    private static Map<String, Object> readFromJar(String lang) {
        if (plugin == null) return new HashMap<>();
        try (InputStream in = plugin.getResource("lang/" + lang + ".yml")) {
            if (in == null) return new HashMap<>();
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            return flatten(yaml);
        } catch (Exception e) {
            plugin.getLogger().warning("Impossible de lire la langue par défaut du JAR : " + e.getMessage());
            return new HashMap<>();
        }
    }

    private static Map<String, Object> flatten(ConfigurationSection section) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String key : section.getKeys(true)) {
            if (section.isConfigurationSection(key)) continue;
            if (section.isList(key)) {
                List<String> lines = new ArrayList<>();
                for (Object o : section.getList(key, List.of())) lines.add(String.valueOf(o));
                out.put(key, lines);
            } else {
                Object value = section.get(key);
                if (value != null) out.put(key, String.valueOf(value));
            }
        }
        return out;
    }

    // Accès

    private static Object raw(String key) {
        Object value = messages.get(key);
        if (value == null) value = defaults.get(key);
        return value;
    }

    public static boolean has(String key) {
        return raw(key) != null;
    }

    public static String get(String key, Object... pairs) {
        return get(key, toMap(pairs));
    }

    public static String get(String key, Map<String, ?> placeholders) {
        Object value = raw(key);
        if (value == null) return missing(key);
        String template;
        if (value instanceof List<?> list) {
            StringBuilder joined = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) joined.append('\n');
                joined.append(String.valueOf(list.get(i)));
            }
            template = joined.toString();
        } else {
            template = String.valueOf(value);
        }
        return render(template, placeholders);
    }

    public static List<String> getList(String key, Object... pairs) {
        Object value = raw(key);
        if (value == null) return List.of(missing(key));
        Map<String, Object> map = toMap(pairs);
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object line : list) out.add(render(String.valueOf(line), map));
        } else {
            for (String line : String.valueOf(value).split("\n", -1)) out.add(render(line, map));
        }
        return out;
    }

    public static void send(CommandSender target, String key, Object... pairs) {
        if (target == null) return;
        String text = get(key, pairs);
        if (text.isEmpty()) return;
        for (String line : text.split("\n")) {
            target.sendMessage(line);
        }
    }

    public static void send(Collection<? extends CommandSender> targets, String key, Object... pairs) {
        String text = get(key, pairs);
        if (text.isEmpty()) return;
        for (CommandSender target : targets) {
            if (target == null) continue;
            for (String line : text.split("\n")) target.sendMessage(line);
        }
    }

    /**
     * Durée lisible et accordée ("1 minute", "5 secondes", "1 minute 30 secondes"),
     * à partir des clés {@code time.second}, {@code time.minute} et {@code time.separator}.
     */
    public static String duration(long totalSeconds) {
        if (totalSeconds < 60) return get("time.second", "n", totalSeconds);
        long minutes = totalSeconds / 60;
        long rest = totalSeconds % 60;
        String text = get("time.minute", "n", minutes);
        if (rest == 0) return text;
        return text + get("time.separator") + get("time.second", "n", rest);
    }

    private static String missing(String key) {
        if (plugin != null && warnedMissing.add(key)) {
            plugin.getLogger().warning("Texte manquant dans la langue '" + language + "' : " + key);
        }
        return "<" + key + ">";
    }

    // rendu

    static String render(String template, Map<String, ?> placeholders) {
        Map<String, Object> values = new HashMap<>();
        Object prefix = raw("general.prefix");
        if (prefix != null) values.put("prefix", color(String.valueOf(prefix)));
        if (placeholders != null) values.putAll(placeholders);

        // 1) couleurs du gabarit (avant remplacement : les valeurs ne sont jamais interprétées)
        String colored = color(template);
        // 2) placeholders
        return process(colored, values);
    }

    private static String process(String text, Map<String, ?> values) {
        StringBuilder out = new StringBuilder(text.length() + 16);
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c != '{') {
                out.append(c);
                i++;
                continue;
            }
            int end = findClosing(text, i);
            if (end < 0) {
                out.append(text, i, text.length());
                break;
            }
            out.append(resolve(text.substring(i + 1, end), values));
            i = end + 1;
        }
        return out.toString();
    }

    private static int findClosing(String text, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static List<String> splitTopLevel(String inner) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            else if (c == '|' && depth == 0) {
                parts.add(inner.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(inner.substring(start));
        return parts;
    }

    private static String resolve(String inner, Map<String, ?> values) {
        List<String> parts = splitTopLevel(inner);
        String name = parts.get(0).trim();
        Object value = values.get(name);

        if (value == null) {
            // {nom|défaut}
            if (parts.size() == 2) return process(parts.get(1), values);
            return "{" + inner + "}"; // inconnu : laissé tel quel (utile pour repérer une faute de frappe)
        }

        // {nom}
        if (parts.size() == 1) return String.valueOf(value);

        // {n|singulier|pluriel} ou {n|zéro|singulier|pluriel}
        if (parts.size() == 3 || parts.size() == 4) {
            double n = toNumber(value);
            String chosen;
            if (parts.size() == 3) {
                chosen = (n == 1.0) ? parts.get(1) : parts.get(2);
            } else {
                chosen = (n == 0.0) ? parts.get(1) : (n == 1.0 ? parts.get(2) : parts.get(3));
            }
            return process(chosen, values);
        }

        // {nom|défaut} avec valeur fournie -> la valeur
        if (parts.size() == 2) return String.valueOf(value);

        return "{" + inner + "}";
    }

    private static double toNumber(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static Map<String, Object> toMap(Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        if (pairs == null) return map;
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            map.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return map;
    }

    // ------------------------------------------------------------------ couleurs

    public static String color(String text) {
        if (text == null || text.isEmpty()) return "";

        Matcher matcher = HEX_COLOR.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            StringBuilder replacement = new StringBuilder("§x");
            for (char ch : matcher.group(1).toCharArray()) {
                replacement.append('§').append(ch);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(sb);

        char[] chars = sb.toString().toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && COLOR_CHARS.indexOf(chars[i + 1]) > -1) {
                chars[i] = '§';
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }
}
