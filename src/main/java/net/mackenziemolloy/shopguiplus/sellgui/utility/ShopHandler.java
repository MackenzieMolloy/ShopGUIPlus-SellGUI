package net.mackenziemolloy.shopguiplus.sellgui.utility;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.economy.EconomyManager;
import net.brcdev.shopgui.economy.EconomyType;
import net.brcdev.shopgui.provider.economy.EconomyProvider;
import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;
import org.jetbrains.annotations.NotNull;

public class ShopHandler {
    @NotNull
    public static EconomyType getEconomyType(ItemStack material) {
        EconomyType economyType = ShopGuiPlusApi.getItemStackShop(material).getEconomyType();
        if(economyType != null) {
            return economyType;
        }
    
        EconomyManager economyManager = ShopGuiPlusApi.getPlugin().getEconomyManager();
        EconomyProvider defaultEconomyProvider = economyManager.getDefaultEconomyProvider();
        if(defaultEconomyProvider != null) {
            String defaultEconomyTypeName = defaultEconomyProvider.getName().toUpperCase(Locale.US);
            try {
                return EconomyType.valueOf(defaultEconomyTypeName);
            } catch(IllegalArgumentException ex) {
                return EconomyType.CUSTOM;
            }
        }
        
        return EconomyType.CUSTOM;
    }

    public static Double getItemSellPrice(ItemStack material, Player player) {
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
            if(Double.valueOf(priceToReturn.split("\\.")[1]) == 0) {
                priceToReturn = priceToReturn.split("\\.")[0];
            }

        }

        return priceToReturn;
    }

}
