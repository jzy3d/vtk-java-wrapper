package org.jzy3d.maths.algorithms.decimator;

import java.util.Set;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;

/**
 * Evaluate all possible non oriented pathes that may be formed by the 4 first permutations of 4
 * points and returns as soon as one path is shortest from the others.
 * 
 * In other word, we try to find the first permutation that forms the following shape
 * 
 * <pre>
 * A-------B
 * |       |
 * |       |
 * |       |
 * D-------C
 * </pre>
 * 
 * And will skip any permutation leading to
 * 
 * <pre>
 * A-------B
 *   \   / 
 *    \ /  
 *    / \  
 *   /   \ 
 * D-------C
 * </pre>
 * 
 * or
 * <pre>
 * A       B
 * | \   / |
 * |  \ /  |
 * |  / \  |
 * | /   \ |
 * D       C
 * </pre>
 * 
 * @author martin
 *
 */
public class PolygonBuilderShortestPath implements PolygonBuilder {
  protected static int[][] orders = {{0, 1, 2, 3}, {1, 0, 2, 3}, {0, 2, 1, 3}, {0, 2, 3, 1}};

  protected static boolean tryEarlyEvaluation = false;
  
  /**
   * The output polygon is a convex polygon built from the input set of points.
   * 
   * Note that this merely find a good order for the point but it won't set any Polygon property.
   */
  public Polygon buildPolygonEnvelope(Set<Point> points) {
    if (points.size() != 4) {
      throw new IllegalArgumentException("Expecting 4 points");
    }

    Point[] a = toPointArray(points);

    double[] distances = new double[orders.length];
    double minDist = Double.MAX_VALUE;
    int minDistId = -1;

    for (int i = 0; i < orders.length; i++) {
      distances[i] = distanceSq(a, orders[i]);

      if (distances[i] < minDist) {
        minDist = distances[i];
        minDistId = i;
      }

      // Evaluating early consist in thinking that all permutation of non convex polygon
      // lead to the exact same distance, which seams to be false
      /*if(tryEarlyEvaluation) {
        if (i == 1) {
          if (distances[0] != distances[1]) {
            return makePolygonWith(a, orders, minDistId);
          }
        } else if (i == 2) {
          if (distances[1] != distances[2]) {
            return makePolygonWith(a, orders, minDistId);
          }
        } else if (i == 3) {
          if (distances[2] != distances[3]) {
            return makePolygonWith(a, orders, minDistId);
          }
        }
      }*/
      
    }

    return makePolygonWith(a, orders, minDistId);
  }

  protected Polygon makePolygonWith(Point[] a, int[][] orders, int minDistId) {
    Polygon polygon = new Polygon();

    for (int i = 0; i < orders[minDistId].length; i++) {
      polygon.add(a[orders[minDistId][i]], false);
    }
    polygon.updateBounds();

    return polygon;
  }

  protected Point[] toPointArray(Set<Point> points) {
    Point[] array = new Point[points.size()];
    int k = 0;
    for (Point p : points) {
      array[k++] = p;
    }
    return array;
  }

  protected double distanceSq(Point[] points, int[] order) {
    double cumulated = 0;

    for (int i = 0; i < order.length - 1; i++) {
      cumulated += points[order[i]].xyz.distanceSq(points[order[i+1]].xyz);
    }
    cumulated += points[order[order.length - 1]].xyz.distanceSq(points[order[0]].xyz);

    return cumulated;
  }
}
