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
