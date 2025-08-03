package net.mackenziemolloy.shopguiplus.sellgui.utility;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;

public class PlayerHandler {
    private static Sound getSound(String event) throws IllegalArgumentException {
        SellGUI plugin = JavaPlugin.getPlugin(SellGUI.class);
        CommentedConfiguration configuration = plugin.getConfiguration();
        String soundPath = String.format(Locale.US, "options.sounds.events.%s", event);

        String soundValue = configuration.getString(soundPath);
        return Sound.valueOf(soundValue);
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

        try {
            Location location = player.getLocation();
            Sound sound = getSound(event);
            player.playSound(location, sound, volume, pitch);
        } catch (Exception ex) {
            if (configuration.getBoolean("options.sounds.error_notification")) {
                CommandSender console = Bukkit.getConsoleSender();
                console.sendMessage(ChatColor.DARK_RED + "[ShopGUIPlus-SellGUI] Invalid Sound for version "
                        + plugin.getVersion() + " => '" + configuration.getString("options.sounds.events."
                        + event) + "' (failed) ");
            }
        }
    }
}
