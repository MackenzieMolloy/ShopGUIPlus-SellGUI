package net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

public final class VersionUtility {
    /** Pattern that matches how most server jars format the current version */
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\(MC: )([\\d.]+)(\\))");

    /**
     * @return The current Minecraft version of the server (Example: 1.16.5)
     */
    public static String getMinecraftVersion() {
        String bukkitVersion = Bukkit.getVersion();
        Matcher matcher = VERSION_PATTERN.matcher(bukkitVersion);
        return (matcher.find() ? matcher.group(2) : "");
    }

    /**
     * @return The current major.minor version of the server (Example: 1.16)
     */
    public static String getMajorMinorVersion() {
        String minecraftVersion = getMinecraftVersion();
        int lastPeriodIndex = minecraftVersion.lastIndexOf('.');
        return (lastPeriodIndex < 2 ? minecraftVersion : minecraftVersion.substring(0, lastPeriodIndex));
    }
    
    /**
     * @return The current minor version of the server as an integer (Example: {@code 16})
     */
    public static int getMinorVersion() {
        String majorMinorVersion = getMajorMinorVersion();
        int periodIndex = majorMinorVersion.indexOf('.');
        int nextIndex = (periodIndex + 1);

        String minorString = majorMinorVersion.substring(nextIndex);
        return Integer.parseInt(minorString);
    }
}
