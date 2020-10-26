package net.mackenziemolloy.SGPSellGUI;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;


public class Config {

    private File config = null;
    private YamlConfiguration configC = new YamlConfiguration();

    private File data = null;
    private YamlConfiguration dataC = new YamlConfiguration();

    private final SellGUI sellGUI;

    public Config(SellGUI sellGUI) {
        this.sellGUI = sellGUI;
        generateFiles();
    }

    public void generateFiles() {

        config = new File(sellGUI.getDataFolder(), "config.yml");
        generateConfigurations();
        loadYAML();

    }

    public YamlConfiguration getConfigC(){
        return configC;
    }

    public void reloadConfigC() {
        configC = YamlConfiguration.loadConfiguration(config);
    }

    public void saveConfigC() {
        try {
            configC.save(config);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(" ");
            Bukkit.getConsoleSender().sendMessage("§cShopGUIPlus SellGUI config failed to save.");
            Bukkit.getConsoleSender().sendMessage(" ");
            e.printStackTrace();
        }

    }

    public void loadYAML() {
        try {
            configC.load(config);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void generateConfigurations() {
        if(!config.exists()) {
            sellGUI.saveResource("config.yml", false);
        }
    }

}
