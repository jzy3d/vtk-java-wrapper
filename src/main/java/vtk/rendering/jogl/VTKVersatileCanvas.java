package vtk.rendering.jogl;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import vtk.VTKUtils;
import vtk.vtkGenericOpenGLRenderWindow;
import vtk.rendering.jogl.ChipSelector.Chip;

/**
 * Add several enhancements to the standard VTK Panels.
 * 
 * <ul>
 * <li>Canvas can be removed and re-added to a frame without crashing
 * <li>Canvas can be rendered with CPU or GPU
 * </ul>
 * 
 * @author Martin
 *
 */
public class VTKVersatileCanvas {
  Logger log = Logger.getLogger(VTKVersatileCanvas.class);

  /**
   * Configure environment variable as it is expected on the target OS for the expected rendering
   * chip.
   * 
   * CPU will require
   * <ul>
   * <li>On Linux : To edit the LIBGL_ALWAYS_SOFTWARE variable to value "true"
   * <li>On MacOS : To edit LIBGL_ALWAYS_SOFTWARE variable to value "true"
   * <li>On Windows : To edit the PATH variable so that MESA library path appears before System32
   * path.
   * </ul>
   * 
   * GPU will require
   * <ul>
   * <li>On Linux : To edit the LIBGL_ALWAYS_SOFTWARE variable to value "false"
   * <li>On MacOS : To edit LIBGL_ALWAYS_SOFTWARE variable to value "false"
   * <li>On Windows : To edit the PATH variable so that MESA library path disappear from path.
   * </ul>
   * 
   * Then performs loading of all VTK libraries.
   * 
   * @param chip
   */
  public static void loadNativesFor(Chip chip) {
    defaultChip = chip;

    ChipSelector selector = new ChipSelector();
    selector.use(chip);

    VTKUtils.loadVtkNativeLibraries();
  }

  protected static Chip defaultChip;

  /*********************************************************/

  protected vtkAbstractJoglComponent<?> canvas;
  protected boolean hasRenderedOnce;
  protected Chip actualChip;
  protected Chip queriedChip;

  public VTKVersatileCanvas() {
    init(defaultChip);
  }

  protected void init(Chip chip) {
    queriedChip = chip;

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

    // canvas = new VTKRemoveableCanvasAWT(window, capabilities);
    canvas = new VTKRemoveableCanvasSwing(window, capabilities);

    // Disable multisampling that is not supported by MESA
    if (Chip.CPU.equals(queriedChip))
      canvas.getRenderWindow().SetMultiSamples(0);

    // enable a listener to get notified of actual chip usage
    listeners = new ArrayList<Listener>();

    initActualChipRetriever();
  }

  protected void initActualChipRetriever() {
    hasRenderedOnce = false;

    final Runnable reportCallback = new Runnable() {
      public void run() {
        if (hasRenderedOnce == false) {
          Object[] lines = canvas.getRenderWindow().ReportCapabilities().lines().toArray();

          actualChip = Chip.GPU;
          for (int i = 0; i < 3; i++) {
            String reportLine = (String) lines[i];
            if (reportLine.contains("llvm")) {
              actualChip = Chip.CPU;
            }
            log.debug(reportLine);
          }

          // System.out.println("JOGL is now using : " + actualChip);

          hasRenderedOnce = true;

          fireOnFirstRender();
        }
      }
    };

    canvas.getRenderWindow().AddObserver("RenderEvent", reportCallback, "run");
  }

  /****************************************/

  public static interface Listener {
    public void onFirstRender(VTKVersatileCanvas canvas);
  }

  protected List<Listener> listeners = new ArrayList<Listener>();

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  protected void fireOnFirstRender() {
    for (Listener listener : listeners) {
      listener.onFirstRender(this);
    }
  }

  /****************************************/

  protected void clean() {
    listeners.clear();
    listeners = null;
    canvas = null;
    System.gc();
  }

  /**
   * 
   * After performing customizable pre-switch action through the {@link OnChipSwitch#preSwitch()}
   * callback, this method will :
   * <ul>
   * <li>Apply the environment variable changes required for each OS to be able to change the GL
   * library to either native (system) or software (mesa).</li>
   * <li>After changing these settings, resources held by this class will be deleted and
   * {@link System.gc()} call will be performed to unload the existing GL library and hence allow
   * replacing with the new one.</li>
   * <li>Once unloading is successful, the canvas gets re-initialized in order to reload the
   * appropriate GL library.</li>
   * </ul>
   * 
   * Once everything is loaded, the {@link OnChipSwitch#postSwitch()} callback is called.
   * 
   * @param chip
   * @param onswitch
   */
  public void switchTo(Chip chip, OnChipSwitch onswitch) {
    // Release parent container
    onswitch.preSwitch();

    // Reconfigure environment to allow selecting good chip
    ChipSelector s = new ChipSelector();
    s.use(chip);

    // Call GC to unload natives
    clean();

    // Initialize JOGL components
    init(chip);

    // Rebuild parent container and
    onswitch.postSwitch();
  }

  public static interface OnChipSwitch {
    public void preSwitch();

    public void postSwitch();
  }


  public vtkAbstractJoglComponent<?> getCanvas() {
    return canvas;
  }

  public Chip getActualChip() {
    return actualChip;
  }

  public Chip getQueriedChip() {
    return queriedChip;
  }
}
