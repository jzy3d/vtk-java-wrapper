package vtk.examples.gui.jogl.cpu;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.sun.jna.Library;
import com.sun.jna.Native;
import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkGenericOpenGLRenderWindow;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyDataMapper;
import vtk.rendering.jogl.vtkAbstractJoglComponent;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * An application that displays a 3D cone.
 * 
 * <h2>Support CPU rendering at startup</h2>
 * 
 * CPU rendering is achieved with the help of MESA (see MESA.md for more information on how to get
 * or build MESA).
 * 
 * The below indication explain how to add MESA to the path. One can verify environment variables
 * and settings known by JVM by adding the <code>-XshowSettings:properties</code> VM argument.
 * 
 * CPU rendering activation can be verified by reading in console : "OpenGL renderer string:
 * llvmpipe"
 *
 * 
 * <h4>Requirements on Linux</h4>
 * <ul>
 * <li>Set environment variable <code>LIBGL_ALWAYS_SOFTWARE=true</code> or programmatically in java <code>libc.setenv("LIBGL_ALWAYS_SOFTWARE", "true", 1);</code>.
 * <li><code>LD_LIBRARY_PATH=/home/martin/Dev/jzy3d/external/osmesa:$LD_LIBRARY_PATH</code>
 * </ul>
 * 
 * <h4>Requirements on Windows</h4>
 * <ul>
 * <li>System PATH should hold MESA and VTK path before system32 path (to ensure opengl lib provided
 * by mesa is loaded before). In addition, the program must force the load of OpenGL32.</li>
 * <li>-Djava.library.path="${env_var:PATH}"</li>
 * </ul>
 * 
 * <h4>Requirements on MacOS</h4>
 * <ul>
 * <li>
 * <li>
 * </ul>
 * 
 * 
 * 
 * <h2>Support GPU rendering at startup</h2>
 * 
 * Requires
 * <ul>
 * <li><code>libc.setenv("LIBGL_ALWAYS_SOFTWARE", "false", 1);</code>.
 * </ul>
 * 
 *
 * <h2>Switch CPU/GPU</h2>
 *
 * Ability to switch dynamically CPU/GPU using key 'c' and 'g'.
 * 
 * CPU rendering may trigger warnings about non supported feature of OpenGL when going from GPU to
 * CPU rendering.
 * 
 * E.g.
 * 
 * "tkTextureObject (0x7fb9907562b0): failed after SendParameters 1 OpenGL errors detected 0 :
 * (1280) Invalid enum"
 * 
 * is due to the fact that GL_MAX_TEXTURE_MAX_ANISOTROPY is a not supported extension.
 */
public class DemoVTKPanelJoglCPU {

  static {
    try {
      // Preload opengl on Windows only
      if(isWindows()) {
        System.loadLibrary("opengl32");
      }
      
      // Load VTK
      if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
        for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
          if (!lib.IsLoaded()) {
            System.out.println(lib.GetLibraryName() + " not loaded");
          }
        }
      }

      vtkNativeLibrary.DisableOutputWindow(null);
    } catch (UnsatisfiedLinkError e) {
      e.printStackTrace();
    } finally {
      printEnv("PATH", ";");
      printEnv("LIBGL_ALWAYS_SOFTWARE");
      System.out.println("-Djava.library.path=" + System.getProperty("java.library.path"));
    }
  }

  public interface LibC extends Library {
    public int setenv(String name, String value, int overwrite);
  }

  static vtkConeSource cone;
  static vtkPolyDataMapper coneMapper;
  static vtkActor actor;
  static JFrame frame;
  static vtkAbstractJoglComponent<?> joglWidget;
  static boolean report = true;

  static boolean libC = false;

  public static void init() {

    // Scene content
    vtkConeSource cone = new vtkConeSource();
    cone.SetResolution(8);

    vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
    coneMapper.SetInputConnection(cone.GetOutputPort());

    vtkActor actor = new vtkActor();
    actor.SetMapper(coneMapper);

    // ---------------------------------------------
    // Reset GL profile to ensure we load capabilities with a profile matching the driver
    // we use (CPU or GPU), so that we do not start a CPU rendering configured with the GPU
    // capabilities. This is important to avoid crashes with CPU/Mesa at startup

    GLProfile.shutdown();
    GLProfile.initSingleton();

    // Create a window and panel with bounded GL capabilities to ensure compatibility
    // between native and software GL

    vtkGenericOpenGLRenderWindow window = new vtkGenericOpenGLRenderWindow();
    GLCapabilities capabilities = new GLCapabilities(GLProfile.getMaximum(true));
    // GLCapabilities capabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));

    joglWidget = new vtkJoglPanelComponent(window, capabilities);

    // Disable multisampling that is not supported by MESA
    joglWidget.getRenderWindow().SetMultiSamples(0);


    // ----------------------------------------------
    // Add content

    joglWidget.getRenderer().AddActor(actor);

    // ----------------------------------------------
    // Frame

    frame = new JFrame(DemoVTKPanelJoglCPU.class.getSimpleName());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(joglWidget.getComponent(), BorderLayout.CENTER);
    frame.setSize(400, 400);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);


    // ----------------------------------------------
    // A 1 time report

    report = true;

    final Runnable reportCallback = new Runnable() {
      public void run() {
        if (report == true) {
          Object[] lines = joglWidget.getRenderWindow().ReportCapabilities().lines().toArray();

          System.out.println("Report: ");
          for (int i = 0; i < 3; i++) {
            System.out.println((String) lines[i]);
          }

          report = false;
        }
      }
    };

    joglWidget.getRenderWindow().AddObserver("RenderEvent", reportCallback, "run");


    // ----------------------------------------------
    // CPU/GPU Keyboard toggle

    joglWidget.getComponent().addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == 'r') {
          joglWidget.resetCamera();
        } else if (e.getKeyChar() == 'c') {
          LibC libc = (LibC) Native.loadLibrary("c", LibC.class);
          libc.setenv("LIBGL_ALWAYS_SOFTWARE", "true", 1);

          clean();
          init();
        } else if (e.getKeyChar() == 'g') {
          LibC libc = (LibC) Native.loadLibrary("c", LibC.class);
          libc.setenv("LIBGL_ALWAYS_SOFTWARE", "false", 1);

          clean();
          init();
        } else if (e.getKeyChar() == 'q') {
          System.exit(0);
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {}

      @Override
      public void keyPressed(KeyEvent e) {}
    });
  }

  public static void useCPU(boolean cpu) {
    try {
      LibC libc = (LibC) Native.loadLibrary("c", LibC.class);

      if (cpu)
        libc.setenv("LIBGL_ALWAYS_SOFTWARE", "true", 1);
      else
        libc.setenv("LIBGL_ALWAYS_SOFTWARE", "false", 1);
    } catch (Throwable e) {
      System.err.println("Can't dynamically change CPU/GPU because could not invoke libC");
    }
  }

  public static void clean() {
    frame.setVisible(false);
    frame.dispose();

    frame = null;
    System.gc();
  }

  public static void main(String s[]) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {

        useCPU(true);
        init();

      }
    });
  }

  public static void printEnv(String var) {
    printEnv(var, null);
  }

  public static void printEnv(String var, String splitWith) {
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
  
  public static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().indexOf("win")>=0;
  }
}
