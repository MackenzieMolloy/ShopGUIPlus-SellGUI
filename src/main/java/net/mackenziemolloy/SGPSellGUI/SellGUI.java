package net.mackenziemolloy.SGPSellGUI;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class SellGUI extends JavaPlugin implements Listener {

    public CommentedConfiguration configFile;

    @Override
    public void onEnable() {
        new Commands(this);

        this.getLogger().info("*-*");
        this.getLogger().info("ShopGUIPlus SellGUI");
        this.getLogger().info("Made by Mackenzie Molloy");
        this.getLogger().info("*-*");

        String version = "";
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        getLogger().info("Your server is running version " + version);

        this.generateFiles();

        Metrics metrics = new Metrics(this, 9356);


    }

    public void generateFiles() {
        File file = new File(getDataFolder(), "config.yml");

        if(!file.exists()) saveResource("config.yml", false);

        configFile = CommentedConfiguration.loadConfiguration(file);
        try {
            configFile.syncWithConfig(file, getResource("config.yml"), "updates");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
