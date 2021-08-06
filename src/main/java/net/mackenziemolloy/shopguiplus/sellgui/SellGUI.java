package net.mackenziemolloy.shopguiplus.sellgui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import net.mackenziemolloy.shopguiplus.sellgui.command.CommandSellGUI;
import net.mackenziemolloy.shopguiplus.sellgui.utility.CommentedConfiguration;
import net.mackenziemolloy.shopguiplus.sellgui.utility.UpdateChecker;
import net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman.VersionUtility;
import org.bstats.bukkit.Metrics;

public final class SellGUI extends JavaPlugin {
    private CommentedConfiguration configuration;
    private String version;
    
    public SellGUI() {
        this.configuration = new CommentedConfiguration();
        this.version = null;
    }

    @Override
    public void onEnable() {
        new CommandSellGUI(this).register();
        Logger logger = getLogger();
        
        this.version = VersionUtility.getNetMinecraftServerVersion();
        logger.info("Your server is running version '" + this.version + "'.");
        
        generateFiles();
        setupMetrics();
        setupUpdates();
        
        logger.info("*-*");
        logger.info("ShopGUIPlus SellGUI");
        logger.info("Made by Mackenzie Molloy");
        logger.info("*-*");
    }
    
    public CommentedConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    private void setupMetrics() {
        new Metrics(this, 9356);
    }
    
    private void setupUpdates() {
        PluginDescriptionFile description = getDescription();
        String localVersion = description.getVersion();
        String pluginPrefix = description.getPrefix();
    
        UpdateChecker updateChecker = new UpdateChecker(this, 85170);
        updateChecker.getVersion(updateVersion -> {
            CommandSender console = Bukkit.getConsoleSender();
            if(localVersion.contains("dev")) {
                String message = (ChatColor.DARK_RED + "[" + pluginPrefix + "] You are running a DEVELOPMENT " +
                        "build. This may contain bugs.");
                console.sendMessage(message);
                return;
            }
            
            if(localVersion.equalsIgnoreCase(updateVersion)) {
                String message = (ChatColor.GREEN + "[" + pluginPrefix + "] You are running the LATEST release.");
                console.sendMessage(message);
                return;
            }
            
            String message = (ChatColor.DARK_RED + "[" + pluginPrefix + "] There is a new update available." +
                    " Please update ASAP. Download: https://www.spigotmc.org/resources/85170/");
            console.sendMessage(message);
        });
    }

    public void generateFiles() {
        saveDefaultConfig();
        File pluginFolder = getDataFolder();
        File configFile = new File(pluginFolder, "config.yml");
        
        try {
            this.configuration.load(configFile);
    
            InputStream jarConfig = getResource("config.yml");
            this.configuration.syncWithConfig(configFile, jarConfig, "stupid_option");
        } catch(IOException | InvalidConfigurationException ex) {
            Logger logger = getLogger();
            logger.log(Level.SEVERE, "Failed to load the 'config.yml' file due to an error:", ex);
        }
    }
}
