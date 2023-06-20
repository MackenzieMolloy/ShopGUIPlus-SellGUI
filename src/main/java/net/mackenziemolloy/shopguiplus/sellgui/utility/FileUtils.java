package net.mackenziemolloy.shopguiplus.sellgui.utility;

import java.io.*;
import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;

public class FileUtils {

  public static void copy(InputStream in, File file) {
    try {
      OutputStream out = new FileOutputStream(file);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = in.read(buffer)) > 0) {
        out.write(buffer, 0, length);
      }
      out.close();
      in.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static void mkdir(File file) {
    try {
      file.mkdir();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static File loadFile(String name) {
    if (!SellGUI.getInstance().getDataFolder().exists()) {
      FileUtils.mkdir(SellGUI.getInstance().getDataFolder());
    }

    File f = new File(SellGUI.getInstance().getDataFolder(), name);
    if (!f.exists()) {
      FileUtils.copy(SellGUI.getInstance().getResource(name), f);
    }

    return f;
  }
}
