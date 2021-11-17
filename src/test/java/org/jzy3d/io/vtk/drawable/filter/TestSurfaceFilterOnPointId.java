package org.jzy3d.io.vtk.drawable.filter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.jzy3d.maths.TicToc;
import junit.framework.Assert;
import vtk.VTKReader;
import vtk.VTKUtils;
import vtk.vtkUnstructuredGrid;

public class TestSurfaceFilterOnPointId {
  @BeforeClass
  public static void load() {
    VTKUtils.loadVtkNativeLibraries();
  }

  @Test
  public void testSurfaceFilter() {
    int EXPECT_CELLS = 5778;
    int EXPECT_POINTS = 9114;
    //int EXPECT_POLYGONS_BEFORE_FILTER = EXPECT_CELLS * VTKDrawableBuilder.HEXAHEDRON_FACES;
    //int EXPECT_POLYGONS_AFTER_FILTER = 9684;

    TicToc clock = new TicToc();

    // Given
    
    vtkUnstructuredGrid grid = VTKReader.getReaderOutput("./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080.pvtu");

    //vtkUnstructuredGrid grid = reader.GetOutput();

    Assert.assertEquals(EXPECT_CELLS, grid.GetNumberOfCells());
    Assert.assertEquals(EXPECT_POINTS, grid.GetNumberOfPoints());

    
    
    SurfaceFilterOnPointId filter = new SurfaceFilterOnPointId(grid, grid.GetCells());
    System.out.println(filter.keep.size());
    System.out.println(filter.drop.size());
    
    
    
    /*VTKDrawableBuilder builder = new VTKDrawableBuilder(grid);

    clock.tic();
    List<Polygon> polygons = builder.makePolygons("cn");
    clock.tocShow("Building polygons took");
    Assert.assertEquals(EXPECT_POLYGONS_BEFORE_FILTER, polygons.size());*/


    // When
    /*clock.tic();
    List<Polygon> out = new SurfaceFilter().filterPolygonsWithExactNeighbourhood(polygons);
    clock.tocShow("Filtering polygons took");

    // Then
    Assert.assertEquals(EXPECT_POLYGONS_AFTER_FILTER, out.size());*/
  }

}
