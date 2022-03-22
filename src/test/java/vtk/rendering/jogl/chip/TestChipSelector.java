package vtk.rendering.jogl.chip;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import vtk.rendering.jogl.Environment;
import vtk.rendering.jogl.OS;

public class TestChipSelector {
  static String WINDOWS_MESA_PATH_RELATIVE = ".\\lib\\9.1.0\\mesa-Windows-x86_64";
  public static File WINDOWS_MESA_PATH = new File(WINDOWS_MESA_PATH_RELATIVE);

  static String MACOS_MESA_PATH_RELATIVE = ".\\lib\\9.1.0\\mesa-Darwin-x86_64";
  public static File MACOS_MESA_PATH = new File(MACOS_MESA_PATH_RELATIVE);

  @Test
  public void whenWindows_ThenEditPath() {
    if (!OS.isWindows()) {
      System.err.println("Not running Windows test on this computer");
      return;
    }


    ChipSelector selector = new ChipSelector(WINDOWS_MESA_PATH.getAbsolutePath());
    String mesaPath = selector.getMesaPath();

    // When using CPU, can find MESA in Path
    selector.use(Chip.CPU);

    Assert.assertTrue(getEnv("PATH").contains(mesaPath));

    // When using GPU, can't find MESA in Path
    selector.use(Chip.GPU);
    // System.out.println(System.getenv("PATH"));
    Assert.assertFalse(getEnv("PATH").contains(mesaPath));

  }

  @Test
  public void whenUnix_ThenEditMesaEnvVar() {
    if (!OS.isUnix()) {
      System.err.println("Not running Unix test on this computer");
      return;
    }

    ChipSelector selector = new ChipSelector();

    // When using CPU, Mesa env variable is set for CPU rendering
    selector.use(Chip.CPU);
    Assert.assertEquals("true", getEnv(ChipSelector.MESA_CPU_RENDERING_ENV_VAR));

    // When using GPU, Mesa env variable is set for CPU rendering
    selector.use(Chip.GPU);
    Assert.assertEquals("false", getEnv(ChipSelector.MESA_CPU_RENDERING_ENV_VAR));

  }

  @Test
  public void whenMacOS_ThenEditMesaEnvVar() {
    if (!OS.isMac()) {
      System.err.println("Not running MacOS test on this computer");
      return;
    }

    ChipSelector selector = new ChipSelector();

    // When using CPU, Mesa env variable is set for CPU rendering
    selector.use(Chip.CPU);
    Assert.assertEquals("true", getEnv(ChipSelector.MESA_CPU_RENDERING_ENV_VAR));

    // When using GPU, Mesa env variable is set for CPU rendering
    selector.use(Chip.GPU);
    Assert.assertEquals("false", getEnv(ChipSelector.MESA_CPU_RENDERING_ENV_VAR));

  }

  ////////////////////////////////////////////////////////


  @Test(expected = IllegalArgumentException.class)
  public void whenChipSelectMissMesaConfig_ThrowException() {
    System.setProperty("mesa.path", "");

    System.out.println("For info " + ChipSelector.MESA_PATH_PROPERTY_NAME + "="
        + System.getProperty(ChipSelector.MESA_PATH_PROPERTY_NAME));
    
    // If no argument given, neither a system property is defined, an exception is expected
    ChipSelector c = new ChipSelector();
  }

  @Test
  public void whenChipSelectConfigureMesaPath_Windows() {
    if(!OS.isWindows()) {
      System.err.println("Do not execute this test for Windows");
      return;
    }
    
    // Given path to MESA given explicitely
    ChipSelector c = new ChipSelector("/path/to/mesa");

    // When getting path 
    Assert.assertEquals("\\path\\to\\mesa\\opengl32.dll", c.getOpenGLPath_Windows_Mesa());
    
    // Given path to MESA given explicitely, with a trailing separator
    ChipSelector c2 = new ChipSelector("/path/to/mesa/");

    // When getting path 
    Assert.assertEquals("\\path\\to\\mesa\\opengl32.dll", c2.getOpenGLPath_Windows_Mesa());
    

    // Given path to MESA given through env variable
    System.setProperty("mesa.path", "/path/to/mesa");
    ChipSelector c3 = new ChipSelector();

    // When getting path 
    Assert.assertEquals("\\path\\to\\mesa\\opengl32.dll", c3.getOpenGLPath_Windows_Mesa());
  }
  
