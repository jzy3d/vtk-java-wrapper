package vtk.rendering.jogl.chip;

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtk.VTKUtils;
import vtk.rendering.jogl.Environment;
import vtk.rendering.jogl.OS;

/**
 * A class allowing to configure path and other environment variable to configure GPU or CPU
 * rendering on demand.
 * 
 * Switching GPU/CPU rendering without restarting the JVM may not be supported on some OS (e.g.
 * Windows).
 * 
 * <h2>CPU rendering configuration</h2>
 * 
 * A path to the mesa library should be provided either by
 * <ul>
 * <li>invoking <code>new ChipSelector("/path/to/mesa/folder/");</code>
 * <li>configuring the environment variable mesa.path through a call to
 * <code>System.setProperty("mesa.path", "/path/to/mesa/folder/");</code>
 * <li>configuring the environment variable mesa.path through the JVM argument
 * <code>-Dmesa.path="/path/to/mesa/folder/"</code>
 * </ul>
 * 
 * <h2>GPU rendering configuration</h2>
 * 
 * The path to the system OpenGL library is expected to be
 * 
 * <ul>
 * <li>%PATH%/opengl32.dll on Windows, which may be C:/Windows/System32/</li>
 * <li>/System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/libGL.dylib on macOS</li>
 * <li>Already in the path in Linux
 * </ul>
 * 
 * If these default settings aren't appropriate and if you need to override this, use the following
 * system properties
 * <ul>
 * <li>opengl.windows.path
 * <li>opengl.macos.path
 * </ul>
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * <h2>Useful notes for debugging on MacOS</h2>
 * 
 * <h4>dyld</h4>
 * 
 * <ul>
 * <li>DYLD_PRINT_LIBRARIES=YES env var on macOS will let dyld print all library as soon as they are
 * loaded.
 * <li>DYLD_PRINT_BINDINGS=YES
 * <li>DYLD_INSERT_LIBRARIES=/usr/local/Cellar/mesa/21.1.2/lib/libGL.dylib to force a lib to be
 * loaded before the other
 * <li>DYLD_INSERT_LIBRARIES=/opt/homebrew/Cellar/mesa/21.3.7/lib/libGL.dylib
 * <li>DYLD_LIBRARY_PATH=/usr/local/Cellar/mesa/21.1.2/lib:/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-x86_64:${env_var:DYLD_LIBRARY_PATH}
 * </ul>
 * 
 * man dyld to get documentation on these env var.
 * https://stackoverflow.com/questions/51504439/what-environment-variables-control-dyld
 * 
 * 
 * <h4>otool</h4>
 * 
 * 
 * @author Martin Pernollet
 *
 */
public class ChipSelector {
  public static final String MESA_PATH_PROPERTY_NAME = "mesa.path";
  public static final String MESA_CPU_RENDERING_ENV_VAR = "LIBGL_ALWAYS_SOFTWARE";

  public static final String OPENGL_LIB_PATH_WINDOWS_PROPERTY_NAME = "opengl.windows.path";
  public static final String OPENGL_LIB_PATH_MACOS_PROPERTY_NAME = "opengl.macos.path";


  protected static String OPENGL_LIB_MACOS = "GL"; // libGL.dylib
  protected static String OPENGL_LIB_WINDOWS = "opengl32"; // opengl32.dll


  protected static Logger log = LogManager.getLogger(ChipSelector.class);


  protected Environment env = new Environment();
  protected Chip queriedChip;
  protected String mesaPath = "";
  protected String openglPathWindows = "";
  protected String openglPathMacOS = "";



  public ChipSelector() {
    this(getMesaPathFromJVMProperty());
  }

  /**
   * 
   * @param mesaPath is required for Windows to allow adding Mesa to the PATH variable before
   *        system32.
   */
  public ChipSelector(String mesaPath) {
    this(fixPath(mesaPath), getOpenGLPathWindows(), getOpenGLPathMacOS());
  }

  public ChipSelector(String mesaPath, String windowsPath, String macOSPath) {
    this.mesaPath = fixPath(mesaPath);
    this.openglPathMacOS = fixPath(macOSPath);
    this.openglPathWindows = fixPath(windowsPath);
  }

  protected static String fixPath(String path) {
    if (path == null) {
      return null;
    }

    if (OS.isWindows()) {
      return path.replace("/", File.separator);
    } else {
      return path.replace("\\", File.separator);
    }
  }

