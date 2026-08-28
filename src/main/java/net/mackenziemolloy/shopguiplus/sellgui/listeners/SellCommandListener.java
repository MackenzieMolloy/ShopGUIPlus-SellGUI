package net.mackenziemolloy.shopguiplus.sellgui.listeners;

import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

public class SellCommandListener implements Listener {

    private final SellGUI plugin;

    public SellCommandListener(SellGUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        String[] args = message.split(" ");

        if (args.length != 1) {
            return;
        }

        String command = args[0].substring(1).toLowerCase(Locale.US);
        java.util.List<String> aliases = plugin.getConfiguration().getStringList("options.commands.aliases");

        if (aliases.contains(command)) {
            event.setCancelled(true);
            plugin.getCommand("sellgui").execute(event.getPlayer(), "sellgui", new String[0]);
        }
    }
}
