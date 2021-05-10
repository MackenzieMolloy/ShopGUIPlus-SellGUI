package net.mackenziemolloy.shopguiplus.sellgui;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import net.mackenziemolloy.shopguiplus.sellgui.command.CommandSellGUI;
import net.mackenziemolloy.shopguiplus.sellgui.utility.CommentedConfiguration;
import net.mackenziemolloy.shopguiplus.sellgui.utility.UpdateChecker;
import org.bstats.bukkit.Metrics;

public class SellGUI extends JavaPlugin {

    public CommentedConfiguration configFile;
    public static SellGUI sellGUI;
    public String version = "";

    @Override
    public void onEnable() {
        sellGUI = this;
        new CommandSellGUI(this).register();

        Logger logger = this.getLogger();
        logger.info("*-*");
        logger.info("ShopGUIPlus SellGUI");
        logger.info("Made by Mackenzie Molloy");
        logger.info("*-*");

        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        logger.info("Your server is running version " + version);

        this.generateFiles();

        new Metrics(this, 9356);
        new UpdateChecker(this, 85170).getVersion(version -> {
            CommandSender console = getServer().getConsoleSender();
            if(this.getDescription().getVersion().toLowerCase().contains("dev")) {
                console.sendMessage(ChatColor.DARK_RED + "[ShopGUIPlus-SellGUI] You are running a DEVELOPMENT BUILD, this may contain bugs.");
            }

            else if(this.getDescription().getVersion().equalsIgnoreCase(version)) {
                console.sendMessage(ChatColor.GREEN + "[ShopGUIPlus-SellGUI] You are running the LATEST release.");
            }

            else {
                console.sendMessage(ChatColor.DARK_RED + "[ShopGUIPlus-SellGUI] There is a new update available, please update ASAP. Download: https://www.spigotmc.org/resources/85170/");
            }
        });
    }

    public void generateFiles() {
        saveDefaultConfig();

        File file = new File(getDataFolder(), "config.yml");
        configFile = CommentedConfiguration.loadConfiguration(file);
        try {
            configFile.syncWithConfig(file, getResource("config.yml"), "stupid_option"); //decorations
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static SellGUI getInstance() {
        return sellGUI;
    }
}
