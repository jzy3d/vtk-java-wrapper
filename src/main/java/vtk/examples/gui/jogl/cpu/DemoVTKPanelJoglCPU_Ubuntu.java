package vtk.examples.gui.jogl.cpu;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
 * Requires 
 * <ul>
 * <li><code>libc.setenv("LIBGL_ALWAYS_SOFTWARE", "true", 1);</code>.
 * <li><code>LD_LIBRARY_PATH=/home/martin/Dev/jzy3d/external/osmesa:$LD_LIBRARY_PATH</code>
 * </ul>
 * 
 * Check CPU rendering by reading in console : "OpenGL renderer string:  llvmpipe"
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
 * CPU rendering may trigger warnings about non supported feature of OpenGL when going from GPU to CPU rendering.
 * 
 * E.g.
 * 
 * "tkTextureObject (0x7fb9907562b0): failed after SendParameters 1 OpenGL errors detected  0 : (1280) Invalid enum"
 * 
 * is due to the fact that GL_MAX_TEXTURE_MAX_ANISOTROPY is a not supported extension.
 */
public class DemoVTKPanelJoglCPU_Ubuntu {

  static {
    if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
      for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
        if (!lib.IsLoaded()) {
          System.out.println(lib.GetLibraryName() + " not loaded");
        }
      }
    }
    vtkNativeLibrary.DisableOutputWindow(null);
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
    //GLCapabilities capabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));

    joglWidget = new vtkJoglPanelComponent(window, capabilities);
    
    // Disable multisampling that is not supported by MESA
    joglWidget.getRenderWindow().SetMultiSamples(0); 
    
    
    // ----------------------------------------------
    // Add content
    
    joglWidget.getRenderer().AddActor(actor);

    // ----------------------------------------------
    // Frame
    
    frame = new JFrame(DemoVTKPanelJoglCPU_Ubuntu.class.getSimpleName());
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

  public static void clean() {
    frame.setVisible(false);
    frame.dispose();

    frame = null;
    System.gc();
  }

  public static void main(String s[]) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {

        LibC libc = (LibC) Native.loadLibrary("c", LibC.class);
        libc.setenv("LIBGL_ALWAYS_SOFTWARE", "true", 1);
        
        init();

      }
    });
  }
}