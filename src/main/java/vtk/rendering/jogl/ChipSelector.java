package vtk.rendering.jogl;

public class ChipSelector {
  public enum Chip {
    CPU, GPU
  }

  protected Environment env = new Environment();

  protected Chip queriedChip;


  static String DEFAULT_MESA_PATH =
      "C:\\Users\\Martin\\Dev\\jzy3d\\private\\vtk-java-wrapper\\lib\\9.1.0\\mesa-Windows-x86_64";
  protected String mesaPath = "";
  protected boolean debug = false;

  public ChipSelector() {
    this(DEFAULT_MESA_PATH);
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

    if (debug) {
      System.out.println("--------------------");
      System.out.println("WANT TO USE : " + chip);
    }

    // WIndows
    if (OS.isWindows()) {
      useOnWindows(chip);
    }

    // WIndows
    if (OS.isMac()) {
      useOnMac(chip);
    }

    // Linux
    else if(OS.isUnix()){
      useOnLinux(chip);
    }
  }

  /*****************************************************/
  /**                                                  */
  /** LINUX */
  /**                                                  */
  /*****************************************************/

  public static String LINUX_ENV_VAR = "LIBGL_ALWAYS_SOFTWARE";

  protected void useOnLinux(Chip chip) {

    // ------------------------------
    // CPU configuration

    if (Chip.CPU.equals(chip)) {
      env.set(LINUX_ENV_VAR, "true");
    }

    // ------------------------------
    // GPU configuration

    else if (Chip.GPU.equals(chip)) {
      env.set(LINUX_ENV_VAR, "false");
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


  protected void useOnMac(Chip chip) {

    // ------------------------------
    // CPU configuration

    if (Chip.CPU.equals(chip)) {
      env.set(LINUX_ENV_VAR, "true");
      
      loadOpenGLMac_MesaLibrary();

    }

    // ------------------------------
    // GPU configuration

    else if (Chip.GPU.equals(chip)) {
      env.set(LINUX_ENV_VAR, "false");
      
      //loadOpenGLMac_MesaLibrary();

    }

    // ------------------------------
    // Error

    else
      throw new RuntimeException("Unsupported " + chip);
  }
  
  /*protected void loadOpenGLMac_System() {
    String path = "C:\\Windows\\System32\\opengl32.dll";
    if (debug)
      System.out.println("Try loading Windows GL " + path);
    System.load(path);
  }*/

  protected void loadOpenGLMac_MesaLibrary() {
    mesaPath = "/opt/homebrew/Cellar/mesa/21.3.7/lib";
    
    String path = mesaPath + "/libGL.dylib";
    if (debug)
      System.out.println("Try loading MESA GL " + path);
    System.load(path);
  }


  /*****************************************************/
  /**                                                  */
  /** WINDOWS */
  /**                                                  */
  /*****************************************************/


  protected void useOnWindows(Chip chip) {
    env.set("TEST_ENV_VAR", "toto");

    if (debug)
      System.out.println("TEST_ENV_VAR:" + env.get("TEST_ENV_VAR"));

    String oldpath = env.get("PATH");

    if (debug)
      System.out.println("oldpath : " + oldpath);

    // ------------------------------
    // CPU configuration

    if (Chip.CPU.equals(chip)) {

      env.set("PATH", mesaPath + ";" + oldpath);

      if (debug)
        System.out.println("newpath : " + env.get("PATH"));

      loadOpenGLWindows_MesaLibrary();

    }

    // ------------------------------
    // GPU configuration

    else if (Chip.GPU.equals(chip)) {

      String newpath = oldpath.replace(mesaPath, "");
      newpath = newpath.replace(";;", ";");
      // TODO : avec et sans slash final
      // TODO : faire slash et backslash

      env.set("PATH", newpath);

      if (debug)
        System.out.println("newpath : " + env.get("PATH"));

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

  protected void loadOpenGL() {
    System.loadLibrary("opengl32");
  }

  protected void loadOpenGLWindows_System() {
    String path = "C:\\Windows\\System32\\opengl32.dll";
    if (debug)
      System.out.println("Try loading Windows GL " + path);
    System.load(path);
  }

  protected void loadOpenGLWindows_MesaLibrary() {
    String path = mesaPath + "/opengl32.dll";
    if (debug)
      System.out.println("Try loading MESA GL " + path);
    System.load(path);
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
