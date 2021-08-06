package net.mackenziemolloy.shopguiplus.sellgui.utility;

import java.text.DecimalFormat;
import java.util.Locale;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.economy.EconomyType;
import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;

public class ShopHandler {
    public static EconomyType getEconomyType(ItemStack material, Player player) {
        EconomyType itemEconomyType = ShopGuiPlusApi.getItemStackShop(material).getEconomyType();
        String defaultEconomyType = ShopGuiPlusApi.getPlugin().getEconomyManager().getDefaultEconomyProvider()
                .getName().toUpperCase(Locale.US);

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

    public static Double getItemSellPrice(ItemStack material, Player player) {

        //SellGUI sellGUI = SellGUI.getInstance();

        return ShopGuiPlusApi.getItemStackPriceSell(player, material);


    }

    public static String getFormattedPrice(Double priceToFormat, EconomyType economyType) {
        SellGUI plugin = JavaPlugin.getPlugin(SellGUI.class);
        CommentedConfiguration configuration = plugin.getConfiguration();
        String priceToReturn = priceToFormat.toString();

        if(configuration.getBoolean("options.rounded_pricing")) {
            DecimalFormat formatToApplyRaw = new DecimalFormat("0.00");
            priceToReturn = formatToApplyRaw.format(priceToFormat);
        }

        if(configuration.getBoolean("options.remove_trailing_zeros")) {
            if(Integer.valueOf(priceToReturn.split("\\.")[1]) == 0) {
                priceToReturn = priceToReturn.split("\\.")[0];
            }

        }

        return priceToReturn;

    }

    public static String getFormattedNumber(Double numberToFormat) {
        SellGUI plugin = JavaPlugin.getPlugin(SellGUI.class);
        CommentedConfiguration configuration = plugin.getConfiguration();
        String numberToReturn =  numberToFormat.toString();

        if (configuration.getBoolean("options.rounded_pricing")) {

            final DecimalFormat formatToApply = new DecimalFormat("#,##0.00");
            numberToReturn = formatToApply.format(numberToFormat);
        }

        if(configuration.getBoolean("options.remove_trailing_zeros")) {
            if(Integer.valueOf(numberToReturn.split("\\.")[1]) == 0) {
                numberToReturn = numberToReturn.split("\\.")[0];
            }
        }

        return numberToReturn;
    }

}
