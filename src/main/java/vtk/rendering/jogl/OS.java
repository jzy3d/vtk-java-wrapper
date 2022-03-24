package vtk.rendering.jogl;

public class OS {
  public static boolean isWindows() {
    return name().indexOf("win") >= 0;
  }
  
  public static boolean isUnix() {
    String name = name();
    return (name.indexOf("nix") >= 0 || name.indexOf("nux") >= 0 || name.indexOf("aix") > 0);
  }
  
  public static boolean isMac() {
    return name().indexOf("mac") >= 0;
  }

  public static String name() {
    return System.getProperty("os.name").toLowerCase();
  }
}
