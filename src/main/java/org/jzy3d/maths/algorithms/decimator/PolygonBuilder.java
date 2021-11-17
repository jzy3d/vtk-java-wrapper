package org.jzy3d.maths.algorithms.decimator;

import java.util.Set;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;

public interface PolygonBuilder {
  public Polygon buildPolygonEnvelope(Set<Point> vertices);
}
