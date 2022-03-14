package vtk.rendering.jogl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import vtk.rendering.jogl.ChipSelector.Chip;

public class TestVersatileCanvas_CPU extends TestVersatileCanvas{
  @BeforeClass
  public static void load() {
    VTKVersatileCanvas.loadNativesFor(Chip.CPU);
  }
  
  @Test
  public void whenQueryCPU_ThenCPUIsUsed() throws InterruptedException {
    Chip chip = Chip.CPU;
    
    //VTKVersatileCanvas.loadNativesFor(chip);

    VTKVersatileCanvas canvas = new VTKVersatileCanvas();
    
 // Embedding GUI with auto-reload hability    
    frame = newSceneAndFrame(canvas);
    
    Thread.sleep(1000);
    
    Assert.assertEquals(chip, canvas.getQueriedChip());
    Assert.assertEquals(chip, canvas.getActualChip());
  }
}
