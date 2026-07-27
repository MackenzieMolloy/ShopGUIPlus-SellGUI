package net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class HexColorUtility {

    private static final Map<Character, Pattern> KNOWN_PATTERN_MAP = new ConcurrentHashMap<>();

    /**
     * @param colorChar The character that will be used for color codes (usually {@code '&'})
     * @param string    The {@link String} that will have its values replaced.
     * @return A new {@link String} with the hex color codes being replaced.
     */
    public static @NotNull String replaceHexColors(char colorChar, @NotNull String string) {
        Pattern pattern = getReplaceAllRgbPattern(colorChar);
        Matcher matcher = pattern.matcher(string);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                matcher.appendReplacement(buffer, colorChar + "#$2");
                continue;
            }

            try {
                String hexCodeString = matcher.group(2);
                String hexCode = parseHexColor(hexCodeString);
                matcher.appendReplacement(buffer, hexCode);
            } catch (NumberFormatException ignored) {
                // Ignored Exception
            }
        }

        matcher.appendTail(buffer);

        // Normalize Adventure legacy serializers so that colors reset styles (bold/italic/etc.)
        Component component = LegacyComponentSerializer.legacySection().deserialize(buffer.toString());
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    private static Pattern getReplaceAllRgbPattern(char colorChar) {
        if (KNOWN_PATTERN_MAP.containsKey(colorChar)) {
            return KNOWN_PATTERN_MAP.get(colorChar);
        }

        String colorCharString = Character.toString(colorChar);
        String colorCharPattern = Pattern.quote(colorCharString);

        String patternString = ("(" + colorCharPattern + ")?" + colorCharPattern + "#([0-9a-fA-F]{6})");
        Pattern pattern = Pattern.compile(patternString);
        KNOWN_PATTERN_MAP.put(colorChar, pattern);
        return pattern;
    }

    private static @NotNull String parseHexColor(@NotNull String string) throws NumberFormatException {
        if (string.startsWith("#")) {
            string = string.substring(1);
        }

        if (string.length() != 6) {
            throw new NumberFormatException("Invalid hex length!");
        }

        int colorInt = Integer.decode("#" + string);
        if (colorInt < 0x000000 || colorInt > 0xFFFFFF) {
            throw new NumberFormatException("Invalid hex color value!");
        }

        StringBuilder assembled = new StringBuilder();
        assembled.append(ChatColor.COLOR_CHAR);
        assembled.append("x");

        char[] charArray = string.toCharArray();
        for (char character : charArray) {
            assembled.append(ChatColor.COLOR_CHAR);
            assembled.append(character);
        }

        return assembled.toString();
    }

    public static String purgeAllColor(String input) {
        // Remove Minecraft color codes (e.g., &7)
        String noMinecraftColorCodes = input.replaceAll("[&§][0-9a-fA-FkKlLnNoOrR]", "");

        // Remove hex color codes (e.g., &#FFFF0)
        return noMinecraftColorCodes.replaceAll("[&§]#([0-9a-fA-F]){6}", "");
    }
}
