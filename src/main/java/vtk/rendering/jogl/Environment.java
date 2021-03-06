package vtk.rendering.jogl;

import java.io.File;
import java.util.Map;
import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * A helper class to set and get environment variables immediately.
 * 
 * This method is more powerful than System.getenv because it returns the state of the variable as
 * it is when the get method is called, whereas System.getenv returns the state of the variable as
 * it was when the program was started.
 * 
 * 
 * https://developer.apple.com/library/archive/documentation/DeveloperTools/Conceptual/DynamicLibraries/100-Articles/DynamicLibraryUsageGuidelines.html
 * 
 * @author Martin
 */
public class Environment {

  /**
   * This get method is more powerful than System.getenv because it returns the state of the
   * variable as it is when the get method is called, whereas System.getenv returns the state of the
   * variable as it was when the program was started.
   * 
   * @param name
   * @return
   */
  public String get(String name) {
    if (OS.isWindows()) {
      WinLibC libc = (WinLibC) Native.loadLibrary("msvcrt", WinLibC.class);
      return libc.getenv(name);
    } else if (OS.isUnix() || OS.isMac()) {
      LibC libc = (LibC) Native.loadLibrary("c", LibC.class);
      return libc.getenv(name);
    } else {
      throw new RuntimeException("Not supported yet");
    }
  }

  public void set(String name, String value) {
    if (OS.isWindows()) {
      WinLibC libc = (WinLibC) Native.loadLibrary("msvcrt", WinLibC.class);
      libc._putenv(name + "=" + value);
    } else {
      LibC libc = (LibC) Native.loadLibrary("c", LibC.class);
      libc.setenv(name, value, 1);
    }
  }

  public void appendFirst(String name, String value) {
    appendFirst(name, value, File.pathSeparator);
  }

  public void appendFirst(String name, String value, String separator) {
    set(name, value + separator + get(name));
  }
  
  public void appendLast(String name, String value) {
    appendLast(name, value, File.pathSeparator);
  }

  public void appendLast(String name, String value, String separator) {
    set(name, get(name)+ separator + value);
  }

  public void removeFrom(String name, String toBeRemoved) {
    String oldvalue = get(name);
    
    if(!oldvalue.contains(toBeRemoved))
      return;
    
    // identify position of string
    int position = oldvalue.indexOf(toBeRemoved);
    int endsAt = position + toBeRemoved.length();
    
    // remove string
    String newvalue = oldvalue.replace(toBeRemoved, "");
    
    // remove related separator(s) 
    // at first position
    if(position==0 && (newvalue.charAt(position)==File.pathSeparatorChar)) {
      newvalue = newvalue.substring(1);
    }
    // at last position
    else if((endsAt==oldvalue.length()) && (newvalue.charAt(newvalue.length()-1)==File.pathSeparatorChar)) {
      newvalue = newvalue.substring(0, position-1);
    }
    // in the middle
    else {
      newvalue = newvalue.substring(0, position-1) + newvalue.substring(position, endsAt-1) + newvalue.substring(endsAt-1);
    }
    
    
    //newpath = newpath.replace(";;", ";");
    set(name, newvalue);
  }

  public void console(String name, String splitWidth) {
    print(name, get(name), splitWidth);
  }
  
  /*************** LIBC INTERFACE **************/

  /**
   * LibC interface for Unix, Linux, MacOS
   */
  public interface LibC extends Library {
    public int setenv(String name, String value, int overwrite);

    public String getenv(String name);
  }

  /**
   * LibC interface for Windows
   */
  public interface WinLibC extends Library {
    public int _putenv(String value);

    public String getenv(String name);
  }

  /*************** STATIC USING Sytem.getenv **************/

  public static void print(String var) {
    print(var, null);
  }

  public static void print(String var, String splitWith) {
    Map<String, String> env = System.getenv();

    boolean found = false;

    for (Map.Entry<String, String> entry : env.entrySet()) {
      if (entry.getKey().toLowerCase().equals(var.toLowerCase())) {
        found = true;

        String name = entry.getKey();
        String value = entry.getValue();

        print(name, value, splitWith);
      }
    }

    if (!found) {
      System.out.println(var + " was not found in the environment variable lists");
    }
  }

  public static void print(String name, String value, String splitWith) {
    if (splitWith == null) {
      System.out.println(name + " : " + value);
    } else {
      System.out.println(name + " : ");

      String[] values = value.split(splitWith);

      for (String val : values) {
        System.out.println(" " + val);
      }
    }
  }

}
