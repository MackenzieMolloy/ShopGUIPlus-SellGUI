package net.mackenziemolloy.shopguiplus.sellgui.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.StringUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.economy.EconomyType;
import net.brcdev.shopgui.provider.economy.EconomyProvider;
import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;
import net.mackenziemolloy.shopguiplus.sellgui.utility.Hastebin;
import net.mackenziemolloy.shopguiplus.sellgui.utility.PlayerHandler;
import net.mackenziemolloy.shopguiplus.sellgui.utility.ShopHandler;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.Nullable;

public final class CommandSellGUI implements TabExecutor {
    private final SellGUI plugin;
    public CommandSellGUI(SellGUI plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) {
            List<String> valueSet = Arrays.asList("rl", "reload", "debug", "dump");
            return StringUtil.copyPartialMatches(args[0], valueSet, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) return commandBase(sender);

        String sub = args[0].toLowerCase(Locale.US);
        switch(sub) {
            case "rl":
            case "reload":
                return commandReload(sender);

            case "debug":
            case "dump":
                return commandDebug(sender);

            default: break;
        }

        return false;
    }

    public void register() {
        PluginCommand pluginCommand = this.plugin.getCommand("sellgui");
        if(pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
    }

    private boolean commandReload(CommandSender sender) {
        if(!sender.hasPermission("sellgui.reload")) {
            sendMessage(sender, "no_permission");
            return true;
        }

        CompletableFuture.runAsync(this.plugin::generateFiles).whenComplete((success, error) -> {
            if(error != null) {
                sender.sendMessage(ChatColor.RED + "An error occurred, please check the server console.");
                error.printStackTrace();
                return;
            }

            sendMessage(sender, "reloaded_config");
            if(sender instanceof Player) {
                Player player = (Player) sender;
                PlayerHandler.playSound(player, "success");
            }
        });
        return true;
    }

    private boolean commandDebug(CommandSender sender) {
        if(sender instanceof Player && ((Player) sender).getUniqueId().toString().equals("6b23291c-495b-478d-9055-d0d151206bff")) {
            String pluginVersion = this.plugin.getDescription().getVersion();
            sender.sendMessage(String.format(Locale.US, "This server is running SellGUI made by Mackenzie Molloy#1821 v%s", pluginVersion));
        }

        if(!sender.hasPermission("sellgui.dump")) {
            sendMessage(sender, "no_permission");
            return true;
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin[] pluginArray = pluginManager.getPlugins();
        List<String> pluginInfoList = new ArrayList<>();

        for(Plugin plugin : pluginArray) {
            String pluginName = plugin.getName();
            String pluginVersion = plugin.getDescription().getVersion();
            String pluginAuthorList = String.join(", ", plugin.getDescription().getAuthors());

            String pluginInfo = String.format(Locale.US, "- %s v%s by %s", pluginName, pluginVersion, pluginAuthorList);
            pluginInfoList.add(pluginInfo);
        }

        String pasteRaw = "| System Information\n\n" + "- OS Type: " + System.getProperty("os.name") + "\n" +
                "- OS Version: " + System.getProperty("os.version") +
                " (" + System.getProperty("os.arch") + ")\n" +
                "- Processor: " + System.getenv("PROCESSOR_IDENTIFIER") +
                "\n\n| Server Information\n\n" +
                "- Version: " + Bukkit.getBukkitVersion() + "\n" +
                "- Online Mode: " + Bukkit.getOnlineMode() + "\n" +
                "- Memory Usage: " + getMemoryUsage() +
                "\n\n| Plugins\n" + String.join("\n", pluginInfoList) +
                "\n\n| Plugin Configuration\n\n" + this.plugin.configFile.saveToString();
        try {
            String pasteUrl = new Hastebin().post(pasteRaw, true);
            String pastedDumpMsg = ChatColor.translateAlternateColorCodes('&', "&c[ShopGUIPlus-SellGUI] Successfully dumped server information here: %s.");

            String message = String.format(Locale.US, pastedDumpMsg, pasteUrl);
            Bukkit.getConsoleSender().sendMessage(message);
            sender.sendMessage(message);

            if(sender instanceof Player) {
                Player player = (Player) sender;
                PlayerHandler.playSound(player, "success");
            }
        } catch(IOException ex) {
            sender.sendMessage(ChatColor.RED + "An error occurred, please check the console:");
            ex.printStackTrace();

        }

        return true;
    }

    private boolean commandBase(CommandSender sender) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;
        if(!player.hasPermission("sellgui.use")) {
            sendMessage(player, "no_permission");
            return true;
        }

        ShopGuiPlugin shopGuiPlugin = ShopGuiPlusApi.getPlugin();
        List<String> invalidGameModeList = shopGuiPlugin.getConfigMain().getConfig().getStringList("disableShopsInGamemodes");
        if(invalidGameModeList.contains(player.getGameMode().name()) && !player.hasPermission("shopguiplus.bypassgamemode")) {
            String gamemodeFormatted = WordUtils.capitalize(player.getGameMode().toString().toLowerCase());
            String gamemodeNotAllowed = ChatColor.translateAlternateColorCodes('&', this.plugin.configFile.getString("messages.gamemode_not_allowed").replace("{gamemode}", gamemodeFormatted));
            player.sendMessage(gamemodeNotAllowed);
            return true;
        }

        int serverVersion = Integer.parseInt(Bukkit.getVersion().split("MC: ")[1].subSequence(0, Bukkit.getVersion().split("MC: ")[1].length()-1).toString().split("\\.")[1]);String sellGUITitle = ChatColor.translateAlternateColorCodes('&', this.plugin.configFile.getString("messages.sellgui_title"));
        int guiSize = this.plugin.configFile.getInt("options.rows");
        if(guiSize > 6 || guiSize < 1) {
            guiSize = 6;
        }

        Gui gui = new Gui(guiSize, sellGUITitle);
        PlayerHandler.playSound(player, "open");

        List<Integer> ignoredSlots = new ArrayList<>();

        if(!(this.plugin.configFile.getConfigurationSection("options.decorations") == null) && this.plugin.configFile.getConfigurationSection("options.decorations").getKeys(false).size() > 0) {
            for (int i = 0; i < this.plugin.configFile.getConfigurationSection("options.decorations").getKeys(false).size(); i++) {

                @Nullable Object[] ProcessItem = this.plugin.configFile.getConfigurationSection("options.decorations").getKeys(false).toArray();
                ConfigurationSection decorations = this.plugin.configFile.getConfigurationSection("options.decorations");

                if (decorations.getString(ProcessItem[i] + ".item.material") == null ||
                        Material.matchMaterial(decorations.getString(ProcessItem[i] + ".item.material")) == null ||
                        decorations.getString(ProcessItem[i] + ".slot") == null ||
                        decorations.getInt(ProcessItem[i] + ".slot") > (guiSize * 9) - 1 ||
                        decorations.getInt(ProcessItem[i] + ".slot") < 0) {

                    this.plugin.getLogger().info("Error loading decoration item identified as " + ProcessItem[i]);
                    continue;

                }

                ItemStack toAdd = new ItemStack(Material.matchMaterial(decorations.getString(ProcessItem[i] + ".item.material")));

                if(!(String.valueOf(decorations.getInt(ProcessItem[i] + ".item.damage")) == null)) {
                    toAdd.setDurability((short) decorations.getInt(ProcessItem[i] + ".item.damage"));
                }

                if (String.valueOf(decorations.get(ProcessItem[i] + ".item.quantity")) != "null") {
                    toAdd.setAmount(decorations.getInt(ProcessItem[i] + ".item.quantity"));
                }

                ItemMeta im = toAdd.getItemMeta();

                if (!(decorations.getString(ProcessItem[i] + ".item.name") == null)) {
                    String ItemName = ChatColor.translateAlternateColorCodes('&',
                            String.valueOf(decorations.getString(ProcessItem[i] + ".item.name")));

                    im.setDisplayName(ItemName);
                }

                if(!(String.valueOf(decorations.getList(ProcessItem[i] + ".item.lore")) == null)) {

                    List<String> loreToSet = new ArrayList<>();

                    for(int ii = 0; ii < decorations.getStringList(ProcessItem[i] + ".item.lore").size(); ii++) {

                        List<String> loreLines = decorations.getStringList(ProcessItem[i] + ".item.lore");

                        String loreLineToAdd = ChatColor.translateAlternateColorCodes('&', loreLines.get(ii));

                        loreToSet.add(loreLineToAdd);

                    }

                    im.setLore(loreToSet);

                }

                List<String> consoleCommands = new ArrayList<>();

                if (!(String.valueOf(decorations.getList(ProcessItem[i] + ".commandsOnClickConsole")) == null)) {

                    for(int iii = 0; iii < decorations.getStringList(ProcessItem[i] + ".commandsOnClickConsole").size(); iii++) {

                        consoleCommands.add(decorations.getStringList(ProcessItem[i] + ".commandsOnClickConsole").get(iii));

                    }

                }

                List<String> playerCommands = new ArrayList<>();

                if (!(String.valueOf(decorations.getList(ProcessItem[i] + ".commandsOnClick")) == null)) {

                    for(int iii = 0; iii < decorations.getStringList(ProcessItem[i] + ".commandsOnClick").size(); iii++) {

                        playerCommands.add(decorations.getStringList(ProcessItem[i] + ".commandsOnClick").get(iii));

                    }

                }

                toAdd.setItemMeta(im);

                GuiItem guiItem = new GuiItem(toAdd, event -> {
                    event.setCancelled(true);

                    for (String consoleCommand : consoleCommands) {

                        this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), consoleCommand.replace("%PLAYER%", event.getWhoClicked().getName()));

                    }

                    for (String playerCommand : playerCommands) {

                        this.plugin.getServer().dispatchCommand(event.getWhoClicked(), playerCommand.replace("%PLAYER%", event.getWhoClicked().getName()));

                    }

                });

                gui.setItem(decorations.getInt(ProcessItem[i] + ".slot"), guiItem);

                ignoredSlots.add(decorations.getInt(ProcessItem[i] + ".slot"));

            }
        }

