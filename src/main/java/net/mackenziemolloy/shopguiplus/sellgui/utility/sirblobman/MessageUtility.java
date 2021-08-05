package net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageUtility {
    private static final char COLOR_CHAR = '\u00A7';
    
    public static String color(String message) {
        if(message == null) return "";
        
        int minorVersion = VersionUtility.getMinorVersion();
        if(minorVersion >= 16) {
            message = translateHexColorCodes("&#", "", message);
            return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
        }

        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static String translateHexColorCodes(String startTag, String endTag, String message) {
        Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
        Matcher matcher = hexPattern.matcher(message);
        
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while(matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }
}
