package net.mackenziemolloy.SGPSellGUI;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;


public class Config {

    private File config = null;
    private YamlConfiguration yamlConfiguration /* more meaningful variable name should be used */ = new
    YamlConfiguration();

    private File data = null;
    private YamlConfiguration dataC = new YamlConfiguration();

    private final Main main;

    public Config(Main main) {
        this.main = main;
        generateFiles();
    }

    public void generateFiles() {

        config = new File(main.getDataFolder(), "config.yml");
        generateConfigurations();
        loadYAML();

    }

    public YamlConfiguration getYamlConfiguration(){
        return yamlConfiguration;
    }

    public void reloadYamlConfiguration() {
        yamlConfiguration = YamlConfiguration.loadConfiguration(config);
    }

    public void saveConfigC() {
        try {
            yamlConfiguration.save(config);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(" ");
            Bukkit.getConsoleSender().sendMessage("§cShopGUIPlus SellGUI config failed to save.");
            Bukkit.getConsoleSender().sendMessage(" ");
            e.printStackTrace();
        }

    }

    public void loadYAML() {
        try {
            yamlConfiguration.load(config);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void generateConfigurations() {
        if(!config.exists()) {
            main.saveResource("config.yml", false);
        }
    }

}