        gui.setCloseGuiAction(event -> {

            final Map<ItemStack, Map<Short, Integer>> soldMap2 = new HashMap<>();

            Map<EconomyType, Double> moneyMap = new EnumMap<>(EconomyType.class);

            double totalPrice = 0;
            int itemAmount = 0;

            Inventory items = event.getInventory();
            final Boolean[] ExcessItems = {false};

            boolean itemsPlacedInGui = false;

            Inventory inventory = event.getInventory();
            for (int a = 0; a < inventory.getSize(); a++) {
                ItemStack i = inventory.getItem(a);

                if (i == null) continue;

                if(ignoredSlots.contains(a)) {

                    continue;

                }

                itemsPlacedInGui = true;

                if (ShopHandler.getItemSellPrice(i, player) > 0) {

                    itemAmount += i.getAmount();

                    @Deprecated
                    short materialDamage = i.getDurability();
                    int amount = i.getAmount();

                    double itemSellPrice = ShopHandler.getItemSellPrice(i, player);

                    totalPrice = totalPrice + itemSellPrice;

                    EconomyType itemEconomyType = ShopHandler.getEconomyType(i, (Player) event.getPlayer());

                    ItemStack SingleItemStack = new ItemStack(i);
                    SingleItemStack.setAmount(1);

                    Map<Short, Integer> totalSold = soldMap2.getOrDefault(SingleItemStack, new HashMap<>());
                    int totalSoldCount = totalSold.getOrDefault(materialDamage, 0);
                    int amountSold = (totalSoldCount + amount);

                    totalSold.put(materialDamage, amountSold);
                    soldMap2.put(SingleItemStack, totalSold);

                    double totalSold2 = moneyMap.getOrDefault(itemEconomyType, 0.0);
                    double amountSold2 = (totalSold2 + itemSellPrice);
                    moneyMap.put(itemEconomyType, amountSold2);


                }
                else {

                    final Map<Integer, ItemStack> fallenItems = event.getPlayer().getInventory().addItem(i);
                    fallenItems.values().forEach(item -> {
                        event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation().add(0, 0.5, 0), item);
                        ExcessItems[0] = true;
                    });

                }

            }

            if(ExcessItems[0]) {

                String ExcessItemsMsg = ChatColor.translateAlternateColorCodes('&', this.plugin.configFile.getString("messages.inventory_full"));
                player.sendMessage(ExcessItemsMsg);

            }

            if (totalPrice > 0) {

                PlayerHandler.playSound((Player) event.getPlayer(), "success");

                StringBuilder formattedPricing = new StringBuilder();

                for(Map.Entry<EconomyType, Double> entry : moneyMap.entrySet()) {
                    EconomyProvider economyProvider =
                            ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(entry.getKey());
                    economyProvider
                            .deposit(player, entry.getValue());



                    formattedPricing.append(economyProvider.getCurrencyPrefix()).append(ShopHandler.getFormattedPrice(entry.getValue(), entry.getKey())).append(economyProvider.getCurrencySuffix()).append(", ");

                }

                if (formattedPricing.toString().endsWith(", ")) {
                    formattedPricing = new StringBuilder(formattedPricing.substring(0, formattedPricing.length() - 2));
                }

                StringBuilder receiptList = new StringBuilder();
                StringBuilder itemList = new StringBuilder();


                if(this.plugin.configFile.getInt("options.receipt_type") == 1 || this.plugin.configFile.getString("messages.items_sold").contains("{list}")) {

                    for(Map.Entry<ItemStack, Map<Short, Integer>> entry : soldMap2.entrySet()) {
                        for(Map.Entry<Short, Integer> damageEntry : entry.getValue().entrySet()) {
                            @Deprecated
                            ItemStack materialItemStack = entry.getKey();

                            double profits = ShopHandler.getItemSellPrice(materialItemStack, player) * damageEntry.getValue();
                            String profitsFormatted = ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(ShopHandler.getEconomyType(materialItemStack, player)).getCurrencyPrefix() + ShopHandler.getFormattedPrice(profits, ShopHandler.getEconomyType(materialItemStack, player)) + ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(ShopHandler.getEconomyType(materialItemStack, player)).getCurrencySuffix();

                            String itemNameFormatted = WordUtils.capitalize(materialItemStack.getType().name().replace("AETHER_LEGACY_", "").replace("LOST_AETHER_", "").replace("_", " ").toLowerCase());

                            if(!(materialItemStack.getItemMeta().getDisplayName() == null)) {
                                if (!materialItemStack.getItemMeta().getDisplayName().equals("")) {
                                    itemNameFormatted = materialItemStack.getItemMeta().getDisplayName();
                                }
                            }

                            if(serverVersion <= 12 && !this.plugin.configFile.getBoolean("options.show_item_damage")) itemNameFormatted += ":" + damageEntry.getKey();

                            receiptList.append("\n").append(this.plugin.configFile.getString(
                                    "messages.receipt_item_layout").replace("{amount}",
                                    String.valueOf(damageEntry.getValue())).replace(
                                    "{item}", itemNameFormatted).replace("{price}", profitsFormatted));

                            itemList.append(itemNameFormatted).append(", ");

                        }
                    }

                }

                if(this.plugin.configFile.getInt("options.receipt_type") == 1) {



                    String msg = ChatColor.translateAlternateColorCodes('&', this.plugin.configFile.getString("messages.items_sold").replace("{earning}", formattedPricing).replace("{receipt}", "").replace("{list}", itemList.substring(0, itemList.length()-2)).replace("{amount}", String.valueOf(itemAmount)));
                    String receiptName = ChatColor.translateAlternateColorCodes('&', this.plugin.configFile.getString("messages.receipt_text"));

                    String receipt = ChatColor.translateAlternateColorCodes('&', this.plugin.configFile.getString("messages.receipt_title") + receiptList);
                    TextComponent test = new TextComponent(" " + receiptName);
                    test.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(receipt).create()));

                    TextComponent test2 = new TextComponent(msg);

                    player.spigot().sendMessage(test2, test);

                }

