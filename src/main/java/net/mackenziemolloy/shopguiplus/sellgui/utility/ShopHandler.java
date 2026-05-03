package net.mackenziemolloy.shopguiplus.sellgui.utility;

import java.text.DecimalFormat;
import java.util.Locale;

import net.brcdev.shopgui.economy.EconomyCurrencyType;
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
    public static EconomyCurrencyType getEconomyType(ItemStack itemStack) {
        EconomyCurrencyType economyCurrencyType = ShopGuiPlusApi.getItemStackShop(itemStack).getEconomyCurrencyType();

        return economyCurrencyType;
    }

    public static Double getItemSellPrice(ItemStack material, Player player) {
        return ShopGuiPlusApi.getItemStackPriceSell(player, material);
    }

    public static String getFormattedPrice(Double priceToFormat, EconomyType economyType) {
        SellGUI plugin = JavaPlugin.getPlugin(SellGUI.class);
        CommentedConfiguration configuration = plugin.getConfiguration();
        String priceToReturn = priceToFormat.toString();

        if (configuration.getBoolean("options.rounded_pricing")) {
            DecimalFormat formatToApplyRaw = new DecimalFormat("0.00");
            priceToReturn = formatToApplyRaw.format(priceToFormat);
        }

        if (configuration.getBoolean("options.remove_trailing_zeros")) {
            if (Double.valueOf(priceToReturn.split("\\.")[1]) == 0) {
                priceToReturn = priceToReturn.split("\\.")[0];
            }
        }

        return priceToReturn;
    }

    public static String getCurrencyId(EconomyCurrencyType economyCurrencyType) {
        return economyCurrencyType.getEconomyProviderId() +  "-" + economyCurrencyType.getCurrencyId();
    }
}
