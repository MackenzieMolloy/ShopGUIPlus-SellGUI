package net.mackenziemolloy.SGPSellGUI;

import net.mackenziemolloy.SGPSellGUI.Utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class SellGUI extends JavaPlugin {

    public CommentedConfiguration configFile;

    @Override
    public void onEnable() {
        new Commands(this);

        this.getLogger().info("*-*");
        this.getLogger().info("ShopGUIPlus SellGUI");
        this.getLogger().info("Made by Mackenzie Molloy");
        this.getLogger().info("*-*");

        String version2 = "";
        try {
            version2 = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        getLogger().info("Your server is running version " + version2);

        this.generateFiles();

        Metrics metrics = new Metrics(this, 9356);

        new UpdateChecker(this, 85170).getVersion(version -> {

            if(this.getDescription().getVersion().toLowerCase().contains("dev")) {
                getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "You are running a DEVELOPMENT BUILD, this may contain bugs.");
            }

            else if(this.getDescription().getVersion().equalsIgnoreCase(version)) {
                // DO NOTHING
            }

            else {

                getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[ShopGUIPlus-SellGUI] There is a new update available, please update ASAP. Download: https://www.spigotmc.org/resources/85170/");

            }

        });

    }

    public void generateFiles() {
        File file = new File(getDataFolder(), "config.yml");

        if(!file.exists()) saveResource("config.yml", false);

        configFile = CommentedConfiguration.loadConfiguration(file);
        try {
            configFile.syncWithConfig(file, getResource("config.yml"), "stupid_option"); //decorations
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
