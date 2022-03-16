package windows.opengl32;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

// https://www.baeldung.com/java-jna-dynamic-libraries
/** experimental */
public class TestLoadUnload {
  

  public interface WinOpenGL extends Library {
    public long glGetString(int value);
    
    //https://docs.microsoft.com/en-us/windows/win32/opengl/glgetintegerv
    public void glGetIntegerv(int pname, int[] params);
    
    
  }
  
  public static void main(String[] args) {
    
    WinOpenGL lib = (WinOpenGL) Native.load("opengl32", WinOpenGL.class);
    
    //Native.unregister(WinOpenGL.class);
    //System.gc();
    //https://docs.microsoft.com/en-us/windows/win32/opengl/glgetstring
    //https://java-native-access.github.io/jna/4.2.1/com/sun/jna/Native.html
    long addr = lib.glGetString(GL_VERSION);

    System.out.println("Out:" + addr);

    Pointer p = new Pointer(addr);
      
    String value = p.getString(0);
    System.out.println("Out:" + value);
    
    int[] out = {-1};
    
    lib.glGetIntegerv(GL_ACCUM_RED_BITS, out);
    
    System.out.println("Out:" + out[0]);

  }
  
  
  public static final int GL_VENDOR = 0x1F00;
  public static final int GL_RENDERER = 0x1F01;
  public static final int GL_VERSION = 0x1F02;
  public static final int GL_EXTENSIONS = 0x1F03;
  
  /* Accumulation buffer */
  public static final int GL_ACCUM_RED_BITS = 0x0D58;
  public static final int GL_ACCUM_GREEN_BITS = 0x0D59;
  public static final int GL_ACCUM_BLUE_BITS = 0x0D5A;
  public static final int GL_ACCUM_ALPHA_BITS = 0x0D5B;
  public static final int GL_ACCUM_CLEAR_VALUE = 0x0B80;
  public static final int GL_ACCUM = 0x0100;
  public static final int GL_ADD = 0x0104;
  public static final int GL_LOAD = 0x0101;
  public static final int GL_MULT = 0x0103;
  public static final int GL_RETURN = 0x0102;

}
