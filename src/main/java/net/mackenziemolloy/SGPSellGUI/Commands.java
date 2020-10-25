/*

    TO-DO LIST

    - Integrate receipt config option
    - Remove pricing variable


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
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Commands implements CommandExecutor {

    private final Main main;

    @SuppressWarnings("ConstantConditions")
    public Commands(final Main main) {
        this.main = main;

        main.getCommand("sellgui").setExecutor(this);
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

    @Override @SuppressWarnings("ConstantConditions")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length == 0) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if (sender.hasPermission("sellgui.use")) {
                    Gui gui = new Gui(4, "Sell GUI");

                    gui.setCloseGuiAction(event -> {
                        Map<Material, Map<Short, Integer>> soldMap = new EnumMap<>(Material.class);
                        Map<EconomyType, Double> moneyMap = new EnumMap<>(EconomyType.class);

                        double totalPrice = 0;

                        Inventory items = event.getInventory();


                        for (ItemStack i : items) {

                            if (i == null) continue;

                            if (ShopGuiPlusApi.getItemStackPriceSell(player, i) > 0) {

                                Material material = i.getType();
                                @Deprecated
                                short materialDamage = i.getDurability();
                                int amount = i.getAmount();

                                double itemSellPrice = ShopGuiPlusApi.getItemStackPriceSell(player, i) * amount;

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

                            StringBuilder output = new StringBuilder();


                            if(main.configHandler.getConfigC().getInt("options.receipt_type") == 1 || main.configHandler.getConfigC().getString("messages.items_sold").contains("{list}")) {

                                player.sendMessage(moneyMap.keySet().toString());

                                for(Map.Entry<Material, Map<Short, Integer>> entry : soldMap.entrySet()) {
                                    for(Map.Entry<Short, Integer> damageEntry : entry.getValue().entrySet()) {
                                        @Deprecated
                                        ItemStack materialItemStack = new ItemStack(entry.getKey(), 1,
                                                damageEntry.getKey());

                                        double profits = ShopGuiPlusApi.getItemStackPriceSell(player,
                                                materialItemStack) * damageEntry.getValue();
                                        String profitsFormatted = ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(getEconomyType(materialItemStack, player)).getCurrencyPrefix() + profits + ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(getEconomyType(materialItemStack, player)).getCurrencySuffix();

                                        String itemNameFormatted =
                                                WordUtils.capitalize(materialItemStack.getType().name().replace("_", " ").toLowerCase()) + ":" + damageEntry.getKey();

                                        output.append("\n").append(main.configHandler.getConfigC().getString(
                                                "messages.receipt_item_layout").replace("{amount}",
                                                String.valueOf(damageEntry.getValue())).replace(
                                                "{item}", itemNameFormatted).replace("{price}", profitsFormatted));

                                    }
                                }

                            }

                            if(main.configHandler.getConfigC().getInt("options.receipt_type") == 1) {

                                String msg = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.items_sold").replace("{earning}", pricing.toString()).replace("{receipt}", "").replace("{list}", output.toString()));
                                String receiptName = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.receipt_text"));

                                String receipt = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.receipt_title") + output);
                                TextComponent test = new TextComponent(" " + receiptName);
                                test.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new ComponentBuilder(receipt).create()));

                                TextComponent test2 = new TextComponent(msg);

                                player.spigot().sendMessage(test2, test);

                            }

                            else {

                                String msg = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.items_sold").replace("{earning}", pricing.toString()).replace("{receipt}", "").replace("{list}", output.toString()));
                                player.sendMessage(msg);

                            }


                        } else {

                            String msg = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.no_items_sold"));
                            player.sendMessage(msg);

                        }
                    });


                    gui.open(player);
                } else {

                    String NoPermission = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.no_permission"));
                    sender.sendMessage(NoPermission);

                }
            } else {

                System.out.println("Only players can execute this commands!");

            }
        }


        else if(args[0].toLowerCase().equals("reload") || args[0].toLowerCase().equals("rl")) {

            String configReloadedMsg = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.reloaded_config"));

            if(sender instanceof Player) {

                if (sender.hasPermission("sellgui.reload")) {

                    main.configHandler.reloadConfigC();
                    sender.sendMessage(configReloadedMsg);

                }

                else {

                    @SuppressWarnings("null")
                    String noPermission = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.no_permission"));
                    sender.sendMessage(noPermission);

                }

            }

            else {

                main.configHandler.reloadConfigC();
                main.getServer().getConsoleSender().sendMessage(configReloadedMsg);

            }
        }

        return false;
    }

}
