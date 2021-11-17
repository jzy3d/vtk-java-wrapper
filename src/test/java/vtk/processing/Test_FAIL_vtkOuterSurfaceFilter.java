package vtk.processing;

import org.junit.BeforeClass;
import org.junit.Test;
import junit.framework.Assert;
import vtk.VTKGeometry;
import vtk.VTKReader;
import vtk.VTKUtils;
import vtk.vtkAlgorithm;
import vtk.vtkGeometryFilter;
import vtk.vtkPolyData;

public class Test_FAIL_vtkOuterSurfaceFilter {
  @BeforeClass
  public static void load() {
    VTKUtils.loadVtkNativeLibraries();
  }

  @Test
  public void givenMergedPoints_WhenFilteringOuterSurface_ThenLotOfPolygonsAreRemoved() {
    String file = "./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080.pvtu";

    // Given a PVTU with merged points
    vtkAlgorithm reader = VTKReader.getReader(file);
    VTKCommonPointsMerge merge = new VTKCommonPointsMerge(reader.GetOutputPort());
    merge.update();

    vtkPolyData quads = merge.getOutput();

    Assert.assertEquals(4672, quads.GetNumberOfPoints());
    Assert.assertEquals(7160, quads.GetNumberOfCells());
    Assert.assertEquals(0001, quads.GetNumberOfPieces());
    Assert.assertEquals(VTKGeometry.VTK_QUAD, quads.GetCellType(0));

    // ----------------------------------------------------
    // When filtering outer surface

    vtkGeometryFilter outerSurfFilter = new vtkGeometryFilter();
    outerSurfFilter.SetInputConnection(merge.getOutputPort());
    outerSurfFilter.CellClippingOff();
    outerSurfFilter.PointClippingOff();
    
    outerSurfFilter.Update();

    vtkPolyData outerSurface = outerSurfFilter.GetOutput();

    Assert.assertEquals(4672, outerSurface.GetNumberOfPoints());
    Assert.assertEquals(7160, quads.GetNumberOfCells());
    Assert.assertEquals(0001, quads.GetNumberOfPieces());
    Assert.assertEquals(VTKGeometry.VTK_QUAD, quads.GetCellType(0));

  }

}
