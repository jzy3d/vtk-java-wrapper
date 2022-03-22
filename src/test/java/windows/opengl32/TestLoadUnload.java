package windows.opengl32;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import jgl.GL;
import jgl.context.gl_context;

// https://www.baeldung.com/java-jna-dynamic-libraries
/** experimental */
public class TestLoadUnload {
  

  public interface WinOpenGL extends Library {
  //https://docs.microsoft.com/en-us/windows/win32/opengl/glgetstring
    //https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glGetString.xhtml
    public long glGetString(int value);
    
    //https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glGetError.xhtml
    public int glGetError();
    
    //https://docs.microsoft.com/en-us/windows/win32/opengl/glgetintegerv
    public void glGetIntegerv(int pname, int[] params);
    
    
  }
  
  public static void main(String[] args) {
    
    WinOpenGL lib = (WinOpenGL) Native.load("opengl32", WinOpenGL.class);
    
    //Native.unregister(WinOpenGL.class);
    //System.gc();
    
    //https://java-native-access.github.io/jna/4.2.1/com/sun/jna/Native.html
    long addr = lib.glGetString(GL.GL_VERSION);

    if(addr!=0) {
      System.out.println("Out:" + addr);

      Pointer p = new Pointer(addr);
        
      String value = p.getString(0);
      System.out.println("Out:" + value);
      
      int[] out = {-1};
      
      lib.glGetIntegerv(GL.GL_ACCUM_RED_BITS, out);
      
      System.out.println("Out:" + out[0]);      
    }
    else {
     int err = lib.glGetError();
     System.out.println("Error: " + err);
     
     System.out.println( gl_context.errorCodeToString("", err, ""));
    }
    


  }
  

}
