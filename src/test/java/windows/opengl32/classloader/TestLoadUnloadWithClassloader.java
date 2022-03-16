package windows.opengl32.classloader;

import java.lang.reflect.Method;
import vtk.rendering.jogl.Environment;

/**
 * how to ask the native its identity (between Mesa and system openGL?)
 * 
 * May require
 * 
 * -Djava.library.path=C:\Users\Martin\Dev\jzy3d\private\vtk-java-wrapper\lib\9.1.0\vtk-Windows-x86_64;"${env_var:PATH}"
 * -Djava.library.path=C:\Users\Martin\Dev\jzy3d\private\vtk-java-wrapper\lib\9.1.0\mesa-Windows-x86_64;C:\Users\Martin\Dev\jzy3d\private\vtk-java-wrapper\lib\9.1.0\vtk-Windows-x86_64;"${env_var:PATH}"
 * -Djava.library.path=C:\Users\Martin\Dev\jzy3d\private\vtk-java-wrapper\lib\9.1.0\vtk-Windows-x86_64;"${env_var:PATH}"
 * 
 */
public class TestLoadUnloadWithClassloader {
  static String mesa =
      "C:\\Users\\Martin\\Dev\\jzy3d\\private\\vtk-java-wrapper\\lib\\9.1.0\\mesa-Windows-x86_64";
  static String vtk =
      "C:\\Users\\Martin\\Dev\\jzy3d\\private\\vtk-java-wrapper\\lib\\9.1.0\\vtk-Windows-x86_64";


  public static void main(String[] args) throws Exception {
    
    loadUnload_Mesa();
    
    loadUnload_Windows();

  }

  protected static void loadUnload_Windows()
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    CustomClassLoader cl = new CustomClassLoader();
    Class ca = cl.findClass("windows.opengl32.classloader.OpenGLWindowsLoader");
    Object a = ca.newInstance();
    // Method p = ca.getMethod("print");
    // p.invoke(a);
    // p = null;
    ca = null;
    a = null;
    cl = null;
    System.gc();
  }

  protected static void loadUnload_Mesa()
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    
    Environment env = new Environment();
    // create append /remove method
    env.appendFirst("PATH", vtk, ";");
    env.appendFirst("PATH", mesa, ";");
    // env.set("PATH", path + ";" + env.get("PATH"));
    // System.err.println("new PATH : " + env.get("PATH"));

    // create print/console method
    env.console("PATH", ";");
    
    CustomClassLoader cl2 = new CustomClassLoader();
    Class cb = cl2.findClass("windows.opengl32.classloader.OpenGLMesaLoader");
    Object b = cb.newInstance();
    // Method p = ca.getMethod("print");
    // p.invoke(a);
    // p = null;
    cb = null;
    b = null;
    cl2 = null;
    System.gc();
  }

}