  @Test
  public void whenChipSelectConfigureMesaPath_MacOS() {
    if(!OS.isMac()) {
      System.err.println("Do not execute this test for macOS");
      return;
    }
    
    // Given path to MESA given explicitely
    ChipSelector c = new ChipSelector("/path/to/mesa");

    // When getting path on mac
    Assert.assertEquals("/path/to/mesa/libGL.dylib", c.getOpenGLPath_MacOS_Mesa());

    // Given path to MESA given explicitely
    ChipSelector c2 = new ChipSelector("/path/to/mesa/");

    // When getting path on mac
    Assert.assertEquals("/path/to/mesa/libGL.dylib", c2.getOpenGLPath_MacOS_Mesa());

    // Given path to MESA given through env variable
    System.setProperty("mesa.path", "/path/to/mesa");
    ChipSelector c3 = new ChipSelector();

    // When getting path on mac
    Assert.assertEquals("/path/to/mesa/libGL.dylib", c3.getOpenGLPath_MacOS_Mesa());
  }

  @Test
  public void whenFixPath() {
    String pathWithSlash = "/path/with/wrong/separator";
    String pathWithAntislash = "\\path\\with\\wrong\\separator";
    
    if(OS.isWindows()) {
      // Given a path with slash on Windows
      ChipSelector c = new ChipSelector(pathWithSlash);
      String mesaPath = c.getOpenGLPath_Windows_Mesa();
      
      // Then path is fixed with antislashes
      Assert.assertTrue(mesaPath.startsWith(pathWithAntislash));
    }
    else if(OS.isMac()){
      // Given a path with antislash on mac
      ChipSelector c = new ChipSelector(pathWithAntislash);
      String mesaPath = c.getOpenGLPath_MacOS_Mesa();
      
      // Then path is fixed with slashes
      Assert.assertTrue(mesaPath.startsWith(pathWithSlash));

    }
  }
  
  @Test
  public void configureGLSystemPath_MacOS() {
    if(!OS.isMac()) {
      System.err.println("Do not execute this test for macOS");
      return;
    }
    
    
    // Given path to MESA given through env variable
    System.setProperty("mesa.path", "/path/to/mesa");
    System.setProperty("opengl.macos.path", "/path/to/gl");
    ChipSelector c3 = new ChipSelector();

    // When getting path on mac
    Assert.assertEquals("/path/to/mesa/libGL.dylib", c3.getOpenGLPath_MacOS_Mesa());
    Assert.assertEquals("/path/to/gl/libGL.dylib", c3.getOpenGLPath_MacOS_System());
  }
  
  @Test
  public void configureGLSystemPath_Windows() {
    if(!OS.isWindows()) {
      System.err.println("Do not execute this test for windows");
      return;
    }
    
    
    // Given path to MESA given through env variable
    System.setProperty("mesa.path", "/path/to/mesa");
    System.setProperty("opengl.windows.path", "/path/to/gl");
    ChipSelector c3 = new ChipSelector();

    // When getting path on mac
    Assert.assertEquals("\\path\\to\\mesa\\opengl32.dll", c3.getOpenGLPath_Windows_Mesa());
    Assert.assertEquals("\\path\\to\\gl\\opengl32.dll", c3.getOpenGLPath_Windows_System());
  }

  ////////////////////////////////////////////////////////

  /**
   * Helper to retrieve an environment variable whatever the OS. Working better than System.getenv
   * on windows since System.getenv seams to return the state of the environment variable when
   * program was started, not the state of the variable NOW.
   */
  public static String getEnv(String variable) {
    Environment env = new Environment();
    return env.get(variable);
  }
}
