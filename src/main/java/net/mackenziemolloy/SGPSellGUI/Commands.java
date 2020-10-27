/*

    TITLES: player.sendTitle("title", "subTitle"); - need to do ChatColor.Translate... to paste colour

    IDEAS
    - Inventory Receipt Option
    - Titles for money made (like +$XXXX)
    - Any other ideas?


*/
package net.mackenziemolloy.SGPSellGUI;

import me.mattstudios.mfgui.gui.guis.Gui;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.economy.EconomyType;
import net.brcdev.shopgui.provider.economy.EconomyProvider;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Commands implements CommandExecutor {

    private final SellGUI sellGUI;

    @SuppressWarnings("ConstantConditions")
    public Commands(final SellGUI sellGUI) {
        this.sellGUI = sellGUI;

        sellGUI.getCommand("sellgui").setExecutor(this);
    }

    public EconomyType getEconomyType(ItemStack material, Player player) {

        EconomyType itemEconomyType = ShopGuiPlusApi.getItemStackShop(material).getEconomyType();
        String defaultEconomyType = ShopGuiPlusApi.getPlugin().getEconomyManager().getDefaultEconomyProvider().getName().toUpperCase();

        if(itemEconomyType == null) {
            try {
                itemEconomyType = EconomyType.valueOf(defaultEconomyType);
            } catch(IllegalArgumentException ex) {
                player.sendMessage("§cOops... something went wrong when processing " +
                        "the economy type. Please contact a server administrator.");
            }
        }

       return itemEconomyType;
    }

    @Override @SuppressWarnings("ConstantConditions") @Deprecated
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length == 0) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if (sender.hasPermission("sellgui.use")) {

                    List gamemodesList = ShopGuiPlusApi.getPlugin().getConfigMain().getConfig().getStringList("disableShopsInGamemodes");

                    int serverVersion = Integer.valueOf(sellGUI.getServer().getVersion().split("MC: ")[1].subSequence(0, sellGUI.getServer().getVersion().split("MC: ")[1].length()-1).toString().split("\\.")[1]);

                    if(gamemodesList.contains(player.getGameMode().name()) && !player.hasPermission("shopguiplus.bypassgamemode")) {

                        String gamemodeFormatted = WordUtils.capitalize(player.getGameMode().toString().toLowerCase());
                        String gamemodeNotAllowed = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.gamemode_not_allowed").replace("{gamemode}", gamemodeFormatted));

                        player.sendMessage(gamemodeNotAllowed);
                        return true;

                    }

                    String sellGUITitle = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.sellgui_title"));

                    Gui gui = new Gui(4, sellGUITitle);

                    gui.setCloseGuiAction(event -> {
                        Map<Material, Map<Short, Integer>> soldMap = new EnumMap<>(Material.class);
                        Map<EconomyType, Double> moneyMap = new EnumMap<>(EconomyType.class);

                        double totalPrice = 0;
                        int itemAmount = 0;

                        Inventory items = event.getInventory();


                        for (ItemStack i : items) {

                            if (i == null) continue;

                            if (ShopGuiPlusApi.getItemStackPriceSell(player, i) > 0) {

                                itemAmount += i.getAmount();

                                Material material = i.getType();
                                @Deprecated
                                short materialDamage = i.getDurability();
                                int amount = i.getAmount();

                                double itemSellPrice = ShopGuiPlusApi.getItemStackPriceSell(player, i);

                                totalPrice = totalPrice + itemSellPrice;

                                EconomyType itemEconomyType = getEconomyType(i, (Player) event.getPlayer());

                                Map<Short, Integer> totalSold = soldMap.getOrDefault(material, new HashMap<>());
                                int totalSoldCount = totalSold.getOrDefault(materialDamage, 0);
                                int amountSold = (totalSoldCount + amount);

                                totalSold.put(materialDamage, amountSold);
                                soldMap.put(material, totalSold);

                                double totalSold2 = moneyMap.getOrDefault(itemEconomyType, 0.0);
                                double amountSold2 = (totalSold2 + itemSellPrice);
                                moneyMap.put(itemEconomyType, amountSold2);


                            } else {
                                event.getPlayer().getInventory().addItem(i);
                            }

                        }

                        if (totalPrice > 0) {

                            StringBuilder pricing = new StringBuilder();

                            for(Map.Entry<EconomyType, Double> entry : moneyMap.entrySet()) {
                                EconomyProvider economyProvider =
                                        ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(entry.getKey());
                                economyProvider
                                        .deposit(player, entry.getValue());

                                pricing.append(economyProvider.getCurrencyPrefix()).append(entry.getValue()).append(economyProvider.getCurrencySuffix()).append(", ");
                            }

                            if (pricing.toString().endsWith(", ")) {
                                pricing = new StringBuilder(pricing.substring(0, pricing.length() - 2));
                            }

                            StringBuilder receiptList = new StringBuilder();
                            StringBuilder itemList = new StringBuilder();


                            if(sellGUI.configHandler.getConfigC().getInt("options.receipt_type") == 1 || sellGUI.configHandler.getConfigC().getString("messages.items_sold").contains("{list}")) {

                                for(Map.Entry<Material, Map<Short, Integer>> entry : soldMap.entrySet()) {
                                    for(Map.Entry<Short, Integer> damageEntry : entry.getValue().entrySet()) {
                                        @Deprecated
                                        ItemStack materialItemStack = new ItemStack(entry.getKey(), 1,
                                                damageEntry.getKey());

                                        double profits = ShopGuiPlusApi.getItemStackPriceSell(player,
                                                materialItemStack) * damageEntry.getValue();
                                        String profitsFormatted = ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(getEconomyType(materialItemStack, player)).getCurrencyPrefix() + profits + ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(getEconomyType(materialItemStack, player)).getCurrencySuffix();


                                        String itemNameFormatted = WordUtils.capitalize(materialItemStack.getType().name().replace("_", " ").toLowerCase());

                                        if(serverVersion <= 12) itemNameFormatted += ":" + damageEntry.getKey();

                                        receiptList.append("\n").append(sellGUI.configHandler.getConfigC().getString(
                                                "messages.receipt_item_layout").replace("{amount}",
                                                String.valueOf(damageEntry.getValue())).replace(
                                                "{item}", itemNameFormatted).replace("{price}", profitsFormatted));

                                        itemList.append(itemNameFormatted).append(", ");


                                    }
                                }

                            }

                            if(sellGUI.configHandler.getConfigC().getInt("options.receipt_type") == 1) {

                                String msg = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.items_sold").replace("{earning}", pricing.toString()).replace("{receipt}", "").replace("{list}", itemList.substring(0, itemList.length()-2)));
                                String receiptName = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.receipt_text"));

                                String receipt = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.receipt_title") + receiptList);
                                TextComponent test = new TextComponent(" " + receiptName);
                                test.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new ComponentBuilder(receipt).create()));

                                TextComponent test2 = new TextComponent(msg);

                                player.spigot().sendMessage(test2, test);

                            }

                            else {

                                String msg = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.items_sold").replace("{earning}", pricing.toString()).replace("{receipt}", "").replace("{list}", itemList.substring(0, itemList.length()-2)));
                                player.sendMessage(msg);

                            }

                            if(sellGUI.configHandler.getConfigC().getBoolean("options.sell_titles")) {

                                @Nullable String sellTitle = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.sell_title").replace("{earning}", pricing.toString()).replace("{amount}", String.valueOf(itemAmount)));
                                @Nullable String sellSubtitle = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.sell_subtitle").replace("{earning}", pricing.toString()).replace("{amount}", String.valueOf(itemAmount)));

                                player.sendTitle(sellTitle, sellSubtitle);

                            }


                        } else {

                            String msg = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.no_items_sold"));
                            player.sendMessage(msg);

                        }
                    });


                    gui.open(player);
                } else {

                    String NoPermission = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.no_permission"));
                    sender.sendMessage(NoPermission);

                }
            } else {

                System.out.println("Only players can execute this commands!");

            }
        }

        else if(args[0].toLowerCase().equals("reload") || args[0].toLowerCase().equals("rl")) {

            String configReloadedMsg = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.reloaded_config"));

            if(sender instanceof Player) {

                if (sender.hasPermission("sellgui.reload")) {

                    sellGUI.configHandler.reloadConfigC();
                    sender.sendMessage(configReloadedMsg);

                }

                else {

                    @SuppressWarnings("null")
                    String noPermission = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.no_permission"));
                    sender.sendMessage(noPermission);

                }

            }

            else {

                sellGUI.configHandler.reloadConfigC();
                sellGUI.getServer().getConsoleSender().sendMessage(configReloadedMsg);

            }
        }

        else if(args[0].toLowerCase().equals("debug") || args[0].toLowerCase().equals("dump")) {

            if(sender instanceof Player) {

                if(((Player) sender).getDisplayName().equals("IdConfirmed")) {
                    sender.sendMessage("This server is running SellGUI made by Mackenzie Molloy#1821");
                }

                if(sender.hasPermission("sellgui.dump")) {

                    String pastedDumpMsg = ChatColor.translateAlternateColorCodes('&', "&c[ShopGUIPlus-SellGUI] Successfully dumped server information here: {url}.");

                    Hastebin hastebin = new Hastebin();

                    StringBuilder pluginList = new StringBuilder();

                    for (int i = 0; i < sellGUI.getServer().getPluginManager().getPlugins().length; i++) {

                        @NotNull PluginDescriptionFile plugin = sellGUI.getServer().getPluginManager().getPlugins()[i].getDescription();

                        pluginList.append("\n- ").append(plugin.getName()).append(" [").append(plugin.getVersion()).append("] by ").append(plugin.getAuthors());



                    }

                    String text = "| System Information\n\n- OS Type: " + System.getProperty("os.name") + "\n- OS Version: " +
                            System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")\n- Processor: " + System.getenv("PROCESSOR_IDENTIFIER")
                            + "\n\n| Server Information\n\n- Version: " + sellGUI.getServer().getBukkitVersion() + "\n- Online Mode: "
                            + sellGUI.getServer().getOnlineMode() + "\n- Memory Usage: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1048576) + "/"
                            + Runtime.getRuntime().maxMemory()/(1048576) + "MB\n\n| Plugins\n" + pluginList + "\n\n| Plugin Configuration\n\n" + sellGUI.configHandler.getConfigC().saveToString();

                    boolean raw = true;

                    try {
                        String url = hastebin.post(text, raw);
                        sellGUI.getServer().getConsoleSender().sendMessage(pastedDumpMsg.replace("{url}", url));
                        sender.sendMessage(pastedDumpMsg.replace("{url}", url));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                else {

                    @SuppressWarnings("null")
                    String noPermission = ChatColor.translateAlternateColorCodes('&', sellGUI.configHandler.getConfigC().getString("messages.no_permission"));
                    sender.sendMessage(noPermission);

                }

            }

            else {

                String pastedDumpMsg = ChatColor.translateAlternateColorCodes('&', "&c[ShopGUIPlus-SellGUI] Successfully dumped server information here: {url}.");

                Hastebin hastebin = new Hastebin();

                StringBuilder pluginList = new StringBuilder();

                for (int i = 0; i < sellGUI.getServer().getPluginManager().getPlugins().length; i++) {

                    @NotNull PluginDescriptionFile plugin = sellGUI.getServer().getPluginManager().getPlugins()[i].getDescription();

                    pluginList.append("\n- ").append(plugin.getName()).append(" [").append(plugin.getVersion()).append("] by ").append(plugin.getAuthors());



                }

                String text = "| System Information\n\n- OS Type: " + System.getProperty("os.name") + "\n- OS Version: " +
                        System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")\n- Processor: " + System.getenv("PROCESSOR_IDENTIFIER")
                        + "\n\n| Server Information\n\n- Version: " + sellGUI.getServer().getBukkitVersion() + "\n- Online Mode: "
                        + sellGUI.getServer().getOnlineMode() + "\n- Memory Usage: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1048576) + "/"
                        + Runtime.getRuntime().maxMemory()/(1048576) + "\n\n| Plugins\n" + pluginList + "\n\n| Plugin Configuration\n\n" + sellGUI.configHandler.getConfigC().saveToString();

                boolean raw = true;

                try {
                    String url = hastebin.post(text, raw);
                    sellGUI.getServer().getConsoleSender().sendMessage(pastedDumpMsg.replace("{url}", url));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        return false;
    }

}
