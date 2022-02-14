package vtk.examples.gui.jogl;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import com.sun.jna.Library;
import com.sun.jna.Native;
import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyDataMapper;
import vtk.rendering.jogl.vtkAbstractJoglComponent;
import vtk.rendering.jogl.vtkJoglCanvasComponent;
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
 * Ability to switch dynamically CPU/GPU using key 'c' and 'g' but there is a failure on Ubuntu for now.
 * 
 * @see
 * <ul>
 * <li>https://forum.jogamp.org/Linux-OpenGL-error-td4039376.html
 * <li>https://forum.jogamp.org/Caught-GLException-AWT-EventQueue-0-createImpl-ARB-n-a-but-required-profile-gt-GL2-td4039412.html
 * <li>https://fr.mathworks.com/matlabcentral/answers/402920-plots-with-opengl-look-distorted-without-opengl-unable-to-plot-at-all-unless-running-matlab-as-an-a
 * </ul>
 * 
 */
public class DemoVTKPanelJoglCPU {

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
    vtkConeSource cone = new vtkConeSource();
    cone.SetResolution(8);

    vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
    coneMapper.SetInputConnection(cone.GetOutputPort());

    vtkActor actor = new vtkActor();
    actor.SetMapper(coneMapper);

    joglWidget = new vtkJoglPanelComponent();
    System.out.println(
        "We are using " + joglWidget.getComponent().getClass().getName() + " for the rendering.");

    joglWidget.getRenderWindow().SetMultiSamples(0);
    joglWidget.getRenderer().AddActor(actor);

    frame = new JFrame("SimpleVTK");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(joglWidget.getComponent(), BorderLayout.CENTER);
    frame.setSize(400, 400);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

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