  protected static String getMesaPathFromJVMProperty() {
    if (OS.isUnix())
      return "";

    String path = System.getProperty(MESA_PATH_PROPERTY_NAME);

    if (path != null && !"".equals(path))
      return path;
    else {
      String warning = "Either define MESA path through " + MESA_PATH_PROPERTY_NAME
          + " property, or invoke " + ChipSelector.class.getSimpleName()
          + " constructor with a valid path to a MESA installation, otherwise you can not enable CPU rendering.";
      log.warn(warning);
      return null;
    }
  }

  protected static String getOpenGLPathWindows() {
    String path = System.getProperty(OPENGL_LIB_PATH_WINDOWS_PROPERTY_NAME);

    if (isDefined(path))
      return path;
    else
      return null;
  }
  
  //protected static final String OPENGL_SYSTEM_PATH_WINDOWS_DEFAULT = "C:\\Windows\\System32\\";


  protected static String getOpenGLPathMacOS() {
    String path = System.getProperty(OPENGL_LIB_PATH_MACOS_PROPERTY_NAME);

    if (isDefined(path))
      return path;
    else
      return null;
  }
  
  protected static final String OPENGL_SYSTEM_PATH_MACOS_DEFAULT =
  "/System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/";

  /*****************************************************/
  /**                                                  */
  /** CHIP SELECT API */
  /**                                                  */
  /*****************************************************/



  public void use(Chip chip) {
    queriedChip = chip;

    log.debug("Select chip : " + chip);

    // WIndows
    if (OS.isWindows()) {
      // configureMesaEnvironmentVariable(chip);
      configureWindowsPathWithMesaOrNotAndLoadGL(chip);
    }

    // WIndows
    if (OS.isMac()) {
      configureMacOSPathWithMesaOrNotAndLoadGL(chip);
    }

    // Linux
    else if (OS.isUnix()) {
      configureMesaEnvironmentVariable(chip);
    }
  }

  /*****************************************************/
  /**                                                  */
  /** LINUX */
  /**                                                  */
  /*****************************************************/


  protected void configureMesaEnvironmentVariable(Chip chip) {
    log.debug("unix config starting");

    // ------------------------------
    // CPU configuration

    if (Chip.CPU.equals(chip)) {
      env.set(MESA_CPU_RENDERING_ENV_VAR, "true");
      log.debug(
          MESA_CPU_RENDERING_ENV_VAR + " is now set to " + env.get(MESA_CPU_RENDERING_ENV_VAR));
    }

    // ------------------------------
    // GPU configuration

    else if (Chip.GPU.equals(chip)) {
      env.set(MESA_CPU_RENDERING_ENV_VAR, "false");
      log.debug(
          MESA_CPU_RENDERING_ENV_VAR + " is now set to " + env.get(MESA_CPU_RENDERING_ENV_VAR));
    }

    // ------------------------------
    // Error

    else
      throw new RuntimeException("Unsupported " + chip);
  }

  /*****************************************************/
  /**                                                  */
  /** MAC */
  /**                                                  */
  /*****************************************************/


  protected void configureMacOSPathWithMesaOrNotAndLoadGL(Chip chip) {
    log.debug("macOS config starting");

    // ------------------------------
    // CPU configuration

    if (Chip.CPU.equals(chip)) {
      env.set(MESA_CPU_RENDERING_ENV_VAR, "true");
      log.debug(
          MESA_CPU_RENDERING_ENV_VAR + " is now set to " + env.get(MESA_CPU_RENDERING_ENV_VAR));

      loadOpenGLMac_MesaLibrary();

    }

    // ------------------------------
    // GPU configuration

    else if (Chip.GPU.equals(chip)) {
      env.set(MESA_CPU_RENDERING_ENV_VAR, "false");
      log.debug(
          MESA_CPU_RENDERING_ENV_VAR + " is now set to " + env.get(MESA_CPU_RENDERING_ENV_VAR));

      //loadOpenGLMac_System();

    }

    // ------------------------------
    // Error

    else
      throw new RuntimeException("Unsupported " + chip);
  }


