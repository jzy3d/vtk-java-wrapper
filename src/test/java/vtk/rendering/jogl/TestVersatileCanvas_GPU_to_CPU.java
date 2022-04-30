package vtk.rendering.jogl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import vtk.rendering.jogl.chip.Chip;

/**
 * 
 * Limitations
 * <ul>
 * <li>this test will only execute on Unix which supports hot switch from GPU to CPU without restarting the JVM 
 * <li>this test may fail on Windows if the executing JVM already executed TestVersatileCanvas_CPU
 * </ul>
 * 
 * @author Martin Pernollet
 */
public class TestVersatileCanvas_GPU_to_CPU extends TestVersatileCanvas{
 
  @BeforeClass
  public static void load() {
    configureMesaPathProperty();

    VTKVersatileCanvas.loadNativesFor(Chip.GPU);    
  }
  
  @Test
  public void whenQueryGPU_To_CPU_ThenCPUIsUsed_UnixOnly() throws InterruptedException {
    if(!OS.isUnix()) {
      System.err.println(TestVersatileCanvas_GPU_to_CPU.class.getSimpleName() + " not supposed to run on Mac or Windows");
      System.err.println("Exiting test without verifying anything");
      return;
    }
    
    VTKVersatileCanvas canvas = new VTKVersatileCanvas();
    
    // When Embedding GUI with auto-reload hability    
    frame = newSceneAndFrame(canvas);
    
    // Then
    Thread.sleep(1000);
    
    Assert.assertEquals("Init on GPU", Chip.GPU, canvas.getQueriedChip());
    Assert.assertEquals("Init on GPU", Chip.GPU, canvas.getActualChip());
    
    
    // When 
    canvas.switchTo(Chip.CPU, newOnSwitch(canvas));
    
    // Then
    Thread.sleep(1000);
    
    Assert.assertEquals("Switch to CPU", Chip.CPU, canvas.getQueriedChip());
    Assert.assertEquals("Switch to CPU", Chip.CPU, canvas.getActualChip());

  }
}
