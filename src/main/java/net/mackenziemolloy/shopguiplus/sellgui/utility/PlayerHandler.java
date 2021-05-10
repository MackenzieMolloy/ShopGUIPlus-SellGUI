package net.mackenziemolloy.shopguiplus.sellgui.utility;

import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlayerHandler {

    private final SellGUI sellGUI;

    public PlayerHandler(final SellGUI sellGUI) {
        this.sellGUI = sellGUI;
    }

    public static boolean playSound(Player player, String event) {

        SellGUI sellGUI = SellGUI.getInstance();

        boolean enabled = sellGUI.configFile.getBoolean("options.sounds.enabled");
        if(enabled) {

            double volume = 1.0;
            double pitch = 1.0;

            if(sellGUI.configFile.isDouble("options.sounds.pitch") || sellGUI.configFile.isInt("options.sounds.pitch")) {

                pitch = Double.valueOf(sellGUI.configFile.getString("options.sounds.pitch"));

            }

            if(sellGUI.configFile.isDouble("options.sounds.volume") || sellGUI.configFile.isInt("options.sounds.volume")) {

                volume = Double.valueOf(sellGUI.configFile.getString("options.sounds.volume"));

            }

            if(event.equalsIgnoreCase("open")) {

                try {

                    player.playSound(player.getLocation(), Sound.valueOf(sellGUI.configFile.getString("options.sounds.events.open")), (float) volume, (float) pitch);

                }

                catch(Exception e) {

                    if(sellGUI.configFile.getBoolean("options.sounds.error_notifcation")) {

                        sellGUI.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[ShopGUIPlus-SellGUI] Invalid Sound for version " + sellGUI.version + " => '" + sellGUI.configFile.getString("options.sounds.events.open") + "' (open)");

                    }

                }

            }

            else if(event.equalsIgnoreCase("success")) {

                try {

                    player.playSound(player.getLocation(), Sound.valueOf(sellGUI.configFile.getString("options.sounds.events.success")), (float) volume, (float) pitch);

                }

                catch(Exception e) {

                    if(sellGUI.configFile.getBoolean("options.sounds.error_notifcation")) {

                        sellGUI.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[ShopGUIPlus-SellGUI] Invalid Sound for version " + sellGUI.version + " => '" + sellGUI.configFile.getString("options.sounds.events.success") + "' (success)");

                    }

                }

            }

            else if(event.equalsIgnoreCase("failed")) {

                try {

                    player.playSound(player.getLocation(), Sound.valueOf(sellGUI.configFile.getString("options.sounds.events.failed")), (float) volume, (float) pitch);

                }

                catch(Exception e) {

                    if(sellGUI.configFile.getBoolean("options.sounds.error_notifcation")) {

                        sellGUI.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[ShopGUIPlus-SellGUI] Invalid Sound for version " + sellGUI.version + " => '" + sellGUI.configFile.getString("options.sounds.events.failed") + "' (failed)");

                    }

                }

            }

        }

        else {

            return true;

        }

        return true;

    }

}
