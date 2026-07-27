package net.mackenziemolloy.shopguiplus.sellgui.utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman.VersionUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;

public class PlayerHandler {

    private static Sound getSound(String event) {
        SellGUI plugin = JavaPlugin.getPlugin(SellGUI.class);
        CommentedConfiguration configuration = plugin.getConfiguration();
        String soundPath = String.format(Locale.US, "options.sounds.events.%s", event);

        String soundValue = configuration.getString(soundPath);

        try {
            // Minecraft 1.12 has some older sound class
            if (VersionUtility.getMinorVersion() < 13) {
                // Dynamically resolve org.bukkit.Sound
                Class<?> soundClass = Class.forName("org.bukkit.Sound");

                // Use reflection to call valueOf(String)
                Method valueOfMethod = soundClass.getMethod("valueOf", String.class);
                Object soundEnum = valueOfMethod.invoke(null, soundValue);

                return (Sound) soundEnum;
            }

            return Sound.valueOf(soundValue);
        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | IllegalArgumentException | NoSuchMethodException ex) {
            if (configuration.getBoolean("options.sounds.error_notification")) {
                CommandSender console = Bukkit.getConsoleSender();
                console.sendMessage(ChatColor.DARK_RED + "[ShopGUIPlus-SellGUI] Invalid Sound for version "
                  + plugin.getVersion() + " => '" + configuration.getString("options.sounds.events."
                  + event) + "' (failed) ");
            }

            return null;
        }
    }

    public static void playSound(Player player, String event) {
        SellGUI plugin = JavaPlugin.getPlugin(SellGUI.class);
        CommentedConfiguration configuration = plugin.getConfiguration();
        if (!configuration.getBoolean("options.sounds.enabled")) {
            return;
        }

        float volume = 1.0F;
        float pitch = 1.0F;

        if (configuration.isDouble("options.sounds.pitch") || configuration.isInt("options.sounds.pitch")) {
            pitch = (float) configuration.getDouble("options.sounds.pitch");
        }

        if (configuration.isDouble("options.sounds.volume") || configuration.isInt("options.sounds.volume")) {
            volume = (float) configuration.getDouble("options.sounds.volume");
        }

        Location location = player.getLocation();
        Sound sound = getSound(event);
        if (sound == null) {
            return;
        }

        player.playSound(location, sound, volume, pitch);
    }
}
