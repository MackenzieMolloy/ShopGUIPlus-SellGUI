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
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.StringUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.economy.EconomyType;
import net.brcdev.shopgui.provider.economy.EconomyProvider;
import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;
import net.mackenziemolloy.shopguiplus.sellgui.utility.Hastebin;
import net.mackenziemolloy.shopguiplus.sellgui.utility.PlayerHandler;
import net.mackenziemolloy.shopguiplus.sellgui.utility.ShopHandler;
import net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman.MessageUtility;
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
        if(args.length == 0) {
            return commandBase(sender);
        }

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
        if(sender instanceof Player
                && ((Player) sender).getUniqueId().toString().equals("6b23291c-495b-478d-9055-d0d151206bff")) {
            String pluginVersion = this.plugin.getDescription().getVersion();
            sender.sendMessage(String.format(Locale.US, "This server is running SellGUI made by " +
                    "Mackenzie Molloy#1821 v%s", pluginVersion));
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

            String pluginInfo = String.format(Locale.US, "- %s v%s by %s", pluginName, pluginVersion,
                    pluginAuthorList);
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
                "\n\n| Plugin Configuration\n\n" + this.plugin.configuration.saveToString();
        try {
            String pasteUrl = new Hastebin().post(pasteRaw, true);
            String pastedDumpMsg = ChatColor.translateAlternateColorCodes('&',
                    "&c[ShopGUIPlus-SellGUI] Successfully dumped server information here: %s.");

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
        List<String> invalidGameModeList = shopGuiPlugin.getConfigMain().getConfig()
                .getStringList("disableShopsInGamemodes");
        if(invalidGameModeList.contains(player.getGameMode().name())
                && !player.hasPermission("shopguiplus.bypassgamemode")) {
            String gamemodeFormatted = WordUtils.capitalize(player.getGameMode().toString().toLowerCase());
            String gamemodeNotAllowed = ChatColor.translateAlternateColorCodes('&',
                    this.plugin.configuration.getString("messages.gamemode_not_allowed")
                            .replace("{gamemode}", gamemodeFormatted));
            player.sendMessage(gamemodeNotAllowed);
            return true;
        }

        int serverVersion = Integer.parseInt(Bukkit.getVersion().split("MC: ")[1].subSequence(0,
                Bukkit.getVersion().split("MC: ")[1].length()-1).toString().split("\\.")[1]);
        String sellGUITitle = ChatColor.translateAlternateColorCodes('&',
                this.plugin.configuration.getString("messages.sellgui_title"));
        int guiSize = this.plugin.configuration.getInt("options.rows");
        if(guiSize > 6 || guiSize < 1) {
            guiSize = 6;
        }


        Gui gui = new Gui(guiSize, sellGUITitle);
        //Gui gui = Gui.gui().title(Component.text(sellGUITitle)).rows(guiSize).create();
        PlayerHandler.playSound(player, "open");

        List<Integer> ignoredSlotList = new ArrayList<>();

        ConfigurationSection sectionOptionsDecorations = this.plugin.configuration.getConfigurationSection(
                "options.decorations");
        if(!(sectionOptionsDecorations == null) && sectionOptionsDecorations.getKeys(false).size() > 0) {
            for (int i = 0; i < sectionOptionsDecorations.getKeys(false).size(); i++) {
                Object[] processItem = sectionOptionsDecorations.getKeys(false).toArray();

                String materialName = sectionOptionsDecorations.getString(processItem[i] + ".item.material");
                Material material;
                if (materialName == null || (material = Material.matchMaterial(materialName)) == null ||
                        sectionOptionsDecorations.getString(processItem[i] + ".slot") == null ||
                        sectionOptionsDecorations.getInt(processItem[i] + ".slot") > (guiSize * 9) - 1 ||
                        sectionOptionsDecorations.getInt(processItem[i] + ".slot") < 0) {
                    this.plugin.getLogger().info("Error loading decoration item identified as "
                            + processItem[i]);
                    continue;

                }

                ItemStack toAdd = new ItemStack(material);

                int durability = sectionOptionsDecorations.getInt(processItem[i] + ".item.damage", 0);
                if(durability != 0) {
                    setDurability(toAdd, serverVersion, durability);
                }

                int quantity = sectionOptionsDecorations.getInt(processItem[i] + ".item.quantity", 1);
                toAdd.setAmount(quantity);

                ItemMeta toAddMeta = toAdd.getItemMeta();
                if(toAddMeta != null) {
                    String displayName = sectionOptionsDecorations.getString(processItem[i] + ".item.name");
                    if (displayName != null) {
                        String ItemName = ChatColor.translateAlternateColorCodes('&', displayName);
                        toAddMeta.setDisplayName(ItemName);
                    }

                    List<String> loreList = sectionOptionsDecorations.getStringList(processItem[i] +
                            ".item.lore");
                    if(!loreList.isEmpty()) {
                        List<String> loreToSet = new ArrayList<>();
                        for(int ii = 0; ii < sectionOptionsDecorations.getStringList(processItem[i] +
                                ".item.lore").size(); ii++) {
                            List<String> loreLines = sectionOptionsDecorations.getStringList(
                                    processItem[i] + ".item.lore");
                            String loreLineToAdd = ChatColor.translateAlternateColorCodes('&',
                                    loreLines.get(ii));
                            loreToSet.add(loreLineToAdd);

                        }

                        toAddMeta.setLore(loreToSet);
                    }
                }

                List<String> consoleCommandList = sectionOptionsDecorations.getStringList(
                        processItem[i] + ".commandsOnClickConsole");
                List<String> playerCommandList = sectionOptionsDecorations.getStringList(
                        processItem[i] + ".commandsOnClick");
                toAdd.setItemMeta(toAddMeta);

                GuiItem guiItem = new GuiItem(toAdd, event -> {
                    event.setCancelled(true);
                    HumanEntity human = event.getWhoClicked();
                    CommandSender console = Bukkit.getConsoleSender();
                    String humanName = human.getName();

                    for (String consoleCommand : consoleCommandList) {
                        Bukkit.dispatchCommand(console, consoleCommand.replace("%PLAYER%", humanName));
                    }

                    for (String playerCommand : playerCommandList) {
                        Bukkit.dispatchCommand(human, playerCommand.replace("%PLAYER%", humanName));
                    }
                });

                gui.setItem(sectionOptionsDecorations.getInt(processItem[i] + ".slot"), guiItem);
                ignoredSlotList.add(sectionOptionsDecorations.getInt(processItem[i] + ".slot"));
            }
        }

        gui.setCloseGuiAction(event -> {
            Map<ItemStack, Map<Short, Integer>> soldMap2 = new HashMap<>();
            Map<EconomyType, Double> moneyMap = new EnumMap<>(EconomyType.class);

            double totalPrice = 0;
            int itemAmount = 0;
            boolean[] excessItems = {false};
            boolean itemsPlacedInGui = false;

            Inventory inventory = event.getInventory();
            for (int a = 0; a < inventory.getSize(); a++) {
                ItemStack i = inventory.getItem(a);
                if (i == null) continue;

                if(ignoredSlotList.contains(a)) {
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


                } else {
                    Map<Integer, ItemStack> fallenItems = event.getPlayer().getInventory().addItem(i);
                    fallenItems.values().forEach(item -> {
                        event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation()
                                .add(0, 0.5, 0), item);
                        excessItems[0] = true;
                    });

                }

            }

            if(excessItems[0]) {
                sendMessage(player, "inventory_full");
            }

            if (totalPrice > 0) {
                PlayerHandler.playSound((Player) event.getPlayer(), "success");
                StringBuilder formattedPricing = new StringBuilder();
                for(Map.Entry<EconomyType, Double> entry : moneyMap.entrySet()) {
                    EconomyProvider economyProvider = ShopGuiPlusApi.getPlugin().getEconomyManager()
                            .getEconomyProvider(entry.getKey());
                    economyProvider.deposit(player, entry.getValue());
                    formattedPricing.append(economyProvider.getCurrencyPrefix()).append(ShopHandler
                            .getFormattedNumber(entry.getValue())).append(economyProvider.getCurrencySuffix())
                            .append(", ");

                }

                if (formattedPricing.toString().endsWith(", ")) {
                    formattedPricing = new StringBuilder(formattedPricing.substring(0,
                            formattedPricing.length() - 2));
                }

                StringBuilder receiptList = new StringBuilder();
                StringBuilder itemList = new StringBuilder();


                if(this.plugin.configuration.getInt("options.receipt_type") == 1
                        || this.plugin.configuration.getString("messages.items_sold").contains("{list}")) {
                    for(Map.Entry<ItemStack, Map<Short, Integer>> entry : soldMap2.entrySet()) {
                        for(Map.Entry<Short, Integer> damageEntry : entry.getValue().entrySet()) {
                            @Deprecated
                            ItemStack materialItemStack = entry.getKey();

                            double profits = ShopHandler.getItemSellPrice(materialItemStack, player)
                                    * damageEntry.getValue();
                            String profitsFormatted = ShopGuiPlusApi.getPlugin().getEconomyManager()
                                    .getEconomyProvider(ShopHandler.getEconomyType(materialItemStack, player))
                                    .getCurrencyPrefix() + ShopHandler.getFormattedNumber(profits)
                                    + ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(
                                            ShopHandler.getEconomyType(materialItemStack, player))
                                    .getCurrencySuffix();

                            String itemNameFormatted = WordUtils.capitalize(materialItemStack.getType()
                                    .name().replace("AETHER_LEGACY_", "")
                                    .replace("LOST_AETHER_", "")
                                    .replace("_", " ").toLowerCase());

                            if(!(materialItemStack.getItemMeta().getDisplayName() == null)) {
                                if (!materialItemStack.getItemMeta().getDisplayName().equals("")) {
                                    itemNameFormatted = materialItemStack.getItemMeta().getDisplayName();
                                }
                            }

                            if(serverVersion <= 12 && !this.plugin.configuration.getBoolean(
                                    "options.show_item_damage")) itemNameFormatted += ":"
                                    + damageEntry.getKey();
    
                            String finalItemNameFormatted = itemNameFormatted;
                            String itemLine = getMessage("receipt_item_layout", message -> message
                                    .replace("{amount}", String.valueOf(ShopHandler.getFormattedNumber(
                                            (double)damageEntry.getValue())))
                                    .replace("{item}", finalItemNameFormatted)
                                    .replace("{price}", profitsFormatted));

                            receiptList.append(itemLine);
                            itemList.append(itemNameFormatted).append(", ");

                        }
                    }
                }

                String itemAmountFormatted = ShopHandler.getFormattedNumber((double) itemAmount);

                if(this.plugin.configuration.getInt("options.receipt_type") == 1) {
                    int finalItemAmount = itemAmount;
                    StringBuilder finalFormattedPricing1 = formattedPricing;
                    
                    TextComponent itemsSoldComponent = getTextComponentMessage("items_sold",
                            message -> message
                            .replace("{earning}", finalFormattedPricing1)
                            .replace("{receipt}", "")
                            .replace("{list}", itemList.substring(0, itemList.length()-2))
                            .replace("{amount}", String.valueOf(finalItemAmount)));
                    itemsSoldComponent.addExtra(" ");
                    
                    String receiptHoverMessage = (getMessage("receipt_title", null) + receiptList);
                    TextComponent receiptNameComponent = getTextComponentMessage("receipt_text", null);
                    BaseComponent[] hoverEventComponents = TextComponent.fromLegacyText(receiptHoverMessage);
                    
                    HoverEvent hoverEvent = new HoverEvent(Action.SHOW_TEXT, hoverEventComponents);
                    receiptNameComponent.setHoverEvent(hoverEvent);
                    
                    player.spigot().sendMessage(itemsSoldComponent, receiptNameComponent);

                }

                else {
                    StringBuilder finalFormattedPricing = formattedPricing;
                    sendMessage(player, "items_sold", message -> message.replace("{earning}",
                                    finalFormattedPricing)
                            .replace("{receipt}", "")
                            .replace("{list}", itemList.substring(0, itemList.length()-2))
                            .replace("{amount}", itemAmountFormatted));
                }

                if(this.plugin.configuration.getBoolean("options.sell_titles")) {
                    @Nullable String sellTitle = ChatColor.translateAlternateColorCodes('&',
                            this.plugin.configuration.getString("messages.sell_title")
                                    .replace("{earning}", formattedPricing).replace("{amount}",
                                            itemAmountFormatted));
                    @Nullable String sellSubtitle = ChatColor.translateAlternateColorCodes('&',
                            this.plugin.configuration.getString("messages.sell_subtitle")
                                    .replace("{earning}", formattedPricing).replace("{amount}",
                                            itemAmountFormatted));
                    player.sendTitle(sellTitle, sellSubtitle);
                }

                if(this.plugin.configuration.getBoolean("options.action_bar_msgs")) {
                    if(serverVersion >= 9) {
                        String actionBarMessage = ChatColor.translateAlternateColorCodes('&',
                                this.plugin.configuration.getString("messages.action_bar_items_sold")
                                        .replace("{earning}", formattedPricing).replace("{amount}",
                                                itemAmountFormatted));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent(actionBarMessage));
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
    
    private String getMessage(String path, @Nullable Function<String, String> replacer) {
        String message = this.plugin.configuration.getString("messages." + path);
        if(message == null || message.isEmpty()) return "";
    
        if(replacer != null) {
            message = replacer.apply(message);
        }
    
        return MessageUtility.color(message);
    }
    
    private TextComponent getTextComponentMessage(String path, @Nullable Function<String, String> replacer) {
        String message = getMessage(path, replacer);
        if(message.isEmpty()) return new TextComponent("");
        else return new TextComponent(TextComponent.fromLegacyText(message));
    }

    private void sendMessage(CommandSender sender, String path) {
        sendMessage(sender, path, null);
    }
    
    private void sendMessage(CommandSender sender, String path, @Nullable Function<String, String> replacer) {
        String message = getMessage(path, replacer);
        if(message.isEmpty()) return;
        
        if(sender instanceof Player) {
            TextComponent textComponent = new TextComponent(message);
            ((Player) sender).spigot().sendMessage(textComponent);
        } else {
            sender.sendMessage(message);
        }
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

    @SuppressWarnings("deprecation")
    private void setDurability(ItemStack item, int version, int durability) {
        if(version < 13) {
            item.setDurability((short) durability);
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if(meta instanceof Damageable) {
            ((Damageable) meta).setDamage(durability);
            item.setItemMeta(meta);
        }
    }
}