  protected void loadOpenGLMac_System() {
    String path = getOpenGLPath_MacOS_System();

    if (isDefined(path)) {
      log.debug("Try loading MacOS System GL by fullpath : " + path);
      System.load(path);
    } else {
      // Not working yet
      log.debug("Try loading MacOS System GL by name : " + OPENGL_LIB_MACOS);
      System.loadLibrary(OPENGL_LIB_MACOS);

      // fallback on explicit default path
      //String defaultPath = getPathAndLibWithSeparator(OPENGL_SYSTEM_PATH_MACOS_DEFAULT, System.mapLibraryName(OPENGL_LIB_MACOS));
      //System.load(defaultPath);
    }
  }

  protected void loadOpenGLMac_MesaLibrary() {
    String path = getOpenGLPath_MacOS_Mesa();
    log.debug("Try loading MESA GL " + path);
    System.load(path);
  }

  protected String getOpenGLPath_MacOS_System() {
    // if a system path is given, returns a full path
    if (openglPathMacOS != null) {
      return getPathAndLibWithSeparator(openglPathMacOS, System.mapLibraryName(OPENGL_LIB_MACOS));
    }
    // otherwise null to load lib by name
    else {
      return null;
    }
  }

  protected String getOpenGLPath_MacOS_Mesa() {
    return getPathAndLibWithSeparator(mesaPath, System.mapLibraryName(OPENGL_LIB_MACOS));
  }

  /*****************************************************/
  /**                                                  */
  /** WINDOWS */
  /**                                                  */
  /*****************************************************/


  protected void configureWindowsPathWithMesaOrNotAndLoadGL(Chip chip) {
    log.debug("Windows config starting");

    String oldpath = env.get("PATH");

    log.debug("oldpath : " + oldpath);

    // ------------------------------
    // CPU configuration

    if (Chip.CPU.equals(chip)) {

      env.appendFirst("PATH", mesaPath, File.pathSeparator);

      log.debug("newpath : " + env.get("PATH"));

      loadOpenGLWindows_MesaLibrary();

    }

    // ------------------------------
    // GPU configuration

    else if (Chip.GPU.equals(chip)) {

      env.removeFrom("PATH", mesaPath);
      /*
       * String newpath = oldpath.replace(mesaPath, ""); newpath = newpath.replace(";;", ";"); //
       * TODO : avec et sans slash final // TODO : faire slash et backslash env.set("PATH",
       * newpath);
       */

      log.debug("newpath : " + env.get("PATH"));

      loadOpenGLWindows_System();
    }

    // ------------------------------
    // Error

    else
      throw new RuntimeException("Unsupported " + chip);
  }

  protected void loadOpenGLWindows_System() {
    String path = getOpenGLPath_Windows_System();

    if (isDefined(path)) {
      log.debug("Try loading Windows GL by fullpath : " + path);
      System.load(path);
    } else {
      log.debug("Try loading Windows GL by name : " + OPENGL_LIB_WINDOWS);

      try {
        System.loadLibrary(OPENGL_LIB_WINDOWS);
      } catch (Exception e) {
        e.printStackTrace();
        VTKUtils.pathConfigurationReport();
        throw e;
      }
    }
  }

  protected void loadOpenGLWindows_MesaLibrary() {
    String path = getOpenGLPath_Windows_Mesa();
    log.debug("Try loading MESA GL by fullpath : " + path);
    System.load(path);
  }

  protected String getOpenGLPath_Windows_System() {
    if (openglPathWindows != null) {
      return getPathAndLibWithSeparator(openglPathWindows,
          System.mapLibraryName(OPENGL_LIB_WINDOWS));
    } else {
      return null;
    }
  }

  protected String getOpenGLPath_Windows_Mesa() {
    return getPathAndLibWithSeparator(mesaPath, System.mapLibraryName(OPENGL_LIB_WINDOWS));
  }

  /*****************************************************/

  protected static boolean isDefined(String path) {
    return path != null && !"".equals(path);
  }

  protected String getPathAndLibWithSeparator(String path, String libName) {
    if (path.endsWith(File.separator)) {
      return path + libName;
    } else {
      return path + File.separator + libName;
    }
  }


  public Chip getQueriedChip() {
    return queriedChip;
  }


  public void setQueriedChip(Chip queriedChip) {
    this.queriedChip = queriedChip;
  }


  public String getMesaPath() {
    return mesaPath;
  }

  public void setMesaPath(String mesaPath) {
    this.mesaPath = mesaPath;
  }

  /** Able to get/set environment variables on each OS */
  public Environment getEnv() {
    return env;
  }
}
