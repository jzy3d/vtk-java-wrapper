package vtk.examples.gui.jogl.cpu;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jzy3d.maths.Array;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.sun.jna.Library;
import com.sun.jna.Native;
import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkGenericOpenGLRenderWindow;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyDataMapper;
import vtk.examples.gui.jogl.cpu.DemoVTKPanelJoglCPU.ChipSelector.Chip;
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
 * <li>Set environment variable <code>LIBGL_ALWAYS_SOFTWARE=true</code> or programmatically in java
 * <code>libc.setenv("LIBGL_ALWAYS_SOFTWARE", "true", 1);</code>.
 * <li><code>LD_LIBRARY_PATH=/home/martin/Dev/jzy3d/external/osmesa:$LD_LIBRARY_PATH</code>
 * </ul>
 * 
 * <h4>Requirements on Windows</h4>
 * 
 * Get a MESA distribution from https://download.jzy3d.org/mesa/mesa-21.3.7-Windows-x86_64.zip and
 * unpack it somewhere, e.g. in ./lib/
 * <ul>
 * <li>System PATH should hold MESA path (e.g.
 * C:\Users\Martin\Dev\jzy3d\private\vtk-java-wrapper\lib\9.1.0\mesa-Windows-x86_64) and VTK path
 * (C:\Users\Martin\Dev\jzy3d\private\vtk-java-wrapper\lib\9.1.0\vtk-Windows-x86_64) before system32
 * path (to ensure opengl lib provided by mesa is loaded before). In addition, the program must
 * force the load of OpenGL32.</li>
 * <li>Run with -Djava.library.path="${env_var:PATH}" (this is the Eclipse way of providing the PATH
 * variable to java.library.path)</li>
 * </ul>
 * 
 * -Djava.library.path=C:\Users\Martin\Dev\jzy3d\private\vtk-java-wrapper\lib\9.1.0\mesa-Windows-x86_64;C:\Users\Martin\Dev\jzy3d\private\vtk-java-wrapper\lib\9.1.0\vtk-Windows-x86_64
 * 
 * 
 * 
 * C:\Users\Martin\Dev\jzy3d\private\vtk-java-wrapper\lib\9.1.0\mesa-Windows-x86_64;${env_var:PATH}
 * 
 * <h4>Requirements on MacOS</h4>
 * <ul>
 * <li>
 * <li>
 * </ul>
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
 * "vtkTextureObject (0x7fb9907562b0): failed after SendParameters 1 OpenGL errors detected 0 :
 * (1280) Invalid enum"
 * 
 * is due to the fact that GL_MAX_TEXTURE_MAX_ANISOTROPY is a not supported extension.
 */
public class DemoVTKPanelJoglCPU {
  static Chip startChip = Chip.CPU;
  
  static ChipSelector selector = new ChipSelector();
  
  static {


    try {
      // Preload opengl on Windows only
      /*if (isWindows()) {
        System.loadLibrary("opengl32");
      }
      else if (isMac()) {
        System.loadLibrary("GL");
      }*/
      // Preload opengl library configuration according to OS
      // Linux : configure LIBGL_ALWAYS_SOFTWARE=true to use MESA in CPU mode
      // Windows : configure PATH to let MESA appear before system32 if CPU is required
      selector.use(startChip);

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
      Environment.print("PATH", ";");
      Environment.print("LIBGL_ALWAYS_SOFTWARE");
      System.out.println("-Djava.library.path=" + System.getProperty("java.library.path"));
    }
  }


  static JFrame frame;
  static vtkAbstractJoglComponent<?> joglWidget;
  static boolean report = true;

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

          Chip chip = Chip.GPU;
          for (int i = 0; i < 3; i++) {
            if(((String)lines[i]).contains("llvm")) {
              chip = Chip.CPU;
            }
          }
          
          System.out.println("JOGL is now using : " + chip);

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

          ChipSelector s = new ChipSelector();
          s.use(Chip.CPU);

