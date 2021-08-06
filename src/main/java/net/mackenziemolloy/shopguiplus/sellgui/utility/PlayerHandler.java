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
        String soundPath = String.format(Locale.US, "options.sounds.events.%s", event);

        String soundValue = plugin.configuration.getString(soundPath);
        return Sound.valueOf(soundValue);
    }

    public static void playSound(Player player, String event) {
        SellGUI sellGUI = JavaPlugin.getPlugin(SellGUI.class);
        if(!sellGUI.configuration.getBoolean("options.sounds.enabled")) return;

        float volume = 1.0F;
        float pitch = 1.0F;

        if(sellGUI.configuration.isDouble("options.sounds.pitch")
                || sellGUI.configuration.isInt("options.sounds.pitch")) {
            pitch = (float) sellGUI.configuration.getDouble("options.sounds.pitch");
        }

        if(sellGUI.configuration.isDouble("options.sounds.volume")
                || sellGUI.configuration.isInt("options.sounds.volume")) {
            volume = (float) sellGUI.configuration.getDouble("options.sounds.volume");
        }

        try {
            Location location = player.getLocation();
            Sound sound = getSound(event);
            player.playSound(location, sound, volume, pitch);
        } catch(Exception ex) {
            if(sellGUI.configuration.getBoolean("options.sounds.error_notifcation")) {
                CommandSender console = Bukkit.getConsoleSender();
                console.sendMessage(ChatColor.DARK_RED + "[ShopGUIPlus-SellGUI] Invalid Sound for version "
                        + sellGUI.version + " => '" + sellGUI.configuration.getString("options.sounds.events."
                        + event) + "' (failed)");
            }
        }
    }
}
