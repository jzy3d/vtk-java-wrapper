package org.jzy3d.maths.algorithms.decimator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Angle3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Normal;
import org.jzy3d.maths.algorithms.decimator.NextCellFinder.NextCellResult;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import com.google.common.collect.ArrayListMultimap;

/**
 * Assumptions given in
 * <ul>
 * <li>{@link #mergeNeighbourRectangles}
 * <li>{@link #mergeNeighbours}
 * </ul>
 * 
 * 
 * @author martin
 *
 */
public class HexahedronDecimator {
  protected static Logger log = LogManager.getLogger(HexahedronDecimator.class);

  
  /**
   * A value between 0 and 3 to fix the acceptable distance for merging polygons. A h
   * 
   * Can't be more than 3, which is the max square distance between white to black 3 will allow
   * merging any color.
   * 
   * If < 0, will consider color match if the set of point to merge have exactly the same colors.
   */
  protected double neighbourColorDistanceThreshold = 0.5;

  /**
   * The threshold for considering the angle between two polygon normals similar.
   */
  protected double neighbourAngleThreshold = 0.0;

  /**
   * The algorithm for defining which is the next cell to merge.
   */
  protected NextCellFinder nextCellFinder;

  /**
   * The algorithm for defining the points order for building a convex polygon.
   */
  protected PolygonBuilder polygonBuilder;

  /**
   * Internal representation of neighbourhood around an edge.
   */
  protected ArrayListMultimap<Set<Coord3d>, Polygon> neighbourhood;

  /**
   * Allows having the same normal size for two planar polygons of different sizes.
   */
  protected boolean normalizeNormals = true;

  protected int numberOfInvalidNeighbours;
  
  
  public HexahedronDecimator() {
    this(0.5, 0.0);
  }

  public HexahedronDecimator(double colorDistanceThreshold, double neighbourAngleThreshold) {
    super();
    this.neighbourColorDistanceThreshold = colorDistanceThreshold;
    this.neighbourAngleThreshold = neighbourAngleThreshold;

    //this.nextCellFinder = new NextCellFinderByDirection(this);
    this.nextCellFinder = new NextCellFinderByPointID(this);
    
    //this.polygonBuilder = new PolygonBuilderArea360();
    this.polygonBuilder = new PolygonBuilderShortestPath();
  }



