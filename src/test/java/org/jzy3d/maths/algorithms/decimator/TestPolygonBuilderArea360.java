package org.jzy3d.maths.algorithms.decimator;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import junit.framework.Assert;

public class TestPolygonBuilderArea360 {

  @Test
  public void polygonEnveloppe() {
    whenPointSet_ThenPolygonEnvelope();
  }

  private Chart whenPointSet_ThenPolygonEnvelope() {
    // Given
    Polygon p1 = new Polygon();
    p1.add(new Point(new Coord3d(0, 0, 0), Color.BLUE)); // P1 left bottom
    p1.add(new Point(new Coord3d(1, 0, 0), Color.BLUE)); // neighbor edge will be dropped
    p1.add(new Point(new Coord3d(1, 1, 0), Color.BLUE)); // neighbor edge will be dropped
    p1.add(new Point(new Coord3d(0, 1, 0), Color.BLUE)); // P1 left top
    
    Set<Point> pts = new HashSet<>();
    pts.addAll(p1.getPoints());

    // When merging
    PolygonBuilder builder = new PolygonBuilderArea360();
    Polygon envelope = builder.buildPolygonEnvelope(pts);
    envelope.setWireframeColor(Color.GREEN);

    Assert.assertTrue(envelope.size()==4);
    
    // TODO : more tests
    
    /*
     * ChartFactory f = new EmulGLChartFactory(); f.getPainterFactory().setOffscreen(600, 400);
     * Chart c = f.newChart(); c.add(envelope);
     * 
     * 
     * 
     * ChartTester tester = new ChartTester(); tester.assertSimilar(c,
     * ChartTester.EXPECTED_IMAGE_FOLDER + "builPolygonEnvelope" + ".png"); return c;
     */
    return null;
  }
}
