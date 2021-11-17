package org.jzy3d.maths.algorithms.decimator;

import java.util.ArrayList;
import java.util.List;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.ChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.plot3d.primitives.Composite;
import org.jzy3d.plot3d.primitives.Drawable;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.RandomGeom;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Decomposition;

/** Demonstrate the ability to reconstruct a non planar polygon. */
public class DemoSpinningCubesRebuild {
  public static void main(String[] args) {
    RandomGeom gg = new RandomGeom();
    List<Composite> cubes = gg.spinningCubes(4*2, 90f/2f, 0.1f);
    //List<Drawable> cubes = gg.spinningCubes(1, 90f/1.5f, 0.1f);
    
    
    
    // ------------------------------------------------
    //ChartFactory f = new EmulGLChartFactory();
    ChartFactory f = new AWTChartFactory();
    Quality q = Quality.Advanced();
    q.setAlphaActivated(false);
    Chart chart = f.newChart(q);
    chart.getView().setSquared(false);
    chart.add(cubes);//(float)Math.PI/4));
    
    // ------------------------------------------------
    // Decompose and reconstruct non planar polygons
    List<Drawable> polygons = Decomposition.getDecomposition(cubes);
    
    
    PolygonBuilder pb = new PolygonBuilderShortestPath();
    
    List<Drawable> reconstruct = new ArrayList<>();
    for(Drawable drawable: polygons) {
      Polygon polygon = (Polygon)drawable;
      Polygon p2 = pb.buildPolygonEnvelope(polygon.getPointSet());
      p2.setWireframeColor(Color.RED);
      p2.setWireframeWidth(5);
      p2.setFaceDisplayed(false);
      
      reconstruct.add(p2);
      
    }
   
    chart.add(reconstruct);

    // ------------------------------------------------
    // Open and rotate
    chart.open();
    chart.addMouseCameraController().getThread().start();

  }

}
