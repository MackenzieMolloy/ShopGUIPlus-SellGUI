package net.mackenziemolloy.shopguiplus.sellgui.objects;

import net.brcdev.shopgui.economy.EconomyType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class SoldItemEntry {

    private final ItemStack item;
    private final int amount;
    private final double price;
    private final EconomyType economyType;

    public SoldItemEntry(@NotNull ItemStack item, int amount, double price, @NotNull EconomyType economyType) {
        this.item = item.clone();
        this.item.setAmount(1);
        this.amount = amount;
        this.price = price;
        this.economyType = economyType;
    }

    public @NotNull ItemStack getItem() {
        return item.clone();
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public @NotNull EconomyType getEconomyType() {
        return economyType;
    }
}
