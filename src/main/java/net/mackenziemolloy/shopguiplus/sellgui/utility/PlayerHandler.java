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

        String soundValue = plugin.configFile.getString(soundPath);
        return Sound.valueOf(soundValue);
    }

    public static void playSound(Player player, String event) {
        SellGUI sellGUI = SellGUI.getInstance();
        if(!sellGUI.configFile.getBoolean("options.sounds.enabled")) return;

        float volume = 1.0F;
        float pitch = 1.0F;

        if(sellGUI.configFile.isDouble("options.sounds.pitch") || sellGUI.configFile.isInt("options.sounds.pitch")) {
            pitch = (float) sellGUI.configFile.getDouble("options.sounds.pitch");
        }

        if(sellGUI.configFile.isDouble("options.sounds.volume") || sellGUI.configFile.isInt("options.sounds.volume")) {
            volume = (float) sellGUI.configFile.getDouble("options.sounds.volume");
        }

        try {
            Location location = player.getLocation();
            Sound sound = getSound(event);
            player.playSound(location, sound, volume, pitch);
        } catch(Exception ex) {
            if(sellGUI.configFile.getBoolean("options.sounds.error_notifcation")) {
                CommandSender console = Bukkit.getConsoleSender();
                console.sendMessage(ChatColor.DARK_RED + "[ShopGUIPlus-SellGUI] Invalid Sound for version "
                        + sellGUI.version + " => '" + sellGUI.configFile.getString("options.sounds.events." + event)
                        + "' (failed)");
            }
        }
    }
}
