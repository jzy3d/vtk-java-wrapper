package vtk.rendering.jogl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import vtk.rendering.jogl.chip.Chip;

/**
 * 
 * Limitations
 * <ul>
 * <li>this test may fail on Windows if the executing JVM already executed {@link TestVersatileCanvas_CPU}
 * </ul>
 * 
 * <h2>MacOS IDE configuration</h2>
 * -Djava.library.path=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-x86_64:${env_var:PATH}
 * 
 * @author Martin Pernollet
 */
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
