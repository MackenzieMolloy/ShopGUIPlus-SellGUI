package net.mackenziemolloy.SGPSellGUI;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AmountMaterial {

    private final Material material;
    private final int amount;
    public AmountMaterial(Material material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    private List<AmountMaterial> convert(ItemStack... itemArray) {
        List<AmountMaterial> newList = new ArrayList<>();
        for(ItemStack item : itemArray) {
            if(item == null) continue;
            Material material = item.getType();
            int amount = item.getAmount();
            newList.add(new AmountMaterial(material, amount));
        }
        return newList;
    }

    @Override
    public String toString() {
        return (this.material.name() + " x" + this.amount);
    }

}
