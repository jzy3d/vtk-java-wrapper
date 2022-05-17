package vtk.rendering.jogl.chip;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import vtk.VTKUtils;
import vtk.rendering.jogl.Environment;
import vtk.rendering.jogl.OS;

public class TestChipSelector {
  private static final String TEST_ON_WINDOWS_BYPASSED = "Not running Windows test on this computer (" + OS.name() + ")";
  private static final String TEST_ON_MACOS_BYPASSED = "Not running MacOS test on this computer (" + OS.name() + ")";
  private static final String TEST_ON_UNIX_BYPASSED = "Not running Unix test on this computer (" + OS.name() + ")";
  
  private static final String WINDOWS_MESA_PATH_RELATIVE = ".\\lib\\9.1.0\\mesa-Windows-x86_64";
  private static final String MACOS_MESA_PATH_RELATIVE = ".\\lib\\9.1.0\\mesa-Darwin-x86_64";

  public static final File WINDOWS_MESA_PATH = new File(WINDOWS_MESA_PATH_RELATIVE);
  public static final File MACOS_MESA_PATH = detectMesaMacOS();
  
  public static File detectMesaMacOS() {
    File local = new File(MACOS_MESA_PATH_RELATIVE);
    
    if(local.exists())
      return local;
    
    // try homebrew on macos 11.4
    File brew1 = detectHomebrewInstall("/opt/homebrew/Cellar/mesa");
    if(brew1!=null)
      return brew1;
    
    // try homebrew on macos 10.12
    File brew2 = detectHomebrewInstall("/opt/local/Cellar/mesa");
    if(brew2!=null)
      return brew2;
    
    return null;
  }

  private static File detectHomebrewInstall(String homebrew) {
    File homebrew1 = new File(homebrew);
    
    if(!homebrew1.exists())
      return null;
    
    if(homebrew1.listFiles().length>=1) {
      File v1 = homebrew1.listFiles()[0];
      return new File(v1.getAbsolutePath()+ "/lib/");
    }
    return null;
  }

  ////////////////////////////////////////////////////////
  //
  //   CONFIGURE ENV VARIABLE PER OS
  //
  ////////////////////////////////////////////////////////

  
  @Test
  public void whenWindows_ThenEditPath() {
    if (!OS.isWindows()) {
      System.err.println(TEST_ON_WINDOWS_BYPASSED);
      return;
    }


    ChipSelector selector = new ChipSelector(WINDOWS_MESA_PATH.getAbsolutePath(), null, null);
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
      System.err.println(TEST_ON_UNIX_BYPASSED);
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
      System.err.println(TEST_ON_MACOS_BYPASSED);
      return;
    }
    
    VTKUtils.pathConfigurationReport();

    ChipSelector selector = new ChipSelector(MACOS_MESA_PATH.getAbsolutePath(), null, null);

    // When using CPU, Mesa env variable is set for CPU rendering
    selector.use(Chip.CPU);
    Assert.assertEquals("true", getEnv(ChipSelector.MESA_CPU_RENDERING_ENV_VAR));

    // When using GPU, Mesa env variable is set for CPU rendering
    selector.use(Chip.GPU);
    Assert.assertEquals("false", getEnv(ChipSelector.MESA_CPU_RENDERING_ENV_VAR));

  }

  ////////////////////////////////////////////////////////

  // DOn't throw exception anymore but just a warning informing Mesa path is not defined
  
  /*@Test(expected = IllegalArgumentException.class)
  public void whenChipSelectMissMesaConfig_ThrowException() {
    if(OS.isUnix()) {
      throw new IllegalArgumentException("Throwing exception on unix to make this test pass");
    }
    
    System.setProperty("mesa.path", "");

    System.out.println("For info " + ChipSelector.MESA_PATH_PROPERTY_NAME + "="
        + System.getProperty(ChipSelector.MESA_PATH_PROPERTY_NAME));
    
    // If no argument given, neither a system property is defined, an exception is expected
    ChipSelector c = new ChipSelector();
  }*/
  
  ////////////////////////////////////////////////////////
  //
  //   CONFIGURE MESA PATH THROUGH ENV VAR OR CONSTRUCTOR
  //
  ////////////////////////////////////////////////////////


  @Test
  public void whenChipSelectConfigureMesaPath_Windows() {
    if(!OS.isWindows()) {
      System.err.println(TEST_ON_WINDOWS_BYPASSED);
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
      System.err.println(TEST_ON_MACOS_BYPASSED);
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
  
  ////////////////////////////////////////////////////////
  //
  //      FIX PATH MISTAKE
  //
  ////////////////////////////////////////////////////////


  @Test
  public void whenPathDoNotUseOSSeparator_ThenPathIsFixed() {
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
  
  ////////////////////////////////////////////////////////
  //
  //      CONFIGURE PATH THROUGH JVM PROPERTY
  //
  ////////////////////////////////////////////////////////

  
  @Test
  public void whenConfigureGLSystemPath_ThenFullPathIsProperlyConfigure_MacOS() {
    if(!OS.isMac()) {
      System.err.println(TEST_ON_MACOS_BYPASSED);
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
  public void whenConfigureGLSystemPath_ThenFullPathIsProperlyConfigure_Windows() {
    if(!OS.isWindows()) {
      System.err.println(TEST_ON_WINDOWS_BYPASSED);
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
  //
  //      USE LIB NAME TO LOAD SYSTEM PATH
  //
  ////////////////////////////////////////////////////////
  
  @Test
  public void whenNoGLSystemPath_ThenUseSystemLibName_Windows() {
    if(!OS.isWindows()) {
      System.err.println(TEST_ON_WINDOWS_BYPASSED);
      return;
    }
    
    // Given no path configuration
    ChipSelector c3 = new ChipSelector(WINDOWS_MESA_PATH.getAbsolutePath(), null, null);

    // When getting path 
    Assert.assertNull(c3.getOpenGLPath_Windows_System());
  }
  
  @Test
  public void whenNoGLSystemPath_ThenUseSystemLibName_MacOS() {
    if(!OS.isMac()) {
      System.err.println(TEST_ON_MACOS_BYPASSED);
      return;
    }
    
    // Given no path configuration
    ChipSelector c3 = new ChipSelector();

    // When getting path 
    Assert.assertNull(c3.getOpenGLPath_Windows_System());
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