  /**
   * Merge neighbour polygons if their color distance is less than {@link #neighbourColorDistanceThreshold} and if they belong to quads that have the
   * same normal (hence the two cells are coplanar). After a merge of two neighbour quads, we obtain
   * a rectangle which size equals those of the two quads.
   * 
   * Assume
   * <ul>
   * <li>polygon points are ordered and polygons are convex, but all polygon do not need to have their points ordered the same way : the first point can be either at xmin/ymin, xmax/ymax, etc.
   * <li>polygons are quads made of four points.
   * <li>polygons can be non planar as long as the {@link PolygonBuilder} is a {@link PolygonBuilderShortestPath}.
   * </ul>
   * 
   * The best is to prune internal (hence invisible) faces before running this method (with {@link SurfaceFilter#filterHiddenPolygons(List)}
   * 
   * @param input
   * @return
   */
  public List<Polygon> mergeNeighbours(List<Polygon> input) {

    // (Re-)build a neighbourhood map
    initNeighbourhoodInternal(input);

    // Output and input
    List<Polygon> keep = new ArrayList<>();
    Queue<Polygon> candidates = new LinkedBlockingQueue<>();
    candidates.addAll(input);


    Polygon startCell = candidates.poll();
    Polygon current = startCell;

    int mergedCells = 0;
    
    numberOfInvalidNeighbours = 0;

    // iterate over all candidate cells
    while (current != null) {
      NextCellResult nextCell = nextCellFinder.getNextCell(current);

      if (!nextCell.isValid()) {
        log.warn(nextCell.getInfo());
        numberOfInvalidNeighbours++;
        
        // abort working on this current cell
        keep.add(current);
        current = candidates.poll();
        continue;
      }

      Polygon next = nextCell.getPolygon();
      Set<Coord3d> side = nextCell.getSide();

      // reference to merged polygon
      Polygon mergedPolygon = null;

      while (next != null) {

        /**
         * Before merge attempt, we have two rectangles ("current" and "next") and one shared side
         * marked by "s" below.
         * 
         * <pre>
         * |       |       |
         * o-------o-------o--
         * |       s       |
         * | curr  s  next |
         * |       s       |
         * o-------o-------o--
         * |       |       |
         * </pre>
         */

        // check if two candidates can be merged
        if (matchColor(current, next, side) && matchNormals(current, next)) {
          
          /**
           * Merge the two rectangles
           * 
           * <pre>
           * o---------------o--
           * |       .       |
           * |       .       |
           * |       .       |
           * o---------------o--
           * </pre>
           */
          mergedPolygon = mergeNeighbourRectangles(current, side, next);

          /**
           * Remove the two merged rectangles from candidate queue ("current" and "next").
           */
          candidates.remove(next);
          // current already removed by poll()

          /**
           * Update neighbourhood : remove the side marked by "x" and the two merged neighbours
           * ("current" and "next").
           * 
           * <pre>
           * o---------------o--
           * |       x       |
           * |       x       |
           * |       x       |
           * o---------------o--
           * </pre>
           */
          neighbourhood.removeAll(side); // clean this neighbourhood key


          /**
           * Select the neighbourhood side for the cell called "current" in this iteration to
           * unregister it.
           * 
           * We call the side below marked by "x" opposideSideCurrent
           * 
           * <pre>
           * o---------------o--
           * x               |
           * x               |
           * x               |
           * o---------------o--
           * </pre>
           */
          Set<Coord3d> opposideSideCurr = excludeFromSet(current, side);

          if (!getNeighboursAt(opposideSideCurr).remove(current)) {
            throw new RuntimeException(
                "Decimator : can not remove " + current + " from opposite CURR side" + current);
          }

          /**
           * Select the neighbourhood side for the cell called "next" in this iteration to
           * unregister it.
           * 
           * We call the side below marked by "x" opposideSideNext
           * 
           * <pre>
           * o---------------o--
           * |               x
           * |               x
           * |               x
           * o---------------o--
           * </pre>
           */
          Set<Coord3d> opposideSideNext = excludeFromSet(next, side);

          if (!getNeighboursAt(opposideSideNext).remove(next)) {
            throw new RuntimeException(
                "Decimator : can not remove " + next + " from opposite NEXT side" + next);
          }


          /**
           * Check if next neighbourhood still has polygons to provides
           * 
           * <pre>
           * o---------------o--
           * |               ? Another
           * |               ? cell to 
           * |               ? merge here?
           * o---------------o--
           * </pre>
           * 
           */
          Polygon nextNext;
          if (getNeighboursAt(opposideSideNext).isEmpty())
            nextNext = null; // if not, stop while (next != null) loop
          else
            nextNext = getNeighboursAt(opposideSideNext).get(0);

          /**
           * Whatever the ability to continue, register MERGED in neighbourhood. We do this AFTER
           * checking if polygons were remaining on this side.
           * 
           * <pre>
           * o---------------o--
           * +               +
           * +               +
           * +               +
           * o---------------o--
           * </pre>
           */
          getNeighboursAt(opposideSideNext).add(mergedPolygon);
          getNeighboursAt(opposideSideCurr).add(mergedPolygon);

          /**
           * Update neighbourhood : remove neighbourhood above and below the two removed cells.
           * 
           * Despite geometries not having a known orientation, we will call topSide and bottomSide
           * the side marked as "x" below.
           * 
           * <pre>
           * |       |       |
           * o-xxxxx---xxxxx-o--  top sides of current and next
           * |               |
           * |               |
           * |               |
           * o-xxxxx---xxxxx-o--  bottom sides of current and next
           * |       |       |
           * </pre>
           */

          Coord3d[] sideCoords = toArray(side);
          Coord3d[] oppositeSideNextCoords = toArray(opposideSideNext);
          Coord3d[] oppositeSideCurrCoords = toArray(opposideSideCurr);

          removeTopAndBottomSideNeighbourhood(current, sideCoords, oppositeSideCurrCoords);
          removeTopAndBottomSideNeighbourhood(next, sideCoords, oppositeSideNextCoords);

          // ----------------------------------------------------
          // go on with the next cell
          current = mergedPolygon;
          side = opposideSideNext;

          next = nextNext;

          mergedCells++;

        }
        // otherwise leave current and next in candidate list
        else {
          next = null;
        } // endif


      } // end while / next polygon can merge


      // current is either a single polygon, or a collection of merged polygons
      keep.add(current);
      mergedCells = 0;

      // take next candidate cell
      current = candidates.poll();

    } // end while / all polygons avec been dequeued

    return keep;
  }



  protected Coord3d[] toArray(Set<Coord3d> coordSet) {
    Coord3d[] coordArray = new Coord3d[coordSet.size()];
    coordSet.toArray(coordArray);
    return coordArray;
  }


