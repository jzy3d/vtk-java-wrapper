package vtk.rendering.jogl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import vtk.rendering.jogl.chip.Chip;

public class TestVersatileCanvas_GPU extends TestVersatileCanvas{
 
  @BeforeClass
  public static void load() {
    configureMesaPathProperty();

    //System.out.println("LOADING NATIVE / GPU");
    VTKVersatileCanvas.loadNativesFor(Chip.GPU);    
  }
  
  @Test
  public void whenQueryGPU_ThenGPUIsUsed() throws InterruptedException {
    Chip chip = Chip.GPU;
    
  
    VTKVersatileCanvas canvas = new VTKVersatileCanvas();
    
 // Embedding GUI with auto-reload hability    
    frame = newSceneAndFrame(canvas);
    
    Thread.sleep(1000);
    
    Assert.assertEquals(chip, canvas.getQueriedChip());
    Assert.assertEquals(chip, canvas.getActualChip());
  }
}
