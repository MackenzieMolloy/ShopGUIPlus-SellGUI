package net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class MessageUtility {

    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * @param message The message that will be colored
     * @return A new string containing {@code message} but with the color codes replaced,
     * or an empty string if {@code message} was {@code null}.
     */
    public static @NotNull String color(@NotNull String message) {
        try {
            Class.forName("net.md_5.bungee.api.ChatColor");
            return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
        } catch (ReflectiveOperationException ex) {
            return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
        }
    }

    public static boolean usesMiniMessage(@NotNull String format) {
        return "minimessage".equalsIgnoreCase(format.trim());
    }

    public static @NotNull Component toComponent(@NotNull String message, boolean useMiniMessage) {
        String processed = HexColorUtility.replaceHexColors('&', message);
        if (useMiniMessage) {
            return MINI_MESSAGE.deserialize(processed);
        }

        return LEGACY_SECTION.deserialize(color(processed));
    }

    public static @NotNull String toPlainText(@NotNull Component component) {
        return LEGACY_SECTION.serialize(component);
    }

    /**
     * @param messageArray The array of messages that will be colored
     * @return A new array containing every message in the input array, but with color codes replaced.
     */
    public static String @NotNull [] colorArray(String @NotNull ... messageArray) {
        int messageArrayLength = messageArray.length;
        String[] colorMessageArray = new String[messageArrayLength];

        for (int i = 0; i < messageArrayLength; i++) {
            String message = messageArray[i];
            colorMessageArray[i] = color(message);
        }

        return colorMessageArray;
    }

    /**
     * @param messageList The iterable of messages that will be colored
     * @return A {@code List<String>} containing every message in the input iterable, but with color codes replaced.
     */
    public static @NotNull List<String> colorList(@NotNull Iterable<String> messageList) {
        List<String> colorList = new ArrayList<>();
        for (String message : messageList) {
            String color = color(message);
            colorList.add(color);
        }

        return colorList;
    }

    /**
     * @param messageArray The array of messages that will be colored
     * @return A {@code List<String>} containing every message in the input array, but with color codes replaced.
     */
    public static @NotNull List<String> colorList(String @NotNull ... messageArray) {
        List<String> messageList = Arrays.asList(messageArray);
        return colorList(messageList);
    }

    /**
     * Copies all elements from the iterable collection of originals to a
     * new {@link List}
     */
    public static @NotNull List<String> getMatches(@NotNull String token, @NotNull Iterable<String> originals) {
        List<String> collection = new ArrayList<>();
        for (String string : originals) {
            if (startsWithIgnoreCase(string, token)) {
                collection.add(string);
            }
        }

        return collection;
    }

    public static boolean startsWithIgnoreCase(@NotNull String string, @NotNull String prefix) {
        if (string.length() < prefix.length()) {
            return false;
        }

        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
