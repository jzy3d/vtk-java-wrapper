package windows.opengl32.classloader;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import vtk.rendering.jogl.Environment;

/**
 * To verify loading/unloading, we init a JOGL context to ask to the currently available GL what is
 * the current GL_RENDERER.
 * 
 * 
 * May use these env. var (but this does not change the result

DYLD_INSERT_LIBRARIES=/Users/martin/Dev/jzy3d/external/osmesa/lib/libGL.dylib
DYLD_PRINT_BINDINGS=YES
DYLD_PRINT_LIBRARIES=YES
LIBGL_ALWAYS_SOFTWARE=true

This one corrupts execution

DYLD_LIBRARY_PATH=/Users/martin/Dev/jzy3d/external/osmesa/lib/:${env_var:DYLD_LIBRARY_PATH}

 * 
 * 
 */
public class TestLoadMesaOnMacOS {
  static String mesa = "/Users/martin/Dev/jzy3d/external/osmesa/lib/";

  public static void main(String[] args) throws Exception {

    loadUnload_Mesa_MacOS();

  }


  protected static void loadUnload_Mesa_MacOS()
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {

    // Add MESA DLL to path
    Environment env = new Environment();
    //env.appendFirst("PATH", mesa, ";");
    env.appendFirst("LD_LIBRARY_PATH", mesa, ":");

    // create print/console method
    // env.console("PATH", ";");

    // Load and attach a DLL to a nullable classloader which purpose is to unload the DLL manually
    // later
    CustomClassLoader classLoader = new CustomClassLoader();
    Class mesaLoaderClass =
        classLoader.findClass("windows.opengl32.classloader.OpenGLMesaLoader_macOS");
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

    // ------------------------------------------------------
    // Drawable to get a GL context

    GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
    GLAutoDrawable drawable =
        factory.createOffscreenAutoDrawable(factory.getDefaultDevice(), caps, null, 100, 100);

    drawable.display();
    drawable.getContext().makeCurrent();

    GL gl = drawable.getContext().getGL();


    System.out.println(getDebugInfo(gl));

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

    return sb.toString();
  }
}
