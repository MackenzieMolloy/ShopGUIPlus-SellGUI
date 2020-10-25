/*

    TO-DO LIST

    **MUST** Work on the (hopefully) minor re-write to support different item damages for 1.12 and below.

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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Commands implements CommandExecutor {

    private final Main main;

    public Commands(final Main main) {
        this.main = main;
        main.getCommand("sellgui").setExecutor(this);
    }

    public EconomyType getEconomyType(ItemStack material, Player player) {

        EconomyType itemEconomyType = ShopGuiPlusApi.getItemStackShop(material).getEconomyType();
        String defaultEconomyType = ShopGuiPlusApi.getPlugin().getEconomyManager().getDefaultEconomyProvider().getName().toUpperCase();

        if(itemEconomyType == null) {
            if (defaultEconomyType.equals("CUSTOM")) {
                itemEconomyType = EconomyType.CUSTOM;
            } else if (defaultEconomyType.equals("EXP")) {
                itemEconomyType = EconomyType.EXP;
            } else if (defaultEconomyType.equals("MYSQL_TOKENS")) {
                itemEconomyType = EconomyType.MYSQL_TOKENS;
            } else if (defaultEconomyType.equals("PLAYER_POINTS")) {
                itemEconomyType = EconomyType.PLAYER_POINTS;
            } else if (defaultEconomyType.equals("TOKEN_ENCHANT")) {
                itemEconomyType = EconomyType.TOKEN_ENCHANT;
            } else if (defaultEconomyType.equals("TOKEN_MANAGER")) {
                itemEconomyType = EconomyType.TOKEN_MANAGER;
            } else if (defaultEconomyType.equals("VAULT")) {
                itemEconomyType = EconomyType.VAULT;
            } else {
                player.getPlayer().sendMessage("§cOops... something went wrong when processing the economy type. Please contact a server administrator.");
            }
        }

       return itemEconomyType;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

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
                                Short materialDamage = i.getDurability();
                                int amount = i.getAmount();

                                double itemSellPrice = ShopGuiPlusApi.getItemStackPriceSell(player, i) * amount;

                                totalPrice = totalPrice + itemSellPrice;

                                EconomyType itemEconomyType = ShopGuiPlusApi.getItemStackShop(i).getEconomyType();

                                if (itemEconomyType == null) {
                                    String defaultEconomyType = ShopGuiPlusApi.getPlugin().getEconomyManager().getDefaultEconomyProvider().getName().toUpperCase();

                                    if (defaultEconomyType.equals("CUSTOM")) {
                                        itemEconomyType = EconomyType.CUSTOM;
                                    } else if (defaultEconomyType.equals("EXP")) {
                                        itemEconomyType = EconomyType.EXP;
                                    } else if (defaultEconomyType.equals("MYSQL_TOKENS")) {
                                        itemEconomyType = EconomyType.MYSQL_TOKENS;
                                    } else if (defaultEconomyType.equals("PLAYER_POINTS")) {
                                        itemEconomyType = EconomyType.PLAYER_POINTS;
                                    } else if (defaultEconomyType.equals("TOKEN_ENCHANT")) {
                                        itemEconomyType = EconomyType.TOKEN_ENCHANT;
                                    } else if (defaultEconomyType.equals("TOKEN_MANAGER")) {
                                        itemEconomyType = EconomyType.TOKEN_MANAGER;
                                    } else if (defaultEconomyType.equals("VAULT")) {
                                        itemEconomyType = EconomyType.VAULT;
                                    } else {
                                        event.getPlayer().sendMessage("§cOops... something went wrong when processing the economy type. Please contact a server administrator.");
                                    }

                                }

                                Map<Short, Integer> totalSold = soldMap.getOrDefault(material, new HashMap<Short, Integer>());
                                int totalSoldCount = totalSold.getOrDefault(materialDamage, 0);
                                int amountSold = (totalSoldCount + amount);

                                totalSold.put(materialDamage, amountSold);
                                soldMap.put(material, totalSold);
                                //int totalSold = soldMap.getOrDefault(material, 0);
                                //int amountSold = (totalSold + amount);
                                //soldMap.put(material, (materialDamage, amountSold);


                                double totalSold2 = moneyMap.getOrDefault(itemEconomyType, 0.0);
                                double amountSold2 = (totalSold2 + itemSellPrice);
                                moneyMap.put(itemEconomyType, amountSold2);


                            } else {
                                event.getPlayer().getInventory().addItem(i);
                            }

                        }

                        if (totalPrice > 0) {

                            /*for(int i = 0; i < soldMap.values().toArray().length; i++) {

                                String[] eae = soldMap.values().toArray()[i].toString().subSequence(1,soldMap.values().toArray()[i].toString().length()-1).toString().split("=");

                                // eae[0] - Damage
                                // eae[1] - Amount

                                player.sendMessage(soldMap.keySet().toArray()[i].toString());

                                ItemStack kk = new ItemStack(Material.matchMaterial(soldMap.keySet().toArray()[i].toString()));
                                kk.setDurability(Short.valueOf(eae[0]));

                                Double yes = ShopGuiPlusApi.getItemStackPriceSell(kk);

                                player.sendMessage(String.valueOf(yes));

                            }*/

                            //
                            //
                            //
                            //
                            //

                            String pricing = "";

                            for (Integer a = 0; a < moneyMap.values().toArray().length; a++) {
                                ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider((EconomyType) moneyMap.keySet().toArray()[a]).deposit(player, (double) moneyMap.values().toArray()[a]);

                                if (a != (moneyMap.values().toArray().length - 1)) {
                                    pricing = pricing + ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider((EconomyType) moneyMap.keySet().toArray()[a]).getCurrencyPrefix() + moneyMap.values().toArray()[a] + ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider((EconomyType) moneyMap.keySet().toArray()[a]).getCurrencySuffix() + ", ";
                                } else {
                                    pricing = pricing + ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider((EconomyType) moneyMap.keySet().toArray()[a]).getCurrencyPrefix() + moneyMap.values().toArray()[a] + ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider((EconomyType) moneyMap.keySet().toArray()[a]).getCurrencySuffix() + "";
                                }
                            }

                            String output = "";

                            if(main.configHandler.getConfigC().getInt("options.receipt_type") == 1 || main.configHandler.getConfigC().getString("messages.items_sold").contains("{list}")) {

                                player.sendMessage(moneyMap.keySet().toString());

                                for (Integer i = 0; i < soldMap.keySet().toArray().length; i++) {

                                    String[] itemDamagesAndAmounts = soldMap.values().toArray()[i].toString().subSequence(1,soldMap.values().toArray()[i].toString().length()-1).toString().split("=");
                                    ItemStack materialItemStack = new ItemStack(Material.matchMaterial(soldMap.keySet().toArray()[i].toString()));;
                                    materialItemStack.setDurability(Short.valueOf(itemDamagesAndAmounts[0]));

                                    double profits = ShopGuiPlusApi.getItemStackPriceSell(player, materialItemStack) * Integer.valueOf(itemDamagesAndAmounts[1]);
                                    String profitsFormatted = ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(getEconomyType(materialItemStack, player)).getCurrencyPrefix() + profits + ShopGuiPlusApi.getPlugin().getEconomyManager().getEconomyProvider(getEconomyType(materialItemStack, player)).getCurrencySuffix();

                                    String itemNameFormatted = WordUtils.capitalize(materialItemStack.getType().name().replace("_", " ").toLowerCase());

                                    output = output + "\n" + main.configHandler.getConfigC().getString("messages.receipt_item_layout").replace("{amount}", itemDamagesAndAmounts[1]).replace("{item}", itemNameFormatted).replace("{price}", profitsFormatted);
                                }

                            }

                            if(main.configHandler.getConfigC().getInt("options.receipt_type") == 1) {

                                String msg = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.items_sold").replace("{earning}", pricing).replace("{receipt}", "").replace("{list}", output));
                                String receiptName = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.receipt_text"));

                                String receipt = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.receipt_title") + output);
                                TextComponent test = new TextComponent(" " + receiptName);
                                test.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new ComponentBuilder(receipt).create()));

                                TextComponent test2 = new TextComponent(msg);

                                player.spigot().sendMessage(test2, test);

                            }

                            else {

                                String msg = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.items_sold").replace("{earning}", pricing).replace("{receipt}", "").replace("{list}", output));
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

                    String NoPermission = ChatColor.translateAlternateColorCodes('&', main.configHandler.getConfigC().getString("messages.no_permission"));
                    sender.sendMessage(NoPermission);

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
