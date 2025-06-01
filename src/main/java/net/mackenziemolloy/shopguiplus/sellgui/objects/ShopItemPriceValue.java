package net.mackenziemolloy.shopguiplus.sellgui.objects;

import net.brcdev.shopgui.economy.EconomyType;

public class ShopItemPriceValue {

  private final EconomyType economyType;
  private final double sellPrice;

  public ShopItemPriceValue(EconomyType economyType, double sellPrice) {
    this.economyType = economyType;
    this.sellPrice = sellPrice;
  }

  public EconomyType getEconomyType() {
    return economyType;
  }

  public double getSellPrice() {
    return sellPrice;
  }
}
