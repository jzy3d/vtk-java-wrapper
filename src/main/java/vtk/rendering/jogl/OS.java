package vtk.rendering.jogl;

public class OS {
  public static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
  }
  
  public static boolean isUnix() {
    String name = System.getProperty("os.name").toLowerCase();
    return (name.indexOf("nix") >= 0 || name.indexOf("nux") >= 0 || name.indexOf("aix") > 0);
  }
}
