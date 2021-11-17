package org.jzy3d.maths.algorithms.decimator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jzy3d.maths.Angle3d;
import org.jzy3d.maths.Permutations;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;

/**
 * Generate all possible permutations of 4 points, and iterate until we find one whose angle sum
 * to 360°. This makes the solution suitable for planar polygons only.
 * 
 * <p>
 * There are 24 possible combination, but there is a high chance of finding a good one with the
 * 2-4 first swaps. It won't check more than 12 since the 24 combination contain path in one
 * direction, and the same in the opposite direction.
 * </p>
 * <img src="doc-files/quadrilatereShoelaceAreapng.png"/>
 * 
 * Useful links for thinking of alternative algorithms for the 3D case
 * 
 * @see https://dip4fish.blogspot.com/2012/12/simple-direct-quadrilaterals-found-with.html
 * @see https://stackoverflow.com/questions/242404/sort-four-points-in-clockwise-order
 */
public class PolygonBuilderArea360 implements PolygonBuilder {
  public Polygon buildPolygonEnvelope(Set<Point> points) {
    if (points.size() != 4) {
      throw new IllegalArgumentException("Expecting 4 points");
    }

    List<Point> candidates = new ArrayList<>();
    candidates.addAll(points);

    List<List<Point>> combinations = Permutations.of(candidates);

    if (combinations.size() != 24) {
      throw new IllegalArgumentException("Expecting 24 candidates : " + combinations.size());
    }

    for (List<Point> combination : combinations) {
      double sum = Angle3d.angleSumFromPoints(combination);

      if (Math.abs(sum - 2 * Math.PI) < 0.0001) {
        Polygon merged = new Polygon();
        merged.add(combination);
        return merged;
      }
    }
    throw new RuntimeException(
        "Did not find any valid combination for which a sum of 4 points yields to 360°.\n"
            + "Are input points coplanar? " + points);
  }
}
