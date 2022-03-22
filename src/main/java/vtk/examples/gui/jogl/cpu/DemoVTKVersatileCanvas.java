package vtk.examples.gui.jogl.cpu;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;
import vtk.rendering.jogl.OS;
import vtk.rendering.jogl.VTKVersatileCanvas;
import vtk.rendering.jogl.VTKVersatileCanvas.Listener;
import vtk.rendering.jogl.VTKVersatileCanvas.OnChipSwitch;
import vtk.rendering.jogl.chip.Chip;

/**
 * 
 * <h2>MacOS IDE configuration</h2>
 * -Djava.library.path=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-x86_64:${env_var:PATH}
 * 
 * 
 * 
 * <h2>MacOS hints</h2>
 * 
 * DYLD_INSERT_LIBRARIES=/Users/martin/Dev/jzy3d/external/osmesa/lib/libGL.dylib
 * DYLD_INSERT_LIBRARIES=/opt/homebrew/Cellar/mesa/21.3.7/lib/libGL.dylib
 * DYLD_LIBRARY_PATH=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-arm64:${env_var:DYLD_LIBRARY_PATH}
 * DYLD_PRINT_BINDINGS=YES DYLD_PRINT_LIBRARIES=YES LIBGL_ALWAYS_SOFTWARE=true
 */
public class DemoVTKVersatileCanvas {
  static String WINDOWS_MESA_PATH_RELATIVE = ".\\lib\\9.1.0\\mesa-Windows-x86_64";
  static String MACOS_MESA_PATH_RELATIVE = "./lib/9.1.0/mesa-Darwin-x86_64";

  static {
    configureMesaPathProperty();

    VTKVersatileCanvas.loadNativesFor(Chip.GPU);
  }

  protected static void configureMesaPathProperty() {
    if (OS.isMac()) {
      System.setProperty("mesa.path", new File(MACOS_MESA_PATH_RELATIVE).getAbsolutePath());
    } else if (OS.isWindows()) {
      System.setProperty("mesa.path", new File(WINDOWS_MESA_PATH_RELATIVE).getAbsolutePath());
    } else if (OS.isUnix()) {
      // assume all unix use mesa and do not require loading it explicitely
    }
  }

  public static void main(String[] args) {
    // makeVersatileCanvasDemo();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        makeVersatileCanvasDemo();

        System.out.println("Use C key to switch to CPU rendering using MESA OpenGL library");
        System.out
            .println("Use G key to switch to GPU rendering using Operating System OpenGL library");
        System.out.println("(may not work on Windows)");
        System.out.println(
            "Force start of program on either CPU or GPU with : static { VTKVersatileCanvas.loadNativesFor(Chip.CPU); }");

      }
    });
  }

  static JFrame frame;

  protected static void makeVersatileCanvasDemo() {
    // Versatile Canvas
    VTKVersatileCanvas canvas = new VTKVersatileCanvas();

    // Embedding GUI with auto-reload hability
    frame = newSceneAndFrame(canvas);
  }


  /**
   * Allow re-initializing frame
   */
  protected static OnChipSwitch newOnSwitch(VTKVersatileCanvas canvas) {
    return new OnChipSwitch() {
      @Override
      public void preSwitch() {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
      }

      @Override
      public void postSwitch() {
        frame = newSceneAndFrame(canvas);
      }
    };
  }

  protected static JFrame newSceneAndFrame(VTKVersatileCanvas canvas) {

    // New scene, rebuilt as soon as we change Chip
    newSceneContent(canvas);

    // New frame
    JFrame frame = newFrame(canvas);

    // Add key listener to the canvas
    newKeyChipSwitch(canvas);

    return frame;
  }


  /**
   * Configure a frame
   * 
   * @param canvas
   */
  protected static JFrame newFrame(VTKVersatileCanvas canvas) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(canvas.getCanvas().getComponent());
    frame.setSize(600, 400);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

    canvas.addListener(new Listener() {
      @Override
      public void onFirstRender(VTKVersatileCanvas canvas) {
        frame.setTitle(
            "Chip queried: " + canvas.getQueriedChip() + " - actual: " + canvas.getActualChip());
      }
    });
    return frame;
  }

  /**
   * Configure a canvas key listener able to switch CPU/GPU
   * 
   * @param canvas
   */
  protected static void newKeyChipSwitch(VTKVersatileCanvas canvas) {
    canvas.getCanvas().getComponent().addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
        // RESET
        if (e.getKeyChar() == 'r') {
          canvas.getCanvas().resetCamera();
        }

        // CPU
        else if (e.getKeyChar() == 'c') {
          canvas.switchTo(Chip.CPU, newOnSwitch(canvas));
        }

        // GPU
        else if (e.getKeyChar() == 'g') {
          canvas.switchTo(Chip.GPU, newOnSwitch(canvas));
        }

        // EXIT
        else if (e.getKeyChar() == 'q') {
          System.exit(0);
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {}

      @Override
      public void keyPressed(KeyEvent e) {}
    });
  }

  /**
   * Build scene content inside the given canvas
   * 
   * @param canvas
   */
  protected static void newSceneContent(VTKVersatileCanvas canvas) {
    // Scene content
    vtkConeSource cone = new vtkConeSource();
    cone.SetResolution(8);

    vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
    coneMapper.SetInputConnection(cone.GetOutputPort());

    vtkActor actor = new vtkActor();
    actor.SetMapper(coneMapper);

    canvas.getCanvas().getRenderer().AddActor(actor);
  }
}
