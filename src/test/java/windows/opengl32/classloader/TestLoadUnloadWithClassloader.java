package windows.opengl32.classloader;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import vtk.rendering.jogl.Environment;

/**
 * Trying to unload a DLL as suggested here :
 * https://web.archive.org/web/20131202083900/http://www.codeslices.net/snippets/java-custom-url-class-loader-implementation-with-repository
 * 
 * To verify loading/unloading, we init a JOGL context to ask to the currently available GL what is
 * the current GL_RENDERER.
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
  //static String vtk =
  //    "C:\\Users\\Martin\\Dev\\jzy3d\\private\\vtk-java-wrapper\\lib\\9.1.0\\vtk-Windows-x86_64";


  public static void main(String[] args) throws Exception {

    loadUnload_Mesa();

    loadUnload_Windows();

  }

  protected static void loadUnload_Windows()
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    System.out.println("-------------------");
    System.out.println("LOADING WINDOWS GL");

    // Load and attach a DLL to a nullable classloader which purpose is to unload the DLL manually
    // later
    CustomClassLoader classLoader = new CustomClassLoader();
    Class windowsLoaderClass =
        classLoader.findClass("windows.opengl32.classloader.OpenGLWindowsLoader");
    Object windowsLoader = windowsLoaderClass.newInstance();

    // Start JOGL to get GL_RENDERER string
    initJOGLAndPrint();

    // UNLOAD DLL
    windowsLoaderClass = null;
    windowsLoader = null;
    classLoader = null;

    System.gc();
    System.runFinalization();
    System.gc();
    System.runFinalization();

    System.out.flush();

  }

  protected static void loadUnload_Mesa()
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {

    System.out.println("-------------------");
    System.out.println("LOADING MESA GL");

    // Add MESA DLL to path
    Environment env = new Environment();
    env.appendFirst("PATH", mesa, ";");
    // env.appendFirst("PATH", vtk, ";");

    // create print/console method
    // env.console("PATH", ";");

    // Load and attach a DLL to a nullable classloader which purpose is to unload the DLL manually
    // later
    CustomClassLoader classLoader = new CustomClassLoader();
    Class mesaLoaderClass = classLoader.findClass("windows.opengl32.classloader.OpenGLMesaLoader");
    Object mesaLoader = mesaLoaderClass.newInstance();

    // Start JOGL to get GL_RENDERER string
    initJOGLAndPrint();

    // UNLOAD DLL
    mesaLoaderClass = null;
    mesaLoader = null;
    classLoader = null;


    System.gc();
    System.runFinalization();
    System.gc();
    System.runFinalization();

    System.out.flush();

  }

  protected static void initJOGLAndPrint() {
    // ---------------------------------------------
    // Reset GL profile to ensure we load capabilities with a profile matching the driver
    // we use (CPU or GPU), so that we do not start a CPU rendering configured with the GPU
    // capabilities. This is important to avoid crashes with CPU/Mesa at startup

    GLProfile.shutdown();
    GLProfile.initSingleton();

    // Create profile and capabilities
    GLProfile glp = GLProfile.getMaxProgrammable(true);
    GLCapabilities caps = new GLCapabilities(glp);
    caps.setOnscreen(false);

    // Query GL to get info about the current renderer, MESA or WINDOWS
    createGLContextAndPrintInfo(glp, caps);
  }

  public static void createGLContextAndPrintInfo(GLProfile glp, GLCapabilities caps) {
    boolean glVersion = false;
    boolean glProfile = false;
    boolean glContext = false;
    boolean glVendor = true;

    // ------------------------------------------------------
    // Drawable to get a GL context

    GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
    GLAutoDrawable drawable =
        factory.createOffscreenAutoDrawable(factory.getDefaultDevice(), caps, null, 100, 100);

    drawable.display();
    drawable.getContext().makeCurrent();

    GL gl = drawable.getContext().getGL();


    // ------------------------------------------------------
    // Report

    if (glProfile) {
      System.out.println("PROFILE       : " + glp);
      System.out.println("CAPS (query)  : " + caps);
      System.out.println("CAPS (found)  : " + drawable.getChosenGLCapabilities());

      System.out.println("--------------------------------------------------");
    }

    if (glVendor) {
      System.out.println(getDebugInfo(gl));
    }

    if (glContext) {
      System.out.println("--------------------------------------------------");
      System.out.println(drawable.getContext());
      System.out.println();
      System.out.println("Is compat profile : " + drawable.getContext().isGLCompatibilityProfile());
    }


    if (glVersion) {
      System.out.println("--------------------------------------------------");
      System.out.println("GL2    : " + GLProfile.isAvailable(GLProfile.GL2));
      System.out.println("GL2GL3 : " + GLProfile.isAvailable(GLProfile.GL2GL3));
      System.out.println("GL3    : " + GLProfile.isAvailable(GLProfile.GL3));
      System.out.println("GL3bc  : " + GLProfile.isAvailable(GLProfile.GL3bc));
      System.out.println("GL4    : " + GLProfile.isAvailable(GLProfile.GL4));
      System.out.println("GL4ES3 : " + GLProfile.isAvailable(GLProfile.GL4ES3));
      System.out.println("GL4bc  : " + GLProfile.isAvailable(GLProfile.GL4bc));
    }

    // ------------------------------------------------------
    // We are done, release context for further work

    drawable.getContext().release();
  }

  public static String getDebugInfo(GL gl) {
    StringBuffer sb = new StringBuffer();
    sb.append("GL_VENDOR     : " + gl.glGetString(GL.GL_VENDOR) + "\n");
    sb.append("GL_RENDERER   : " + gl.glGetString(GL.GL_RENDERER) + "\n");
    sb.append("GL_VERSION    : " + gl.glGetString(GL.GL_VERSION) + "\n");

    String ext = gl.glGetString(GL.GL_EXTENSIONS);

    if (ext != null) {
      String[] exts = ext.split(" ");
      sb.append("GL_EXTENSIONS : (" + exts.length + ")\n");
      /*
       * for(String e: exts) { sb.append("\t" + e + "\n"); }
       */
    } else {
      sb.append("GL_EXTENSIONS : null\n");
    }

    sb.append("GL INSTANCE : " + gl.getClass().getName() + "\n");

    return sb.toString();
  }
}