  /**
   * Merge two polygons with 90° angles having a shared edge.
   * 
   * Input
   * 
   * <pre>
   * o-------o-------o
   * |       |       |
   * |       |       |
   * |       |       |
   * o-------o-------o
   * </pre>
   * 
   * 
   * Output
   * 
   * <pre>
   * o---------------o
   * |               |
   * |               |
   * |               |
   * o---------------o
   * </pre>
   *
   * 
   * Assume
   * <ul>
   * <li>mergeability has been verified before (the two polygons really have two points in common).
   * <li>polygon points are coplanar. Without this, the sum of angles of the polygon is not 360 for
   * four points.
   * <li>polygon has 90 degree angles. Without this, the merged polygon will cover an area that is
   * not the area of the two input polygons.
   * </ul>
   * 
   * No assumptions on
   * <ul>
   * <li>Point sequence order. Clockwise/anticlockwise, etc
   * </ul>
   * 
   * @param current
   * @param side the two points that are common to the two input polygons
   * @param next
   * @return
   */
  protected Polygon mergeNeighbourRectangles(Polygon current, Set<Coord3d> side, Polygon next) {

    Set<Point> keptPoints = excludeFromSet(current.getPoints(), side);
    keptPoints.addAll(excludeFromSet(next.getPoints(), side));

    Polygon merged = polygonBuilder.buildPolygonEnvelope(keptPoints);
    
    // trying to rebuild the original order of points to check if normals are modified
    /*int id0 = getPointIdInCurrentOrNext(merged.get(0), current, next);
    int id1 = getPointIdInCurrentOrNext(merged.get(1), current, next);
    int id2 = getPointIdInCurrentOrNext(merged.get(2), current, next);
    int id3 = getPointIdInCurrentOrNext(merged.get(3), current, next);
    
    System.out.println(id0 + " " + id1 + " " + id2 + " " + id3);
    if(id1==0) {
      merged.getPoints().add(merged.getPoints().remove(0));
      System.out.println("swap 1");
    }
    else if(id2==0) {
      merged.getPoints().add(merged.getPoints().remove(0));
      merged.getPoints().add(merged.getPoints().remove(0));
      System.out.println("swap 2");
    }
    else if(id3==0) {
      merged.getPoints().add(merged.getPoints().remove(0));
      merged.getPoints().add(merged.getPoints().remove(0));
      merged.getPoints().add(merged.getPoints().remove(0));
      System.out.println("swap 3");
    }*/

    // rotate points by pushing the first one to the end of the list
    merged.getPoints().add(merged.getPoints().remove(0));
    merged.getPoints().add(merged.getPoints().remove(0));
    
    
    
    
    
    
    //Color meanColor = current.getColor().add(next.getColor()).divSelfWithAlpha(2);
    merged.setWireframeColor(current.getWireframeColor());
    merged.setWireframeDisplayed(current.isWireframeDisplayed());
    
    merged.setSplitInTriangles(current.isSplitInTriangles());
    merged.setNormalizeNormals(current.isNormalizeNormals());
    merged.setReflectLight(current.isReflectLight());
    merged.setNormalProcessingAutomatic(true); // need to be auto
    
    return merged;
  }
  

