package net.mackenziemolloy.shopguiplus.sellgui.utility;

import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class StringFormatter {

  public static ArrayList<String> quantityCodes = new ArrayList<String>(Arrays.asList("K", "M", "B", "T", "Q", "Qa", "Sx", "Sp", "Oc", "No", "De", "Un", "Du", "Tr", "Qu", "Qi", "Se", "Sev", "Oc", "Nov", "Vg", "C"));

  public static String abbreviateQuantity(double count) {
    if (count < 1000) return "" + count;
    int exp = (int) (Math.log(count) / Math.log(1000));

    DecimalFormat format = new DecimalFormat("0.##");
    String val = format.format(count / Math.pow(1000, exp));

    return String.format("%s%s", val, quantityCodes.get(exp-1));
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
      if(numberToReturn.split("\\.")[1] != null) {
        if (Double.valueOf(numberToReturn.split("\\.")[1]) == 0) {
          numberToReturn = numberToReturn.split("\\.")[0];
        }
      }
    }

    return configuration.getBoolean("options.abbreviate_numbers") && !configuration.getBoolean("options.rounded_pricing") ? abbreviateQuantity(Double.parseDouble(numberToReturn)) : numberToReturn;
  }

}
