package org.jzy3d.maths.algorithms.decimator;

import java.util.Set;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Polygon;

public class NextCellFinderByPointID implements NextCellFinder {
  // Parent decimator
  protected HexahedronDecimator decimator;

  // If found no neighbour on that side (meaning the shape is not closed)
  // then try on other sides.
  //
  // This is mainly necessary because some unit tests involve a single row
  // which direction is not known in advance.
  protected boolean allowMergeDirectionChange = true;

  // start browsing in "East" direction but actually depend on how the first polygon points are
  // ordered.
  protected int lineBrowseSideId = 0;


  public NextCellFinderByPointID(HexahedronDecimator decimator) {
    super();
    this.decimator = decimator;
  }

  @Override
  public NextCellResult getNextCell(Polygon current) {

    // pick a side, i.e. a direction for working
    Set<Coord3d> side = decimator.getSide(current, lineBrowseSideId);

    // check inappropriate neighbourhood.
    int n = decimator.getNeighboursAt(side).size();
    if (n > 2) {
      String errorMessage = "Hexahedron : skip neighbourhood of " + n + " quads on side " + side
          + ". May have a non working or inappropriate surface filter if more than 2 neighbour for a given side.";

      return new NextCellResult(null, side, false, errorMessage);
    }

    // get neighbour on this side
    Polygon next = decimator.getNeighbour(current, side);

    // change search direction if next cell is null. The algorithm can only change direction 3
    // times.
    if (allowMergeDirectionChange) {
      while (next == null && lineBrowseSideId < 3) {
        lineBrowseSideId++;
        
        side = decimator.getSide(current, lineBrowseSideId);
        next = decimator.getNeighbour(current, side);
        if (lineBrowseSideId == 3) {
          break; // stop after trying the four sides without success
        }
      }
    }

    return new NextCellResult(next, side);
  }

}