  protected int getPointIdInCurrentOrNext(Point p, Polygon current, Polygon next) {
    int id = current.getPoints().indexOf(p);
    if(id==-1)
      id = next.getPoints().indexOf(p);
    return id;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // NEIGHBOURHOOD
  //
  /////////////////////////////////////////////////////////////////////////////


  public ArrayListMultimap<Set<Coord3d>, Polygon> initNeighbourhood(List<Polygon> input) {
    // {pointId_1, pointId_2} -> {polygon_1, polygon_2}
    ArrayListMultimap<Set<Coord3d>, Polygon> neighbourhood = ArrayListMultimap.create();

    for (Polygon p : input) {
      for (int i = 0; i < numberOfSides(p); i++) {
        Set<Coord3d> pts = getSide(p, i);
        neighbourhood.put(pts, p);
      }
    }
    return neighbourhood;
  }

  public void initNeighbourhoodInternal(List<Polygon> input) {
    neighbourhood = initNeighbourhood(input);
  }

  public Set<Set<Coord3d>> getNeighbourhoodSides() {
    return neighbourhood.keySet();
  }

  /**
   * Return the neighbour of polygon p on the side given by the set of coords.
   * 
   * The method does not verify if the side has coordinates shared with the polygon.
   */
  protected Polygon getNeighbour(Polygon p, Set<Coord3d> side) {
    List<Polygon> neighbours = getNeighboursAt(side);

    if (neighbours.size() == 2) {
      if (p.equals(neighbours.get(0))) {
        return neighbours.get(1);
      } else {
        return neighbours.get(0);
      }
    } else if (neighbours.size() == 1) {
      return null;
    } else {
      if (!except) {
        return null;
      } else {
        throw new IllegalArgumentException(
            "Can't have more than one neighbour for a pair of coordinates." + neighbours.size()
                + " around neighbourhood " + side);
      }
    }
  }

  boolean except = true;

  public List<Polygon> getNeighboursAt(Set<Coord3d> side) {
    return neighbourhood.get(side);
  }



  protected void removeTopAndBottomSideNeighbourhood(Polygon current, Coord3d[] side,
      Coord3d[] opposideSide) {
    // top side is made of one point "oppositeSideCurr" and one point of "side"
    // both set only have two points. We will pick either the actual top side, either
    // the actual bottom side, either one of the two diagonal. We don't care about
    // mixing "top" and "bottom", we just need to avoid picking a diagonal.

    int sideCurrId0 = indexOf(current, side[0]); // TODO : peut rendre externe pour le faire une
                                                 // seule fois.
    int sideCurrId1 = indexOf(current, side[1]);

    int oppositeCurrId1 = indexOf(current, opposideSide[1]);


    Set<Coord3d> topSide = null;
    Set<Coord3d> bottomSide = null;

    if (isPointIdSequence(oppositeCurrId1, sideCurrId1)) {
      topSide = Set.of(opposideSide[1], side[1]);
      bottomSide = Set.of(opposideSide[0], side[0]);
    } else if (isPointIdSequence(oppositeCurrId1, sideCurrId0)) {
      topSide = Set.of(opposideSide[1], side[0]);
      bottomSide = Set.of(opposideSide[0], side[1]);
    }

    // Try removing a neighbourhood. Missing neighbour is possible : if the shape is not closed or
    // if the neighbour is not a
    // single cell but a pair of cells
    getNeighboursAt(topSide).remove(current);
    getNeighboursAt(bottomSide).remove(current);
  }



  /////////////////////////////////////////////////////////////////////////////
  //
  // POINT AND COORD SET MANAGEMENT
  //
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Return the index of the point having the input coordinate inside the polygon, or -1 if none of
   * the point have this coordinate.
   * 
   * @param p
   * @param c
   * @return
   */
  protected int indexOf(Polygon p, Coord3d c) {
    for (int i = 0; i < p.getPoints().size(); i++) {
      if (p.getPoints().get(i).getCoord().equals(c)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Point id form a sequence if they are either following OR looping.
   * 
   * In other word, they should be either 0-1, 1-2, 2-3 or 3-0
   * 
   * @param id1
   * @param id2
   * @return
   */
  protected boolean isPointIdSequence(int id1, int id2) {
    return Math.abs(id1 - id2) == 1 || Math.abs(id1 - id2) == 3;
  }

  /**
   * Return the number of sides (edges) of the polygon, which is the number of point.
   */
  protected int numberOfSides(Polygon p) {
    return p.size();
  }

  /**
   * Returns the side (edge) of a polygon given as an ID and retrieved as a set of point, meaning it
   * does not care about the direction of the side.
   * 
   * A 4 point polygon has 4 set of 2 coords.
   */
  protected Set<Coord3d> getSide(Polygon p, int i) {
    if (i < p.size() - 1) {
      return Set.of(p.get(i).xyz, p.get(i + 1).xyz);
    } else /* if (i == p.size() - 1) */ {
      return Set.of(p.get(i).xyz, p.get(0).xyz);
    }
  }

  /** Return the set of {@link Coord3d} of the {@link Polygon} not in the exclusion set. */
  protected Set<Coord3d> excludeFromSet(Polygon p, Set<Coord3d> excluded) {
    Set<Coord3d> c1 = new HashSet<>();
    for (Point in : p.getPoints()) {
      if (!excluded.contains(in.xyz)) {
        c1.add(in.xyz);
      }
    }
    return c1;
  }

  /**
   * Return the set of {@link Point} of the {@link Polygon} that do not hold a {@link Coord3d} in
   * the exclusion set.
   */
  protected Set<Point> excludeFromSet(Collection<Point> input, Set<Coord3d> excluded) {
    Set<Point> out = new HashSet<>();
    for (Point in : input) {
      if (!excluded.contains(in.xyz)) {
        out.add(in);
      }
    }
    return out;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // POLYGON COMPARISON OPERATORS
  //
  /////////////////////////////////////////////////////////////////////////////


  /**
   * Return true if the polygons have the same set of colors without considering the neighourhood
   * points.
   * 
   * <ul>
   * <li>If {@link #neighbourColorDistanceThreshold} is strictly negative, then color matching is
   * based on strict equality of the set of colors of p1 and p2 without considering the
   * neighbourhood.
   * <li>If {@link #neighbourColorDistanceThreshold} is strictly positive, then color matching is
   * based on the mean squared distance between all points of p1 and all points of p2 except those
   * given as neighbourhood points between p1 and p2. Color sets will be considered matching if the
   * distance is inferior or equal to {@link #neighbourColorDistanceThreshold}.
   * <li>If {@link #neighbourColorDistanceThreshold} is 0, then matching never occurs and this
   * method always returns false.
   * </ul>
   * 
   * @param p1
   * @param p2
   * @param neighbourhood a collection of points that should be ignored from p1 and p2 while
   *        processing color matching.
   * @return
   */
  protected boolean matchColor(Polygon p1, Polygon p2, Collection<Coord3d> neighbourhood) {
    Set<Color> colors1 = colorsNotIn(p1, neighbourhood);
    Set<Color> colors2 = colorsNotIn(p2, neighbourhood);


    if (neighbourColorDistanceThreshold < 0) {
      return colors1.equals(colors2);
    } else if (neighbourColorDistanceThreshold > 0) {

      double cumSquareDist = 0;
      for (Color c1 : colors1) {
        for (Color c2 : colors2) {
          cumSquareDist += c1.distanceSq(c2);
        }
      }
      double meanDist = cumSquareDist / (colors1.size() * colors2.size());
      return meanDist <= neighbourColorDistanceThreshold;
    } else {
      return false;
    }
  }

  /**
   * Return polygon points colors that do not belong to the list of excluded colors.
   * 
   * @param p1
   * @param excluded
   * @return
   */
  protected Set<Color> colorsNotIn(Polygon p1, Collection<Coord3d> excluded) {
    Set<Color> c1 = new HashSet<>();
    for (Point pp1 : p1.getPoints()) {
      if (!excluded.contains(pp1.xyz)) {
        c1.add(pp1.rgb);
      }
    }
    return c1;
  }

  protected boolean matchNormals(Polygon p1, Polygon p2) {
    Coord3d n1 = normal(p1);
    Coord3d n2 = normal(p2);

    if (neighbourAngleThreshold == 0) {
      return (n1.x == n2.x && n1.y == n2.y && n1.z == n2.z)
          || (n1.z == -n2.z && n1.y == -n2.y && n1.x == -n2.x);
    } else {
      // rather compute angles with arccos(dotproduct(N1,N2))
      return new Angle3d(n1, Coord3d.ORIGIN, n2).angleD() < neighbourAngleThreshold
          || new Angle3d(n1, Coord3d.ORIGIN, n2.negative()).angleD() < neighbourAngleThreshold;
    }

  }

  protected Coord3d normal(Polygon p) {
    return Normal.compute(p.get(0).xyz, p.get(1).xyz, p.get(2).xyz, normalizeNormals);
  }  
  
  /////////////////////////////////////////////////////////////////////////////
  //
  // GET/SET
  //
  /////////////////////////////////////////////////////////////////////////////

  public double getNeighbourColorDistanceThreshold() {
    return neighbourColorDistanceThreshold;
  }

  public void setNeighbourColorDistanceThreshold(double neighbourColorDistanceThreshold) {
    this.neighbourColorDistanceThreshold = neighbourColorDistanceThreshold;
  }

  public double getNeighbourAngleThreshold() {
    return neighbourAngleThreshold;
  }

  public void setNeighbourAngleThreshold(double neighbourAngleThreshold) {
    this.neighbourAngleThreshold = neighbourAngleThreshold;
  }

  public NextCellFinder getNextCellFinder() {
    return nextCellFinder;
  }

  public void setNextCellFinder(NextCellFinder nextCellFinder) {
    this.nextCellFinder = nextCellFinder;
  }

  public PolygonBuilder getPolygonBuilder() {
    return polygonBuilder;
  }

  public void setPolygonBuilder(PolygonBuilder polygonBuilder) {
    this.polygonBuilder = polygonBuilder;
  }

  public int getNumberOfInvalidNeighbours() {
    return numberOfInvalidNeighbours;
  }

  
}
