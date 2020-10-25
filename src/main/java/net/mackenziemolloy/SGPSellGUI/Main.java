package net.mackenziemolloy.SGPSellGUI;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    public Config configHandler = new Config(this);

    @Override
    public void onEnable() {
        new Commands(this);

        //Bukkit.getServer().getPluginManager().registerEvents(this, this);

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

        configHandler.generateFiles();

    }

}
