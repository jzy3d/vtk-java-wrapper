package org.jzy3d.maths.algorithms.decimator;

import java.util.HashSet;
import java.util.Set;
import org.jzy3d.maths.Array;
import org.jzy3d.maths.Array.Direction;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Polygon;

public class NextCellFinderByDirection implements NextCellFinder {
  protected Direction direction = Direction.X;
  protected HexahedronDecimator decimator;
  
  public NextCellFinderByDirection(HexahedronDecimator decimator) {
    super();
    this.decimator = decimator;
  }

  public NextCellFinderByDirection(Direction direction, HexahedronDecimator decimator) {
    super();
    this.direction = direction;
    this.decimator = decimator;
  }

  @Override
  public NextCellResult getNextCell(Polygon current) {
    
    Coord3d[] coords = current.getCoordArray();
    
    Set<Coord3d> side = getNextCellSide(coords, direction);    
    
    if (decimator.getNeighboursAt(side).size() > 2) {
      String errorMessage = "Hexahedron : SKIP NEIGHBOURHOOD OF +2 QUADS ON " + side;
      // abort working on this current cell
      return new NextCellResult(null, side, false, errorMessage);
    }

    // get neighbour on this side
    Polygon next = decimator.getNeighbour(current, side);
    
    
    return new NextCellResult(next, side);
  }

  private Set<Coord3d> getNextCellSide(Coord3d[] coords, Direction direction) {
    Array.sortAscending(coords, direction);
    
    Set<Coord3d> side = new HashSet<>();

    side.add(coords[3]);
    side.add(coords[2]);
    
    return side;
  }
}
