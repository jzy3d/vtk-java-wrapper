package vtk.rendering.jogl;

import org.junit.Assert;
import org.junit.Test;
import vtk.rendering.jogl.chip.Chip;
import vtk.rendering.jogl.chip.ChipSelector;

public class TestChipSelector {
  @Test
  public void whenWindows_ThenEditPath() {
    if (!OS.isWindows()) {
      System.err.println("Not running Windows test on this computer");
      return;
    }


    ChipSelector selector = new ChipSelector();
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
  public void whenUnix_ThenEdit() {
    if (!OS.isUnix()) {
      System.err.println("Not running Unix test on this computer");
      return;
    }

    ChipSelector selector = new ChipSelector();

    // When using CPU, Mesa env variable is set for CPU rendering
    selector.use(Chip.CPU);
    Assert.assertEquals("true", getEnv(ChipSelector.LINUX_ENV_VAR));

    // When using GPU, Mesa env variable is set for CPU rendering
    selector.use(Chip.GPU);
    Assert.assertEquals("false", getEnv(ChipSelector.LINUX_ENV_VAR));

  }
  
  @Test
  public void whenMacOS_ThenEdit() {
    if (!OS.isMac()) {
      System.err.println("Not running MacOS test on this computer");
      return;
    }

    ChipSelector selector = new ChipSelector();

    // When using CPU, Mesa env variable is set for CPU rendering
    selector.use(Chip.CPU);
    Assert.assertEquals("true", getEnv(ChipSelector.LINUX_ENV_VAR));

    // When using GPU, Mesa env variable is set for CPU rendering
    selector.use(Chip.GPU);
    Assert.assertEquals("false", getEnv(ChipSelector.LINUX_ENV_VAR));

  }

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