                else {

                    String msg = ChatColor.translateAlternateColorCodes('&', this.plugin.configFile.getString("messages.items_sold")
                            .replace("{earning}", formattedPricing)
                            .replace("{receipt}", "")
                            .replace("{list}", itemList.substring(0, itemList.length()-2))
                            .replace("{amount}", String.valueOf(itemAmount)));

                    player.sendMessage(msg);

                }

                if(this.plugin.configFile.getBoolean("options.sell_titles")) {


                    @Nullable String sellTitle = ChatColor.translateAlternateColorCodes('&', this.plugin.configFile.getString("messages.sell_title").replace("{earning}", formattedPricing).replace("{amount}", String.valueOf(itemAmount)));
                    @Nullable String sellSubtitle = ChatColor.translateAlternateColorCodes('&', this.plugin.configFile.getString("messages.sell_subtitle").replace("{earning}", formattedPricing).replace("{amount}", String.valueOf(itemAmount)));

                    player.sendTitle(sellTitle, sellSubtitle);

                }

                if(this.plugin.configFile.getBoolean("options.action_bar_msgs")) {
                    if(serverVersion >= 9) {
                        String actionBarMessage = ChatColor.translateAlternateColorCodes('&', this.plugin.configFile.getString("messages.action_bar_items_sold").replace("{earning}", formattedPricing).replace("{amount}", String.valueOf(itemAmount)));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
                    }

                }

            } else {
                PlayerHandler.playSound(player, "failed");
                sendMessage(player, itemsPlacedInGui ? "no_items_sold" : "no_items_in_gui");
            }
        });

        gui.open(player);
        return true;
    }

    private void sendMessage(CommandSender sender, String path) {
        String message = this.plugin.configFile.getString("messages." + path);
        if(message == null || message.isEmpty()) return;

        String colored = ChatColor.translateAlternateColorCodes('&', message);
        sender.sendMessage(colored);
    }

    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        long usedMemory = (totalMemory - freeMemory);
        long usedMemoryMB = (usedMemory / 1_048_576L);
        long maxMemoryMB = (maxMemory / 1_048_576L);

        return String.format(Locale.US, "%s / %s MiB", usedMemoryMB, maxMemoryMB);
    }
}
