package vtk.rendering.jogl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import vtk.rendering.jogl.chip.Chip;

/**
 * 
 * Limitations
 * <ul>
 * <li>this test may fail on Windows if the executing JVM already executed {@link TestVersatileCanvas_GPU}
 * </ul>
 * 
 * @author Martin Pernollet
 * 
 * 
 * -Djava.library.path=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-x86_64:${env_var:PATH}
 * 
 * -Djava.library.path=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-arm64
 * 
 * 
DYLD_INSERT_LIBRARIES=/opt/homebrew/Cellar/mesa/21.3.7/lib/libGL.dylib
DYLD_LIBRARY_PATH=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-arm64:${env_var:DYLD_LIBRARY_PATH}
DYLD_PRINT_BINDINGS=YES
DYLD_PRINT_LIBRARIES=NO
LIBGL_ALWAYS_SOFTWARE=true
 * @author martin
 *
 */
public class TestVersatileCanvas_CPU extends TestVersatileCanvas{
  @BeforeClass
  public static void load() {
    configureMesaPathProperty();

    VTKVersatileCanvas.loadNativesFor(Chip.CPU);    
  }
  
  @Test
  public void whenQueryCPU_ThenCPUIsUsed() throws InterruptedException {
    
    VTKVersatileCanvas canvas = new VTKVersatileCanvas();
    
    // Embedding GUI with auto-reload hability    
    frame = newSceneAndFrame(canvas);
    
    Thread.sleep(1000);
    
    Assert.assertEquals(Chip.CPU, canvas.getQueriedChip());
    Assert.assertEquals(Chip.CPU, canvas.getActualChip());
  }
}
