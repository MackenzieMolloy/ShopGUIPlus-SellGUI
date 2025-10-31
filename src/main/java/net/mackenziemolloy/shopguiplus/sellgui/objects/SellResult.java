package net.mackenziemolloy.shopguiplus.sellgui.objects;

import org.bukkit.inventory.ItemStack;

public record SellResult(
        double totalPrice,
        int itemAmount,
        ItemStack shulker
) {}