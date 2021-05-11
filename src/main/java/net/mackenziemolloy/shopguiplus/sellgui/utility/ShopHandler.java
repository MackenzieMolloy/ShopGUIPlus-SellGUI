package net.mackenziemolloy.shopguiplus.sellgui.utility;

import java.text.DecimalFormat;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.economy.EconomyType;
import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;

public class ShopHandler {

    public static EconomyType getEconomyType(ItemStack material, Player player) {

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

    public static Double getItemSellPrice(ItemStack material, Player player) {

        //SellGUI sellGUI = SellGUI.getInstance();

        return ShopGuiPlusApi.getItemStackPriceSell(player, material);


    }

    public static String getFormattedPrice(Double priceToFormat, EconomyType economyType) {

        SellGUI sellGUI = SellGUI.getInstance();

        String priceToReturn = priceToFormat.toString();

        if(sellGUI.configFile.getBoolean("options.rounded_pricing")) {

            DecimalFormat formatToApplyRaw = new DecimalFormat("0.00");
            priceToReturn = formatToApplyRaw.format(priceToFormat);

        }

        if(sellGUI.configFile.getBoolean("options.remove_trailing_zeros")) {

            if(Integer.valueOf(priceToReturn.split("\\.")[1]) == 0) {

                priceToReturn = priceToReturn.split("\\.")[0];

            }

        }

        return priceToReturn;

    }

}

        /*if(sellGUI.configFile.getBoolean("options.rounded_pricing")) {

            DecimalFormat formatToApply = new DecimalFormat("#,##0.00");

            if(raw) {
                DecimalFormat formatToApplyRaw = new DecimalFormat("#.##");
                return formatToApplyRaw.format(price);
            }
            else {
                return formatToApply.format(price);
            }

        }

        else {

            if(raw) {
                return String.valueOf(price);
            }
            else {
                return String.format("%,f", new BigDecimal(price)).replaceAll("0*$", "").replaceAll("\\.$", "");
            }

        }*/