          clean();
          init();
        } else if (e.getKeyChar() == 'g') {

          ChipSelector s = new ChipSelector();
          s.use(Chip.GPU);

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

  public static void clean() {
    frame.setVisible(false);
    frame.dispose();

    frame = null;
    selector = null;
    System.gc();
  }

  public static void main(String s[]) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {

        selector = new ChipSelector();
        selector.use(startChip);

        init();
      }
    });
  }

  /****************************************/
  /**                                    **/
  /** GPU/CPU TOGGLE **/
  /**                                    **/
  /****************************************/

  public static class ChipSelector {
    enum Chip {
      CPU, GPU
    }

    public void use(Chip chip) {
     
      
      System.out.println("--------------------");
      System.out.println("WANT TO USE : " + chip);
      
      // WIndows
      if (isWindows()) {
        Environment e = new Environment();
        e.set("TEST_ENV_VAR", "toto");
        System.out.println("TEST_ENV_VAR:" + e.get("TEST_ENV_VAR"));

        String oldpath = e.get("PATH");

        System.out.println("oldpath : " + oldpath);
        
        if (Chip.CPU.equals(chip)) {

          e.set("PATH", MESA_PATH + ";" + oldpath);
          System.out.println("newpath : " + e.get("PATH"));
          
          loadOpenGLMesa();
          
        } else if (Chip.GPU.equals(chip)) {
          
          String newpath = oldpath.replace(MESA_PATH, "");
          newpath = newpath.replace(";;", ";");
          // TODO : avec et sans slash final
          // TODO : faire slash et backslash
          
          e.set("PATH", newpath);
          System.out.println("newpath : " + e.get("PATH"));
          
          loadOpenGLWindows();
        } else
          throw new RuntimeException("Unsupported " + chip);
        
      } 
      
      // ----------------------------------
      // Linux
      else {
        Environment e = new Environment();

        if (Chip.CPU.equals(chip)) {
          e.set("LIBGL_ALWAYS_SOFTWARE", "true");

          loadOpenGLMesa();
        } else if (Chip.GPU.equals(chip)) {
          e.set("LIBGL_ALWAYS_SOFTWARE", "false");

          // loadOpenGLWindows();
        } else
          throw new RuntimeException("Unsupported " + chip);
      }


    }

    // unload :
    // https://web.archive.org/web/20140704120535/http://www.codethesis.com/blog/unload-java-jni-dll
    /*public static void init(Chip chip) {

      if (isWindows()) {
        if (Chip.CPU.equals(chip))
          loadOpenGLMesa();
        else if (Chip.GPU.equals(chip))
          loadOpenGLWindows();
        else
          throw new RuntimeException("Unsupported " + chip);
      }
    }*/
    
    protected static void loadOpenGL() {
      System.loadLibrary("opengl32");
    }

    protected static void loadOpenGLWindows() {
      // System.un
      System.out.println("Try loading Windows GL");
      System.load("C:\\Windows\\System32\\opengl32.dll");
    }

    protected static void loadOpenGLMesa() {
      System.out.println("Try loading MESA GL");

      System.load(
          MESA_PATH + "/opengl32.dll");
    }
    
    static String MESA_PATH = "C:\\Users\\Martin\\Dev\\jzy3d\\private\\vtk-java-wrapper\\lib\\9.1.0\\mesa-Windows-x86_64";
    //static String MESA_PATH = "C:/Users/Martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/mesa-Windows-x86_64/";

  }
  

  /****************************************/
  /**                                    **/
  /** ENVIRONMENT **/
  /**                                    **/
  /****************************************/


  public static class Environment {

    public void set(String name, String value) {
      if (isWindows()) {
        WinLibC libc = (WinLibC) Native.loadLibrary("msvcrt", WinLibC.class);
        libc._putenv(name +"="+ value);
      } else {
        LibC libc = (LibC) Native.loadLibrary("c", LibC.class);
        libc.setenv(name, value, 1);
      }
    }
    
    public String get(String name) {
      
      if (isWindows()) {
        WinLibC libc = (WinLibC) Native.loadLibrary("msvcrt", WinLibC.class);
        return libc.getenv(name);
      } else {
        LibC libc = (LibC) Native.loadLibrary("c", LibC.class);
        libc.getenv(name);
        return System.getenv(name);
      }
      //return null;
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



  public static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
  }

  public static boolean isMac() {
    return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
  }

}
