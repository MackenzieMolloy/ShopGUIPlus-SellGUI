package net.mackenziemolloy.shopguiplus.sellgui.objects;

import net.brcdev.shopgui.economy.EconomyCurrencyType;
import net.brcdev.shopgui.economy.EconomyType;

public class ShopItemPriceValue {

  private final EconomyCurrencyType economyCurrencyType;
  private final double sellPrice;

  public ShopItemPriceValue(EconomyCurrencyType economyCurrencyType, double sellPrice) {
    this.economyCurrencyType = economyCurrencyType;
    this.sellPrice = sellPrice;
  }

  public EconomyCurrencyType getEconomyCurrencyType() {
    return economyCurrencyType;
  }

  public double getSellPrice() {
    return sellPrice;
  }
}
