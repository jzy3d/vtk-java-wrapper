package org.jzy3d.maths.algorithms.decimator;

import java.util.ArrayList;
import java.util.List;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.ChartFactory;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.plot3d.primitives.Composite;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.RandomGeom;
import org.jzy3d.plot3d.rendering.scene.Decomposition;
import junit.framework.Assert;

public class DemoSurfaceFilterVariousCubeSize {

  public DemoSurfaceFilterVariousCubeSize() {
    // TODO Auto-generated constructor stub
  }

  public static void main(String[] args) {
    RandomGeom rg = new RandomGeom();
    List<Composite> cubes = new ArrayList<>();
    
    // Given
    int s = 5; // small width
    int b = s * 2; // big cube width
    
    Composite big = rg.cube(0, 0, 0, b, b, b); 
    cubes.add(big); // big cube
    cubes.add(rg.cube(0, 0, b, s, s, s)); // small cube 1
    cubes.add(rg.cube(s, 0, b, s, s, s)); // small cube 2
    cubes.add(rg.cube(s, s, b, s, s, s)); // small cube 2
    cubes.add(rg.cube(0, s, b, s, s, s)); // small cube 2
    
    
    List<Polygon> polygons = Decomposition.getPolygonDecomposition(cubes);
    List<Polygon> filteredPolygons = new SurfaceFilter().filterHiddenPolygons(polygons, true);

    
    ChartFactory cf = new EmulGLChartFactory();
    cf.getPainterFactory().setOffscreen(800, 600);
    Chart chart = cf.newChart();
    
    
    
    
    
    chart.add(filteredPolygons);
    //chart.add(filterAnyPolygonOf(filteredPolygons, Decomposition.getPolygonDecomposition(big)));
    //chart.screenshot(new File("./target/whenOneBigCubeTouchFourSmallCubes_after.png"));
    //chart.remove(filteredPolygons);
    
    chart.open();
    chart.addMouseCameraController();
    
    int EXPECT_ALL_FACES = 6 * cubes.size();
    
    Assert.assertEquals(EXPECT_ALL_FACES, polygons.size());

    
    int EXPECT_REMOVE_INTER_SMALL_CUBE_FACES = 4 * 2;
    int EXPECT_REMOVE_INTER_BIG_SMALL_CUBE_FACES = 4 + 1;
    int EXPECT_REMOVE = EXPECT_REMOVE_INTER_SMALL_CUBE_FACES + EXPECT_REMOVE_INTER_BIG_SMALL_CUBE_FACES;
    
    Assert.assertEquals(EXPECT_ALL_FACES - EXPECT_REMOVE, filteredPolygons.size());
  }
  
  public static List<Polygon> filterAnyPolygonOf(List<Polygon> polygons, List<Polygon> exclusions){
    List<Polygon> out = new ArrayList<>();
    
    for(Polygon polygon: polygons) {
      if(!exclusions.contains(polygon)) {
        out.add(polygon);
      }
    }
    return out;
  }

}
