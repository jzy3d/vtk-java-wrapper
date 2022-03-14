package vtk.rendering.jogl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import vtk.rendering.jogl.ChipSelector.Chip;

public class TestVersatileCanvas_GPU_to_CPU extends TestVersatileCanvas{
 
  @BeforeClass
  public static void load() {
    VTKVersatileCanvas.loadNativesFor(Chip.GPU);
  }
  
  @Test
  public void whenQueryGPU_To_CPU_ThenCPUIsUsed() throws InterruptedException {
    Chip chip = Chip.GPU;
    
  
    VTKVersatileCanvas canvas = new VTKVersatileCanvas();
    
    // Embedding GUI with auto-reload hability    
    frame = newSceneAndFrame(canvas);
    
    // When 
    Thread.sleep(1000);
    
    Assert.assertEquals(chip, canvas.getQueriedChip());
    Assert.assertEquals(chip, canvas.getActualChip());
    
    canvas.switchTo(Chip.CPU, newOnSwitch(canvas));

    // When 
    Thread.sleep(1000);
    
    Assert.assertEquals(Chip.CPU, canvas.getQueriedChip());
    Assert.assertEquals(Chip.CPU, canvas.getActualChip());

  }
}
