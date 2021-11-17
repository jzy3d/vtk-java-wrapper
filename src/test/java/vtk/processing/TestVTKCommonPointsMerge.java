package vtk.processing;

import org.junit.BeforeClass;
import org.junit.Test;
import junit.framework.Assert;
import vtk.VTKGeometry;
import vtk.VTKReader;
import vtk.VTKUtils;
import vtk.vtkAlgorithm;
import vtk.vtkPolyData;
import vtk.vtkUnstructuredGrid;

public class TestVTKCommonPointsMerge {
  @BeforeClass
  public static void load() {
    VTKUtils.loadVtkNativeLibraries();
  }

  @Test
  public void givenPVTU_WhenMergingPoints_ThenThereAreNoDoublonPoints() {
    String file = "./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080.pvtu";

    vtkAlgorithm reader = VTKReader.getReader(file);

    // ----------------------------------------------------
    // When reading raw content without processing, then large number of points

    vtkUnstructuredGrid rawInput = VTKReader.getOutput(reader);

    // Then large number of points
    Assert.assertEquals(9114, rawInput.GetNumberOfPoints());
    Assert.assertEquals(5778, rawInput.GetNumberOfCells());
    Assert.assertEquals(0001, rawInput.GetNumberOfPieces());
    Assert.assertEquals(VTKGeometry.VTK_HEXAHEDRON, rawInput.GetCellType(0));

    // ----------------------------------------------------
    // When merging points, then smaller number of points, geometry changed to QUAD, which produce
    // more cells than hexahedron 

    VTKCommonPointsMerge merge = new VTKCommonPointsMerge(reader.GetOutputPort());
    merge.update();

    vtkPolyData quads = merge.getOutput();

    Assert.assertEquals(4672, quads.GetNumberOfPoints());
    Assert.assertEquals(7160, quads.GetNumberOfCells());
    Assert.assertEquals(0001, quads.GetNumberOfPieces());
    Assert.assertEquals(VTKGeometry.VTK_QUAD, quads.GetCellType(0));

  }
}
