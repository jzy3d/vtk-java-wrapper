package org.jzy3d.io.vtk.drawable;

import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jzy3d.colors.Color;
import org.jzy3d.plot3d.primitives.Polygon;
import junit.framework.Assert;
import vtk.VTKUtils;
import vtk.vtkUnstructuredGrid;
import vtk.vtkXMLUnstructuredGridReader;

/**
 * These tests will have the classpath to VTK native automatically added by Maven Surefire Plugin
 * when run with <code>mvn test</code>.
 * 
 * However, if you run them from an IDE, you may need to configure the Run Configuration so that VM
 * arguments are <code>
 * -Djava.library.path=./lib/vtk-Darwin-x86_64
 * </code>
 * 
 * @author Martin Pernollet
 */
public class TestVTKDrawableBuilder {
  @BeforeClass
  public static void load() {
    VTKUtils.loadVtkNativeLibraries();
  }

  @Test
  public void buildPolygons_With_VTK_HEXAHEDRONS() {
    int EXPECT_CELLS = 13767;
    int EXPECT_POINTS = 110136;
    int HEXAHEDRON_FACES = 6;

    // Given
    vtkXMLUnstructuredGridReader reader = new vtkXMLUnstructuredGridReader();
    reader.SetFileName("./src/test/resources/out0006.000.vtu");
    reader.Update();

    vtkUnstructuredGrid grid = reader.GetOutput();

    Assert.assertEquals(EXPECT_CELLS, grid.GetNumberOfCells());
    Assert.assertEquals(EXPECT_POINTS, grid.GetNumberOfPoints());

    // When
    VTKDrawableBuilder builder = new VTKDrawableBuilder(grid);

    // Then
    List<Polygon> polygons = builder.makePolygons("cn");
    Assert.assertEquals(EXPECT_CELLS * HEXAHEDRON_FACES, polygons.size());

    Assert.assertFalse(polygons.get(0).isWireframeDisplayed());
    Assert.assertNull(polygons.get(0).getWireframeColor());
  }

  @Test
  public void buildPolygons_With_VTK_QUADS() {
    int EXPECT_CELLS = 436;
    int EXPECT_POINTS = 501;

    // Given
    vtkXMLUnstructuredGridReader reader = new vtkXMLUnstructuredGridReader();
    reader.SetFileName("./src/test/resources/Enthalpy_HS_wCon_wRad_010_0.vtu");
    reader.Update();

    vtkUnstructuredGrid grid = reader.GetOutput();

    Assert.assertEquals(EXPECT_CELLS, grid.GetNumberOfCells());
    Assert.assertEquals(EXPECT_POINTS, grid.GetNumberOfPoints());

    // When
    VTKDrawableBuilder builder = new VTKDrawableBuilder(grid);
    builder.setWireframeColor(Color.WHITE);
    builder.setWireframeDisplayed(true);

    // Then
    List<Polygon> polygons = builder.makePolygons("temp");
    Assert.assertEquals(EXPECT_CELLS, polygons.size());
    Assert.assertTrue(polygons.get(0).isWireframeDisplayed());
    Assert.assertEquals(Color.WHITE, polygons.get(0).getWireframeColor());

  }
  
}
