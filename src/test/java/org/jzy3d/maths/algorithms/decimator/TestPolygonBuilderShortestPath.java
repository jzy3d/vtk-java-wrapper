package org.jzy3d.maths.algorithms.decimator;

import org.junit.Test;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import junit.framework.Assert;

public class TestPolygonBuilderShortestPath {

  @Test
  public void square() {
    // Given
    Point[] points = new Point[4];
    points[0] = new Point(new Coord3d(0, 0, 0), Color.BLUE); // P1 left bottom
    points[1] = new Point(new Coord3d(1, 0, 0), Color.BLUE); // neighbor edge will be dropped
    points[2] = new Point(new Coord3d(1, 1, 0), Color.BLUE); // neighbor edge will be dropped
    points[3] = new Point(new Coord3d(0, 1, 0), Color.BLUE); // P1 left top

    
    
    // When evaluate distances
    PolygonBuilderShortestPath builder = new PolygonBuilderShortestPath();
    
    
    // Then ideal order is shortest
    Assert.assertEquals(4.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[0]));

    // Then other orders are longer
    Assert.assertEquals(6.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[1]));
    Assert.assertEquals(6.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[2]));
    Assert.assertEquals(6.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[3]));

  }
  
  @Test
  public void rectangle() {
    // Given
    Point[] points = new Point[4];
    points[0] = new Point(new Coord3d(0, 0, 0), Color.BLUE); 
    points[1] = new Point(new Coord3d(10, 0, 0), Color.BLUE); 
    points[2] = new Point(new Coord3d(10, 1, 0), Color.BLUE); 
    points[3] = new Point(new Coord3d(0, 1, 0), Color.BLUE); 

    
    
    // When evaluate distances
    PolygonBuilderShortestPath builder = new PolygonBuilderShortestPath();
    
    
    // Then ideal order is shortest
    Assert.assertEquals(202.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[0]));

    // Then other orders are longer
    Assert.assertEquals(402.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[1]));
    Assert.assertEquals(204.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[2]));
    Assert.assertEquals(402.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[3]));

  }
  
  @Test
  public void rectangleNonPlanar() {
    // Given
    Point[] points = new Point[4];
    points[0] = new Point(new Coord3d(0, 0, 0), Color.BLUE); 
    points[1] = new Point(new Coord3d(10, 0, 0), Color.BLUE); 
    points[2] = new Point(new Coord3d(10, 1, 0), Color.BLUE); 
    points[3] = new Point(new Coord3d(0, 1, 10), Color.BLUE); 

    
    
    // When evaluate distances
    PolygonBuilderShortestPath builder = new PolygonBuilderShortestPath();
    
    
    // Then ideal order is shortest
    Assert.assertEquals(402.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[0]));

    // Then other orders are longer
    Assert.assertEquals(602.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[1]));
    Assert.assertEquals(404.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[2]));
    Assert.assertEquals(602.0, builder.distanceSq(points, PolygonBuilderShortestPath.orders[3]));

  }
}
