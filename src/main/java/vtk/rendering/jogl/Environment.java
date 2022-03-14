package vtk.rendering.jogl;

import java.util.Map;
import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * A helper class to set and get environement variables.
 * 
 * @author Martin
 */
public class Environment {

  public void set(String name, String value) {
    if (OS.isWindows()) {
      WinLibC libc = (WinLibC) Native.loadLibrary("msvcrt", WinLibC.class);
      libc._putenv(name + "=" + value);
    } else {
      LibC libc = (LibC) Native.loadLibrary("c", LibC.class);
      libc.setenv(name, value, 1);
    }
  }

  public String get(String name) {

    if (OS.isWindows()) {
      WinLibC libc = (WinLibC) Native.loadLibrary("msvcrt", WinLibC.class);
      return libc.getenv(name);
    } else {
      LibC libc = (LibC) Native.loadLibrary("c", LibC.class);
      libc.getenv(name);
      return System.getenv(name);
    }
    // return null;
  }

  public interface LibC extends Library {
    public int setenv(String name, String value, int overwrite);

    public String getenv(String name);
  }

  public interface WinLibC extends Library {
    public int _putenv(String value);

    public String getenv(String name);
  }


  public static void print(String var) {
    print(var, null);
  }

  public static void print(String var, String splitWith) {
    Map<String, String> env = System.getenv();

    boolean found = false;

    for (Map.Entry<String, String> entry : env.entrySet()) {
      if (entry.getKey().toLowerCase().equals(var.toLowerCase())) {
        found = true;

        if (splitWith == null) {
          System.out.println(entry.getKey() + " : " + entry.getValue());
        } else {
          System.out.println(entry.getKey() + " : ");

          String[] values = entry.getValue().split(splitWith);

          for (String value : values) {
            System.out.println(" " + value);
          }

        }

      }
    }

    if (!found) {
      System.out.println("Undefined environment variable " + var);
    }
  }

}
