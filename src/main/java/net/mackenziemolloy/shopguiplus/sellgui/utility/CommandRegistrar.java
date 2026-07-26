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

public final class CommandRegistrar {

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
            if (command != null
                    && (command.getLabel().equalsIgnoreCase(alias) || command.getAliases().contains(alias))) {
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
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                return (CommandMap) field.get(Bukkit.getPluginManager());
            }
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get command map", exception);
        }
        return null;
    }

    private PluginCommand createPluginCommand(String name, Plugin plugin) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, plugin);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create PluginCommand for alias: " + name, exception);
            return null;
        }
    }
}
