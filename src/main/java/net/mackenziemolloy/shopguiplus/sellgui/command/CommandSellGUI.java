package net.mackenziemolloy.shopguiplus.sellgui.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;

import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.mackenziemolloy.shopguiplus.sellgui.objects.ShopItemPriceValue;
import net.mackenziemolloy.shopguiplus.sellgui.utility.*;
import net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman.HexColorUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.economy.EconomyType;
import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.provider.economy.EconomyProvider;
import net.brcdev.shopgui.shop.item.ShopItem;
import net.brcdev.shopgui.shop.ShopManager.ShopAction;
import net.brcdev.shopgui.shop.ShopTransactionResult;
import net.brcdev.shopgui.shop.ShopTransactionResult.ShopTransactionResultType;
import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;
import net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman.MessageUtility;
import net.mackenziemolloy.shopguiplus.sellgui.utility.sirblobman.VersionUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public final class CommandSellGUI implements TabExecutor {
    private final SellGUI plugin;
    
    public CommandSellGUI(SellGUI plugin) {
        this.plugin = Objects.requireNonNull(plugin, "The plugin must not be null!");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1) {
            List<String> valueSet = Arrays.asList("rl", "reload", "debug", "dump");
            return StringUtil.copyPartialMatches(args[0], valueSet, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!plugin.compatible) {
            String message = MessageUtility.color("&7\n&7\n&a&lUPDATE REQUIRED \n&7\n&7Unfortunately &fSellGUI &7will not work until you update &cShopGUIPlus&7 to version &c1.78.0&7 or above.\n&7\n&eDownload: https://spigotmc.org/resources/6515/\n&7\n&7");
            sender.sendMessage(message);
            return false;
        }

        if (args.length == 0) {
            return commandBase(sender);
        }

        String sub = args[0].toLowerCase(Locale.US);
        switch (sub) {
            case "rl", "reload" -> {
                return commandReload(sender);
            }
            case "debug", "dump" -> {
                return commandDebug(sender);
            }
            default -> {
            }
        }

        return false;
    }

    public void register() {
        PluginCommand pluginCommand = this.plugin.getCommand("sellgui");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
    }

    private boolean commandReload(CommandSender sender) {
        if (!sender.hasPermission("sellgui.reload")) {
            sendMessage(sender, "no_permission");
            return true;
        }

        CompletableFuture.runAsync(this.plugin::generateFiles).whenComplete((success, error) -> {
            if (error != null) {
                sender.sendMessage(ChatColor.RED + "An error occurred, please check the server console.");
                error.printStackTrace();
                return;
            }

            // This looks painful, I know.
            if (this.plugin.getConfiguration().get("options.transaction_log.enabled") != null && !this.plugin.getConfiguration().getBoolean("options.transaction_log.enabled")) this.plugin.closeLogger();
            else if (this.plugin.fileLogger == null) this.plugin.initLogger();

            sendMessage(sender, "reloaded_config");
            if (sender instanceof Player player) {
                PlayerHandler.playSound(player, "success");
            }
        });
        return true;
    }

    private boolean commandDebug(CommandSender sender) {
        if (sender instanceof Player
                && ((Player) sender).getUniqueId().toString().equals("6b23291c-495b-478d-9055-d0d151206bff")) {
            String pluginVersion = this.plugin.getDescription().getVersion();
            sender.sendMessage(String.format(Locale.US, "This server is running SellGUI made by " +
                    "Mackenzie Molloy#1821 v%s", pluginVersion));
        }

        if (!sender.hasPermission("sellgui.dump")) {
            sendMessage(sender, "no_permission");
            return true;
        }

        CommentedConfiguration configuration = this.plugin.getConfiguration();
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin[] pluginArray = pluginManager.getPlugins();
        List<String> pluginInfoList = new ArrayList<>();

        for (Plugin plugin : pluginArray) {
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
                "\n\n| Plugin Configuration\n\n" + configuration.saveToString();
        try {
            String pasteUrl = new Hastebin().post(pasteRaw, true);
            String pastedDumpMsg = ChatColor.translateAlternateColorCodes('&',
                    "&c[ShopGUIPlus-SellGUI] Successfully dumped server information here: %s.");

            String message = String.format(Locale.US, pastedDumpMsg, pasteUrl);
            Bukkit.getConsoleSender().sendMessage(message);
            sender.sendMessage(message);

            if (sender instanceof Player player) {
                PlayerHandler.playSound(player, "success");
            }
        } catch (IOException ex) {
            sender.sendMessage(ChatColor.RED + "An error occurred, please check the console:");
            ex.printStackTrace();
        }

        return true;
    }

    private boolean commandBase(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        if (!player.hasPermission("sellgui.use")) {
            sendMessage(player, "no_permission");
            return true;
        }

        if (!checkGameMode(player)) {
            return true;
        }

        CommentedConfiguration configuration = this.plugin.getConfiguration();
        int guiSize = configuration.getInt("options.rows");
        if (guiSize > 6 || guiSize < 1) {
            guiSize = 6;
        }

        String sellGuiTitle = getMessage("sellgui_title", null);
        Gui gui = new Gui(guiSize, sellGuiTitle, Collections.emptySet());
        PlayerHandler.playSound(player, "open");

        Set<Integer> ignoredSlotSet = new HashSet<>();
        setDecorationItems(configuration, gui, ignoredSlotSet);
        gui.setCloseGuiAction(event -> this.plugin.getScheduler().runTaskAtEntity(player, () -> onGuiClose(player, event, ignoredSlotSet)));

        gui.open(player);
        return true;
    }

    private boolean checkGameMode(Player player) {
        ShopGuiPlugin shopGui = ShopGuiPlusApi.getPlugin();
        FileConfiguration configuration = shopGui.getConfigMain().getConfig();
        List<String> disabledGameModeList = configuration.getStringList("disableShopsInGamemodes");

        GameMode gameMode = player.getGameMode();
        String gameModeName = gameMode.name();
        if (disabledGameModeList.contains(gameModeName)) {
            String gameModeFormatted = StringFormatter.capitalize(gameModeName);
            sendMessage(player, "gamemode_not_allowed", message ->
                    message.replace("{gamemode}", gameModeFormatted));
            return false;
        }

        return true;
    }

    private void setDecorationItems(ConfigurationSection configuration, Gui gui, Set<Integer> ignoredSlotSet) {
        ConfigurationSection sectionDecorations = configuration.getConfigurationSection("options.decorations");
        if (sectionDecorations != null) {
            Set<String> sectionDecorationsKeys = sectionDecorations.getKeys(false);
            for (String key : sectionDecorationsKeys) {
                ConfigurationSection section = sectionDecorations.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }

                Material material;
                String materialName = section.getString("item.material");
                if (materialName == null || (material = Material.matchMaterial(materialName)) == null
                        || !section.isInt("slot") || section.getInt("slot") > ((gui.getRows() * 9) - 1)
                        || section.getInt("slot") < 0) {
                    Logger logger = this.plugin.getLogger();
                    logger.warning("Failed to load decoration item with id '" + key + "'.");
                    continue;
                }

                ItemStack item = new ItemStack(material);
                int damage = section.getInt("item.damage", 0);
                if (damage != 0) {
                    setItemDamage(item, damage);
                }

                int quantity = section.getInt("item.quantity", 1);
                item.setAmount(quantity);

                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta != null) {
                    String displayName = section.getString("item.name");
                    if (displayName != null) {
                        displayName = MessageUtility.color(displayName);
                        itemMeta.setDisplayName(displayName);
                    }

                    List<String> loreList = section.getStringList("item.lore");
                    if (!loreList.isEmpty()) {
                        loreList = MessageUtility.colorList(loreList);
                        itemMeta.setLore(loreList);
                    }

                    int customModelData = section.getInt("item.customModelData");
                    if (customModelData != 0) {
                        itemMeta.setCustomModelData(customModelData);
                    }

                    item.setItemMeta(itemMeta);
                }

                List<String> consoleCommandList = section.getStringList("commandsOnClickConsole");
                List<String> playerCommandList = section.getStringList("commandsOnClick");

                GuiItem guiItem = new GuiItem(item, e -> {
                    e.setCancelled(true);
                    HumanEntity human = e.getWhoClicked();
                    String humanName = human.getName();

                    CommandSender console = Bukkit.getConsoleSender();
                    for (String consoleCommand : consoleCommandList) {
                        String command = consoleCommand.replace("%PLAYER%", humanName);
                        this.plugin.getScheduler().runTask(() -> Bukkit.dispatchCommand(console, command));
                    }

                    for (String playerCommand : playerCommandList) {
                        String command = playerCommand.replace("%PLAYER%", humanName);
                        this.plugin.getScheduler().runTaskAtEntity(human, () -> Bukkit.dispatchCommand(human, command));
                    }

                    if (section.getBoolean("item.sellinventory")) {
                        human.closeInventory();
                        commandBase(Bukkit.getPlayer(humanName));
                    }
                });
            
                int slot = section.getInt("slot");
                gui.setItem(slot, guiItem);

                ignoredSlotSet.add(slot);
            }
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

    private void setItemDamage(ItemStack item, int damage) {
        int minorVersion = VersionUtility.getMinorVersion();
        if (minorVersion < 13) {
            short durability = (short) damage;
            item.setDurability(durability);
            return;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof Damageable) {
            ((Damageable) itemMeta).setDamage(damage);
            item.setItemMeta(itemMeta);
        }
    }

    private void onGuiClose(Player player, InventoryCloseEvent event, Set<Integer> ignoredSlotSet) {
        int minorVersion = VersionUtility.getMinorVersion();
        CommentedConfiguration configuration = this.plugin.getConfiguration();

        // ItemStack is a stack size of 0, Integer is Price
        Map<ItemStack, ShopItemPriceValue> itemStackSellPriceCache = new HashMap<>();

        Map<ItemStack, Map<Short, Integer>> soldMap2 = new HashMap<>();
        Map<EconomyType, Double> moneyMap = new EnumMap<>(EconomyType.class);

        double totalPrice = 0;
        int itemAmount = 0;
        boolean[] excessItems = {false};
        boolean itemsPlacedInGui = false;

        Inventory inventory = event.getInventory();
        for (int a = 0; a < inventory.getSize(); a++) {
            ItemStack i = inventory.getItem(a);

            if (i == null) {
                continue;
            }

            if (ignoredSlotSet.contains(a)) {
                continue;
            }

            ItemStack singleItem = new ItemStack(i);
            singleItem.setAmount(1);

            itemsPlacedInGui = true;

            if (itemStackSellPriceCache.getOrDefault(singleItem, new ShopItemPriceValue(null, 0.0)).getSellPrice() > 0 || ShopHandler.getItemSellPrice(i, player) > 0) {
                itemAmount += i.getAmount();

                @Deprecated
                short materialDamage = i.getDurability();
                int amount = i.getAmount();

                double itemSellPrice = itemStackSellPriceCache.containsKey(singleItem) ? ( itemStackSellPriceCache.get(singleItem).getSellPrice() * amount ) : ShopHandler.getItemSellPrice(i, player);

                try {
                    ShopPreTransactionEvent shopPreTransactionEvent = (ShopPreTransactionEvent) Class.forName("net.brcdev.shopgui.event.ShopPreTransactionEvent")
                        .getDeclaredConstructor(ShopAction.class, ShopItem.class, Player.class, int.class, double.class)
                        .newInstance(ShopAction.SELL, ShopGuiPlusApi.getItemStackShopItem(i), player, amount, itemSellPrice);

                        Bukkit.getPluginManager().callEvent(shopPreTransactionEvent);
                        itemSellPrice = shopPreTransactionEvent.getPrice();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                totalPrice += itemSellPrice;

                EconomyType itemEconomyType = ShopHandler.getEconomyType(i);

                ItemStack SingleItemStack = new ItemStack(i);
                SingleItemStack.setAmount(1);

                itemStackSellPriceCache.putIfAbsent(SingleItemStack, new ShopItemPriceValue(itemEconomyType, itemSellPrice/amount));

                Map<Short, Integer> totalSold = soldMap2.getOrDefault(SingleItemStack, new HashMap<>());
                int totalSoldCount = totalSold.getOrDefault(materialDamage, 0);
                int amountSold = (totalSoldCount + amount);

                totalSold.put(materialDamage, amountSold);
                soldMap2.put(SingleItemStack, totalSold);

                double totalSold2 = moneyMap.getOrDefault(itemEconomyType, 0.0);
                double amountSold2 = (totalSold2 + itemSellPrice);
                moneyMap.put(itemEconomyType, amountSold2);

                // Item considered sold at this point
                // Requires reflection to instantiate constructors at runtime not contained in the api
                try {
                    ShopTransactionResult shopTransactionResult =
                            (ShopTransactionResult) Class.forName("net.brcdev.shopgui.shop.ShopTransactionResult")
                                    .getDeclaredConstructor(ShopAction.class, ShopTransactionResultType.class, ShopItem.class, Player.class, int.class, double.class)
                                    .newInstance(ShopAction.SELL, ShopTransactionResultType.SUCCESS, ShopGuiPlusApi.getItemStackShopItem(i), player, amount, itemSellPrice);

                    ShopPostTransactionEvent shopPostTransactionEvent =
                            (ShopPostTransactionEvent) Class.forName("net.brcdev.shopgui.event.ShopPostTransactionEvent")
                                    .getDeclaredConstructor(ShopTransactionResult.class).newInstance(shopTransactionResult);

                    Runnable task = () -> Bukkit.getPluginManager().callEvent(shopPostTransactionEvent);
                    this.plugin.getScheduler().runTask(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Location location = player.getLocation().add(0.0D, 0.5D, 0.0D);
                Map<Integer, ItemStack> fallenItems = event.getPlayer().getInventory().addItem(i);
                Runnable task = () -> {
                    World world = player.getWorld();
                    fallenItems.values().forEach(item -> {
                        world.dropItemNaturally(location, item);
                        excessItems[0] = true;
                    });
                };
                this.plugin.getScheduler().runTaskAtLocation(location, task);
            }
        }

        if (excessItems[0]) {
            sendMessage(player, "inventory_full");
        }

        if (totalPrice > 0) {
            PlayerHandler.playSound((Player) event.getPlayer(), "success");
            StringBuilder formattedPricing = new StringBuilder();
            for (Entry<EconomyType, Double> entry : moneyMap.entrySet()) {
                EconomyProvider economyProvider = ShopGuiPlusApi.getPlugin().getEconomyManager()
                        .getEconomyProvider(entry.getKey());
                economyProvider.deposit(player, entry.getValue());
                formattedPricing.append(economyProvider.getCurrencyPrefix()).append(StringFormatter
                                .getFormattedNumber(entry.getValue())).append(economyProvider.getCurrencySuffix())
                        .append(", ");
            }

            if (formattedPricing.toString().endsWith(", ")) {
                formattedPricing = new StringBuilder(formattedPricing.substring(0,
                        formattedPricing.length() - 2));
            }

            List<String> receiptList = new LinkedList<>();
            List<String> itemList = new LinkedList<>();

            if (configuration.getInt("options.receipt_type") == 1
                    || Objects.requireNonNull(configuration.getString("messages.items_sold")).contains("{list}")) {
                for (Entry<ItemStack, Map<Short, Integer>> entry : soldMap2.entrySet()) {
                    for (Entry<Short, Integer> damageEntry : entry.getValue().entrySet()) {
                        @Deprecated
                        ItemStack materialItemStack = entry.getKey();

                        double profits = ShopHandler.getItemSellPrice(materialItemStack, player)
                                * damageEntry.getValue();
                        String profitsFormatted = ShopGuiPlusApi.getPlugin().getEconomyManager()
                                .getEconomyProvider(ShopHandler.getEconomyType(materialItemStack))
                                .getCurrencyPrefix() + StringFormatter.getFormattedNumber(profits)
                                + ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(
                                        ShopHandler.getEconomyType(materialItemStack))
                                .getCurrencySuffix();
                    
                        String itemNameFormatted = StringFormatter.capitalize(materialItemStack.getType()
                                .name().replace("AETHER_LEGACY_", "")
                                .replace("LOST_AETHER_", "")
                                .replace("_", " ").toLowerCase());

                        ItemMeta itemMeta = materialItemStack.getItemMeta();
                        if (itemMeta != null && itemMeta.hasDisplayName()) {
                            String displayName = itemMeta.getDisplayName();
                            if (!displayName.isEmpty()) {
                                itemNameFormatted = materialItemStack.getItemMeta().getDisplayName();
                            }
                        }

                        if (minorVersion <= 12 && !configuration.getBoolean("options.show_item_damage")) {
                            itemNameFormatted += (":" + damageEntry.getKey());
                        }

                        String finalItemNameFormatted = itemNameFormatted;
                        String itemLine = getMessage("receipt_item_layout", message -> message
                                .replace("{amount}", String.valueOf(damageEntry.getValue()))
                                .replace("{item}", finalItemNameFormatted)
                                .replace("{price}", profitsFormatted));

                        receiptList.add(itemLine);
                        itemList.add(itemNameFormatted);
                    }
                }
            }

            String itemAmountFormatted = StringFormatter.getFormattedNumber((double) itemAmount);
            if (configuration.getInt("options.receipt_type") == 1) {
                int finalItemAmount = itemAmount;
                StringBuilder finalFormattedPricing1 = formattedPricing;

                TextComponent itemsSoldComponent = getTextComponentMessage("items_sold", message -> message
                        .replace("{earning}", finalFormattedPricing1)
                        .replace("{receipt}", "")
                        .replace("{list}", String.join(", ", itemList))
                        .replace("{amount}", String.valueOf(finalItemAmount)));
                itemsSoldComponent.addExtra(" ");

                String receiptHoverMessage = (getMessage("receipt_title", null) + ChatColor.RESET + String.join("\n", receiptList) + ChatColor.RESET);

                TextComponent receiptNameComponent = getTextComponentMessage("receipt_text", null);
                BaseComponent[] hoverEventComponents = {
                        new TextComponent(receiptHoverMessage)
                };

                HoverEvent hoverEvent = new HoverEvent(Action.SHOW_TEXT, hoverEventComponents);
                receiptNameComponent.setHoverEvent(hoverEvent);

                sendMessage(player, Arrays.asList(itemsSoldComponent, receiptNameComponent));
            } else {
                StringBuilder finalFormattedPricing = formattedPricing;
                sendMessage(player, "items_sold", message -> message.replace("{earning}",
                                finalFormattedPricing)
                        .replace("{receipt}", "")
                        .replace("{list}", String.join(", ", itemList))
                        .replace("{amount}", itemAmountFormatted));
            }

            /* Subject to deprecation */
            if (plugin.fileLogger != null) {
                plugin.fileLogger.info(player.getName() + " (" + player.getUniqueId() + ") sold: {" + HexColorUtility.purgeAllColor(String.join(", ", receiptList)) + "}");
            }

            if (configuration.getBoolean("options.sell_titles")) {
                sendSellTitles(player, formattedPricing, itemAmountFormatted);
            }

            if (configuration.getBoolean("options.action_bar_msgs") && minorVersion >= 9) {
                sendActionBar(player, formattedPricing, itemAmountFormatted);
            }
        } else {
            PlayerHandler.playSound(player, "failed");
            sendMessage(player, itemsPlacedInGui ? "no_items_sold" : "no_items_in_gui");
        }
    }

    private String getMessage(String path, @Nullable Function<String, String> replacer) {
        CommentedConfiguration configuration = this.plugin.getConfiguration();
        String message = configuration.getString("messages." + path);
        if (message == null || message.isEmpty()) {
            return "";
        }

        if (replacer != null) {
            message = replacer.apply(message);
        }

        return HexColorUtility.replaceHexColors('&', MessageUtility.color(message));
    }

    private TextComponent getTextComponentMessage(String path, @Nullable Function<String, String> replacer) {
        String message = getMessage(path, replacer);
        if (message.isEmpty()) {
            return new TextComponent("");
        } else {
            return new TextComponent(message);
        }
    }

    private void sendMessage(CommandSender sender, String path) {
        sendMessage(sender, path, null);
    }

    private void sendMessage(CommandSender sender, String path, @Nullable Function<String, String> replacer) {
        String message = getMessage(path, replacer);
        if (message.isEmpty()) {
            return;
        }

        if (sender instanceof Player player) {
            TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(message));
            player.spigot().sendMessage(textComponent);
        } else {
            sender.sendMessage(message);
        }
    }

    private void sendMessage(Player player, List<TextComponent> textComponents) {
        boolean isTextPresent = textComponents.stream()
                .anyMatch(component -> !component.getText().isEmpty());

        if (!isTextPresent) {
            return;
        }

        player.spigot().sendMessage(textComponents.toArray(new BaseComponent[0]));
    }

    private void sendSellTitles(Player player, CharSequence price, String amount) {
        Function<String, String> replacer = message -> message.replace("{earning}", price)
                .replace("{amount}", amount);

        String title = getMessage("sell_title", replacer);
        String subtitle = getMessage("sell_subtitle", replacer);
        player.sendTitle(title, subtitle);
    }

    private void sendActionBar(Player player, CharSequence price, String amount) {
        Function<String, String> replacer = message -> message.replace("{earning}", price)
                .replace("{amount}", amount);
        
        TextComponent message = getTextComponentMessage("action_bar_items_sold", replacer);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
    }
}
