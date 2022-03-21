package vtk.rendering.jogl.chip;

import org.apache.log4j.Logger;
import vtk.rendering.jogl.Environment;
import vtk.rendering.jogl.OS;

/**
 * A class allowing to configure path and other env. variable to configure GPU or CPU rendering on
 * demand.
 * 
 * Switching GPU/CPU rendering may not be supported on some OS (e.g. Windows).
 * 
 * <h2>CPU rendering configuration</h2>
 * 
 * A path to the mesa library should be provided either by
 * <ul>
 * <li>invoking <code>new ChipSelector("/path/to/mesa/folder/");</code>
 * <li>configuring the environment variable mesa.path through a call to <code>System.setProperty("mesa.path", "/path/to/mesa/folder/");</code>
 * <li>configuring the environment variable mesa.path through the JVM argument <code>-Dmesa.path="/path/to/mesa/folder/"</code>
 * </ul>
 * 
 * <h2>GPU rendering configuration</h2>
 * 
 * The path to the system OpenGL library is expected to be
 * 
 * <ul>
 * <li>C:/Windows/System32/opengl32.dll on Windows</li>
 * <li>/System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/libGL.dylib on macOS</li>
 * <li>Already in the path in Linux
 * </ul>
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
  
  protected static String OPENGL_SYSTEM_PATH_MACOS =
      "/System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/";
  protected static String OPENGL_LIB_MACOS = "libGL.dylib";
  protected static String OPENGL_SYSTEM_PATH_WINDOWS = "C:\\Windows\\System32\\";
  protected static String OPENGL_LIB_WINDOWS = "opengl32.dll";

  protected static Logger log = Logger.getLogger(ChipSelector.class);

  protected Environment env = new Environment();
  protected Chip queriedChip;
  protected String mesaPath = "";
  protected boolean debug = false;

  public ChipSelector() {
    this(getMesaPathValue());
  }

  protected static String getMesaPathValue() {
    String path = System.getProperty(MESA_PATH_PROPERTY_NAME);

    if (path != null && !"".equals(path))
      return path;
    throw new IllegalArgumentException(
        "Either define MESA path through " + MESA_PATH_PROPERTY_NAME + " property, or invoke "
            + ChipSelector.class.getSimpleName() + " with a valid path to a MESA installation.");
  }


  /**
   * 
   * @param mesaPath is required for Windows to allow adding Mesa to the PATH variable before
   *        system32.
   */
  public ChipSelector(String mesaPath) {
    this.mesaPath = mesaPath;
  }

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

      // loadOpenGLMac_MesaLibrary();

    }

    // ------------------------------
    // Error

    else
      throw new RuntimeException("Unsupported " + chip);
  }


  protected void loadOpenGLMac_System() {
    String path = getOpenGLPath_MacOS_System();
    log.debug("Try loading MacOS System GL " + path);
    System.load(path);
  }

  protected void loadOpenGLMac_MesaLibrary() {
    String path = getOpenGLPath_MacOS_Mesa();
    log.debug("Try loading MESA GL " + path);
    System.load(path);
  }

  protected String getOpenGLPath_MacOS_System() {
    return OPENGL_SYSTEM_PATH_MACOS + OPENGL_LIB_MACOS;
  }

  protected String getOpenGLPath_MacOS_Mesa() {
    return mesaPath + "/" + OPENGL_LIB_MACOS;
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

      env.appendFirst("PATH", mesaPath, ";");

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

  // unload :
  // https://web.archive.org/web/20140704120535/http://www.codethesis.com/blog/unload-java-jni-dll
  /*
   * public static void init(Chip chip) {
   * 
   * if (isWindows()) { if (Chip.CPU.equals(chip)) loadOpenGLMesa(); else if (Chip.GPU.equals(chip))
   * loadOpenGLWindows(); else throw new RuntimeException("Unsupported " + chip); } }
   */

  /*
   * protected void loadOpenGL() { System.loadLibrary("opengl32"); }
   */

  protected void loadOpenGLWindows_System() {
    String path = getOpenGLPath_Windows_System();
    log.debug("Try loading Windows GL " + path);
    System.load(path);
  }

  protected void loadOpenGLWindows_MesaLibrary() {
    String path = getOpenGLPath_Windows_Mesa();
    log.debug("Try loading MESA GL " + path);
    System.load(path);
  }

  protected String getOpenGLPath_Windows_System() {
    return OPENGL_SYSTEM_PATH_WINDOWS + OPENGL_LIB_WINDOWS;
  }

  protected String getOpenGLPath_Windows_Mesa() {
    return mesaPath + "/" + OPENGL_LIB_WINDOWS;
  }

  /*****************************************************/


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
