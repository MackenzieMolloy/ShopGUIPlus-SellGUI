package net.mackenziemolloy.shopguiplus.sellgui.utility;

import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;
import org.bukkit.Bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

public class CommandRegistrar {

    private final SellGUI plugin;

    public CommandRegistrar(SellGUI plugin) {
        this.plugin = plugin;
    }

    public void registerAliases() {
        List<String> aliases = plugin.getConfiguration().getStringList("options.commands.aliases");
        if (aliases == null || aliases.isEmpty()) {
            return;
        }

        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            plugin.getLogger().severe("Could not retrieve CommandMap. Custom aliases will not work.");
            return;
        }

        PluginCommand sellGuiCommand = plugin.getCommand("sellgui");
        if (sellGuiCommand == null) {
            plugin.getLogger().severe("The main 'sellgui' command is not registered!");
            return;
        }

        for (String alias : aliases) {
            Command command = commandMap.getCommand(alias);
            if (command != null && !command.getLabel().equalsIgnoreCase(alias)) {
                // If the command exists but under a fallback prefix (plugin:cmd), we might
                // still want to register ours?
                // Bukkit's getCommand returns the first match.
                // If Essentials has 'sell', getCommand('sell') returns it.
            }

            // We only skip if there is a command that directly matches the alias
            if (command != null
                    && (command.getLabel().equalsIgnoreCase(alias) || command.getAliases().contains(alias))) {
                // plugin.getLogger().info("Skipping registration of alias '" + alias + "' as it
                // is already registered.");
                continue;
            }

            PluginCommand aliasCommand = createPluginCommand(alias, plugin);
            if (aliasCommand != null) {
                aliasCommand.setExecutor(sellGuiCommand.getExecutor());
                aliasCommand.setTabCompleter(sellGuiCommand.getTabCompleter());
                aliasCommand.setDescription(sellGuiCommand.getDescription());

                commandMap.register(plugin.getDescription().getName(), aliasCommand);
            }
        }
    }

    private CommandMap getCommandMap() {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);
                return (CommandMap) f.get(Bukkit.getPluginManager());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get command map", e);
        }
        return null;
    }

    private PluginCommand createPluginCommand(String name, Plugin plugin) {
        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            return c.newInstance(name, plugin);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create PluginCommand for alias: " + name, e);
            return null;
        }
    }
}
