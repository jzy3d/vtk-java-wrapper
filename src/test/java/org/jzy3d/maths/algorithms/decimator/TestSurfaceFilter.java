package org.jzy3d.maths.algorithms.decimator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.ChartFactory;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.io.vtk.drawable.VTKDrawableBuilder;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.TicToc;
import org.jzy3d.plot3d.primitives.Composite;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.RandomGeom;
import org.jzy3d.plot3d.rendering.scene.Decomposition;
import com.google.common.collect.ArrayListMultimap;
import junit.framework.Assert;
import vtk.VTKUtils;
import vtk.vtkUnstructuredGrid;
import vtk.vtkXMLUnstructuredGridReader;

public class TestSurfaceFilter {
  @BeforeClass
  public static void load() {
    VTKUtils.loadVtkNativeLibraries();
  }

  /** Non reg test on a sample file */
  @Test
  public void whenFilteringHiddenPolygons_ThenNumberOfFinalPolygonsIsSmaller_Part1() {
    int EXPECT_CELLS = 13767;
    int EXPECT_POINTS = 110136;
    int EXPECT_POLYGONS_BEFORE_FILTER = EXPECT_CELLS * VTKDrawableBuilder.HEXAHEDRON_FACES;
    int EXPECT_POLYGONS_AFTER_FILTER = 9684;

    TicToc clock = new TicToc();

    // Given
    vtkXMLUnstructuredGridReader reader = new vtkXMLUnstructuredGridReader();
    reader.SetFileName("./src/test/resources/out0006.000.vtu");
    reader.Update();

    vtkUnstructuredGrid grid = reader.GetOutput();

    Assert.assertEquals(EXPECT_CELLS, grid.GetNumberOfCells());
    Assert.assertEquals(EXPECT_POINTS, grid.GetNumberOfPoints());

    VTKDrawableBuilder builder = new VTKDrawableBuilder(grid);

    clock.tic();
    List<Polygon> polygons = builder.makePolygons("cn");
    clock.tocShow("Building polygons took");
    Assert.assertEquals(EXPECT_POLYGONS_BEFORE_FILTER, polygons.size());


    // When
    clock.tic();
    List<Polygon> out = new SurfaceFilter().filterPolygonsWithExactNeighbourhood(polygons);
    clock.tocShow("Filtering polygons took");

    // Then
    Assert.assertEquals(EXPECT_POLYGONS_AFTER_FILTER, out.size());

  }

  @Test
  public void whenTwoCubesTouchEachOther_ThenTwoTouchingFacesAreFilteredOut() {
    RandomGeom rg = new RandomGeom();
    List<Composite> composites = rg.cubes(2);

    List<Polygon> polygons = Decomposition.getPolygonDecomposition(composites);

    Assert.assertEquals(6 * 2, polygons.size());

    List<Polygon> filteredPolygons = new SurfaceFilter().filterHiddenPolygons(polygons);

    Assert.assertEquals(6 * 2 - 2, filteredPolygons.size());
  }

