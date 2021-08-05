package net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman;

import org.bukkit.ChatColor;

public final class MessageUtility {
    /**
     * @param message The message that will be colored
     * @return A new string containing {@code message} but with the color codes replaced, or an empty string if message was {@code null}.
     * @see ChatColor#translateAlternateColorCodes(char, String)
     * @see HexColorUtility#replaceHexColors(char, String)
     */
    public static String color(String message) {
        if(message == null) return "";
        
        int minorVersion = VersionUtility.getMinorVersion();
        if(minorVersion >= 16) {
            message = HexColorUtility.replaceHexColors('&', message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
