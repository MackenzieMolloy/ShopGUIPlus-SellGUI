package net.mackenziemolloy.shopguiplus.sellgui.utility;

public class ClassUtils {
  public static boolean hasMethod(Class<?> clazz, String methodName) {
    try {
      clazz.getMethod(methodName);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }
}