  /**
   * Check part 2 of the algorithm
   */
  @Test
  public void whenOneBigCubeTouchFourSmallCubes_ThenTouchingFacesAreFilteredOut_Part2() throws IOException {
    RandomGeom rg = new RandomGeom();
    List<Composite> cubes = new ArrayList<>();
    
    // Given
    int s = 5; // small width
    int b = s * 2; // big cube width
    cubes.add(rg.cube(0, 0, 0, b, b, b)); // big cube
    cubes.add(rg.cube(0, 0, b, s, s, s)); // small cube 1
    cubes.add(rg.cube(s, 0, b, s, s, s)); // small cube 2
    cubes.add(rg.cube(s, s, b, s, s, s)); // small cube 2
    cubes.add(rg.cube(0, s, b, s, s, s)); // small cube 2
    
    
    List<Polygon> polygons = Decomposition.getPolygonDecomposition(cubes);
    List<Polygon> filteredPolygons = new SurfaceFilter().filterHiddenPolygons(polygons, true);

    ChartFactory cf = new EmulGLChartFactory();
    cf.getPainterFactory().setOffscreen(800, 600);
    Chart chart = cf.newChart();
    
    chart.add(polygons);
    chart.screenshot(new File("./target/whenOneBigCubeTouchFourSmallCubes_before.png"));
    chart.remove(polygons);
    
    chart.add(filteredPolygons);
    chart.screenshot(new File("./target/whenOneBigCubeTouchFourSmallCubes_after.png"));
    chart.remove(filteredPolygons);
    
    int EXPECT_ALL_FACES = 6 * cubes.size();
    
    Assert.assertEquals(EXPECT_ALL_FACES, polygons.size());

    
    int EXPECT_REMOVE_INTER_SMALL_CUBE_FACES = 4 * 2;
    int EXPECT_REMOVE_INTER_BIG_SMALL_CUBE_FACES = 4 + 1;
    int EXPECT_REMOVE = EXPECT_REMOVE_INTER_SMALL_CUBE_FACES + EXPECT_REMOVE_INTER_BIG_SMALL_CUBE_FACES;
    Assert.assertEquals(EXPECT_ALL_FACES - EXPECT_REMOVE, filteredPolygons.size());

    
  }
  
  @Test
  public void whenOnePolygonAsSinglePointOfContactWithFourOthers() {
    RandomGeom rg = new RandomGeom();

    Polygon big = rg.poly(0, 0, 0, 10, 10);
    Polygon sm1 = rg.poly(0, 0, 0, 5, 5);
    Polygon sm2 = rg.poly(5, 0, 0, 5, 5);
    Polygon sm3 = rg.poly(5, 5, 0, 5, 5);
    Polygon sm4 = rg.poly(0, 5, 0, 5, 5);
    
    List<Polygon> polygons = new ArrayList<>();
    polygons.add(big);
    polygons.add(sm1);
    polygons.add(sm2);
    polygons.add(sm3);
    polygons.add(sm4);
    
    
    ArrayListMultimap<Coord3d, Polygon> pointToPoly = ArrayListMultimap.create();


    for (Polygon p : polygons) {
      for (Point pt : p.getPoints()) {
        pointToPoly.put(pt.xyz, p);
      }
    }
    
    // First verify the low level primitive
    
    SurfaceFilter filter = new SurfaceFilter();
    
    List<Polygon> neighbours1 = filter.getSinglePointOfContactNeighbours(pointToPoly, big, big.get(0).xyz);
    Assert.assertEquals(1, neighbours1.size());
    Assert.assertEquals(sm1, neighbours1.get(0));

    List<Polygon> neighbours2 = filter.getSinglePointOfContactNeighbours(pointToPoly, big, big.get(1).xyz);
    Assert.assertEquals(1, neighbours2.size());
    Assert.assertEquals(sm2, neighbours2.get(0));

    List<Polygon> neighbours3 = filter.getSinglePointOfContactNeighbours(pointToPoly, big, big.get(2).xyz);
    Assert.assertEquals(1, neighbours3.size());
    Assert.assertEquals(sm3, neighbours3.get(0));

    List<Polygon> neighbours4 = filter.getSinglePointOfContactNeighbours(pointToPoly, big, big.get(3).xyz);
    Assert.assertEquals(1, neighbours4.size());
    Assert.assertEquals(sm4, neighbours4.get(0));

    
    // Now verify the higher level primitive
    
    ArrayListMultimap<Coord3d, Polygon> r = filter.getSinglePointOfContactPerVertex(pointToPoly, big);
    Assert.assertEquals(4, r.keySet().size());
    Assert.assertEquals(sm1, r.get(big.get(0).xyz).get(0));
    Assert.assertEquals(sm2, r.get(big.get(1).xyz).get(0));
    Assert.assertEquals(sm3, r.get(big.get(2).xyz).get(0));
    Assert.assertEquals(sm4, r.get(big.get(3).xyz).get(0));
  }

}


