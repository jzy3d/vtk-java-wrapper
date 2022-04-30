package org.jzy3d.maths.algorithms.decimator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import com.google.common.collect.ArrayListMultimap;

public class SurfaceFilter {
  static Logger log = LogManager.getLogger(SurfaceFilter.class);


  static boolean debug = false;

  /**
   * Filter the input list of polygon to only keep those that do not share the same coordinates set
   * with another polygon from the same input list.
   * 
   * Imagine two cubes that are next to each other : each cube will have one face with the same set
   * of vertices than the face of the other cube it is touching. These two faces are removed which
   * results in a parallelepiped.
   * 
   * NB : this will only work in the case of polygons that match exactly. Not in the case of four
   * small polygon in front of one bigger polygon covering the same area.
   * 
   * @param input
   * @return
   */
  public List<Polygon> filterHiddenPolygons(List<Polygon> input,
      boolean filterAdditiveNeighbourhood) {

    List<Polygon> step1 = filterPolygonsWithExactNeighbourhood(input);

    if (filterAdditiveNeighbourhood) {
      return filterPolygonWithAdditiveNeighbourhood(step1);
    } else {
      return step1;
    }
  }

  public List<Polygon> filterHiddenPolygons(List<Polygon> input) {
    return filterHiddenPolygons(input, false);
  }


  ///////////////////////////////////////////////////////////////
  //
  // PART 1
  //
  ///////////////////////////////////////////////////////////////


  public List<Polygon> filterPolygonsWithExactNeighbourhood(List<Polygon> input) {
    /**
     * Gather all polygons that have the same set of coordinates, regardless of coordinates order
     * Entries allow to bundle all polygon that are similar with the following map
     * 
     * {pointId_1, ..., pointId_N} -> {polygon_1, ..., polygon_N}
     */

    ArrayListMultimap<Set<Coord3d>, Polygon> similar = ArrayListMultimap.create();

    for (Polygon p : input) {
      similar.put(p.getCoordSet(), p);
    }

    /**
     * Will iterate over all entries to only keep polygons that are unique w.r.t. their coordinates
     * set
     * 
     * Following pair of polygons are both filtered out
     * 
     * <pre>
     * a     b  a     b
     * o-----o  o-----o
     * | P1  |  | P2  |
     * |     |  |     |
     * o-----o  o-----o
     * d     c  d     c
     * 
     * </pre>
     */
    List<Polygon> uniquePolygons = new ArrayList<>();

    for (Set<Coord3d> key : similar.keySet()) {
      List<Polygon> polygons = similar.get(key);

      // Only keep polygons that are unique considering their coordinates set
      if (polygons.size() == 1) {
        uniquePolygons.add(polygons.get(0));
        // similar.removeAll(key);
      }
      // Highlight when there are more than 2 (something was misunderstood)
      else if (polygons.size() > 2) {

        log.warn(key + " has " + polygons.size() + " polygons");
      }
      // Skip all twin faces
      else {
        // similar.removeAll(key);
      }
    }
    return uniquePolygons;
  }


  ///////////////////////////////////////////////////////////////
  //
  // PART 2
  //
  ///////////////////////////////////////////////////////////////


  public List<Polygon> filterPolygonWithAdditiveNeighbourhood(List<Polygon> uniquePolygons) {
    /**
     * Now will remove polygons that match a group of polygons that have common points
     * 
     * Following polygon and set of polygons are both filtered out, as long as P2, P3, P4, P5 are
     * neighbours
     * 
     * <pre>
     * a-----------b
     * | P1        | 
     * |           |
     * |           |
     * |           |
     * |           |
     * d-----------c
     *
     * a-----o-----b
     * | P2  | P3  |
     * |     |     |
     * o-----o-----o
     * | P5  | P4  |
     * |     |     |
     * d-----o-----c
     * </pre>
     * 
     * Build pointToPoly datastructure, making polygons appear as follow :
     * 
     * <ul>
     * <li>[a] -> [P1, P2]
     * <li>[b] -> [P1, P3]
     * <li>[c] -> [P1, P4]
     * <li>[d] -> [P1, P5]
     * </ul>
     * 
     * 
     * {pointId} -> {polygon_1, ..., polygon_N}
     */

    Set<Polygon> discard = new HashSet<>();

    ArrayListMultimap<Coord3d, Polygon> pointToPoly = ArrayListMultimap.create();


    for (Polygon p : uniquePolygons) {
      for (Point pt : p.getPoints()) {
        pointToPoly.put(pt.xyz, p);
      }
    }

    /** For each polygon having a neighbour sharing a SINGLE point (hence are smaller) */
    for (Polygon candidateForMultiNeighbours : uniquePolygons) {
      // skip if it is already discarded
      if (discard.contains(candidateForMultiNeighbours)) {
        continue;
      }


      ArrayListMultimap<Coord3d, Polygon> singlePointOfContactNeighboursOfCandidate =
          getSinglePointOfContactPerVertex(pointToPoly, candidateForMultiNeighbours);

      if (debug) {
        System.out.println("--------------------------------------------");
        System.out.println("Candidate : " + str(candidateForMultiNeighbours));
        System.out.println(singlePointOfContactNeighboursOfCandidate.keySet().size()
            + " neighbours with a single point of contact with the candidate");
      }

      if (singlePointOfContactNeighboursOfCandidate.keySet().size() == 4) { /////////////// OR > 4?
        /**
         * build the neighbourhood data structure to check if there is a group of polygon that match
         * the below pattern, i.e. which have two neighbour edges between each other
         * 
         * <pre>
         * .-----f-----.
         * | P2  | P3  |
         * |     |     |
         * i-----e-----g
         * | P5  | P4  |
         * |     |     |
         * .-----h-----.
         * </pre>
         * 
         * Build additiveNeighbourhood datastructure, making polygons appear as follow :
         * <ul>
         * <li>[e,f] -> [P2, P3]
         * <li>[e,g] -> [P3, P4]
         * <li>[e,h] -> [P4, P5]
         * <li>[e,i] -> [P5, P2]
         * </ul>
         * 
         * Due to the single point neighbourhood as input, we will have to ignore edges [a,a'],
         * [b,b'], ... that form neighbours that are perpendicular to our input. E.g. [bgg'b'] is
         * polygon with a single point of contact with [abcd] but not suitable for deletion since
         * not coplanar.
         * 
         * <pre>
         *    a'----f'----b'
         *   /     /     /|
         *  /     /     / |
         * a-----f-----b  g'
         * | P2  | P3  | /|
         * |     |     |/ |
         * i-----e-----g  c'
         * | P5  | P4  | /
         * |     |     |/
         * d-----h-----c
         * </pre>
         * 
         * We can deal with this by verifying if each point is ON one of the segments of the input
         * polygon. E.g. [a,f] is on [a,b].
         * 
         * We can deal with it by keeping the segments that all share the e point
         * <ul>
         * <li>which is the intersection of diagonals of abcd if poly is convex -FORGET
         * <li>which is NOT the vertices with max shared edges (all other have four as well)
         * <li>which MAY be in the bounding box of input polygon + a small margin
         * 
         */

        ArrayListMultimap<Set<Coord3d>, Polygon> additiveNeighbourhood = ArrayListMultimap.create();

        BoundingBox3d additiveNeighbourhoodZone = candidateForMultiNeighbours.getBounds().clone();
        additiveNeighbourhoodZone.selfMarginRatio(0.01f); // 1% of bounds size

        for (Coord3d pointOfContact : singlePointOfContactNeighboursOfCandidate.keySet()) {
          // pointOfContact is a, b, c or d in the above schema
          // neighbours may only be P2, P3, P4, P5
          List<Polygon> neighbours = singlePointOfContactNeighboursOfCandidate.get(pointOfContact);



          // System.out.println("- neighbours : " + neighbourhood.keySet().size());

          // List<Set<Coord3d>> sidesOfAdditiveNeighbourhood = new ArrayList<>();

          for (Polygon neighbour : neighbours) {
            // if(candidateForMultiNeighbours.equals(neighbour))
            // continue; // skip itself

            List<Coord3d> vertexForEdges = getAllPointsExcept(neighbour, pointOfContact);

            for (int i = 0; i < vertexForEdges.size() - 2; i++) {
              if (additiveNeighbourhoodZone.contains(vertexForEdges.get(i))
                  && additiveNeighbourhoodZone.contains(vertexForEdges.get(i + 1))) {
                Set<Coord3d> side = Set.of(vertexForEdges.get(i), vertexForEdges.get(i + 1));

                additiveNeighbourhood.put(side, neighbour);

                if (debug)
                  System.out.println("- side : " + side + " has neighbour " + str(neighbour));
              }
            }
          }



        }

        if (debug)
          System.out.println("Candidate has " + additiveNeighbourhood.keySet().size()
              + " additiveNeighbours sides");



        /**
         * Input polygon and its neighbours can be discarded if there are 4 neighbours
         */

        //////////////////// Il y a les 4 + ceux qui sont voisins avec d'autre polygones
        if (additiveNeighbourhood.keySet().size() == 4) {

          // TODO : SHOULD VERIFY THE EDGES CAN BE MERGED!!
          discard.add(candidateForMultiNeighbours);

          for (Polygon smallerNeighbour : additiveNeighbourhood.values()) {
            discard.add(smallerNeighbour);
          }
        }
      }

    }

    if(debug)
      System.out.println("discard length = " + discard.size());

    if (discard.size() == 0) {
      return uniquePolygons;
    } else {
      List<Polygon> nonDiscardedPolygons = new ArrayList<>();

      for (Polygon p : uniquePolygons) {
        if (!discard.contains(p)) {
          nonDiscardedPolygons.add(p);
        }
      }
      return nonDiscardedPolygons;

    }
  }

  public static String str(Polygon candidateForMultiNeighbours) {
    List<Point> pts = candidateForMultiNeighbours.getPoints();
    return str(pts);
  }

  public static String str(List<Point> pts) {
    StringBuffer sb = new StringBuffer();

    sb.append("[");
    for (int i = 0; i < pts.size(); i++) {
      Coord3d c = pts.get(i).xyz;
      sb.append("{" + c.x + "," + c.y + "," + c.z + "}");
      if (i < pts.size() - 1)
        sb.append(", ");
    }
    sb.append("]");
    return sb.toString();
  }


  ///////////////////////////////////////////////////////////////
  //
  // HELPERS
  //
  ///////////////////////////////////////////////////////////////


  /**
   * Return the polygons that have ONLY ONE point of contact for each point of the polygon. The
   * output will not contain the input polygon itself, only its neighbour per point.
   */
  public ArrayListMultimap<Coord3d, Polygon> getSinglePointOfContactPerVertex(
      ArrayListMultimap<Coord3d, Polygon> pointToPoly, Polygon p) {
    ArrayListMultimap<Coord3d, Polygon> singlePointOfContactNeighbours = ArrayListMultimap.create();

    for (Point pt : p.getPoints()) {
      Coord3d pointOfContact = pt.xyz;

      List<Polygon> singlePointNeighbours =
          getSinglePointOfContactNeighbours(pointToPoly, p, pointOfContact);

      singlePointOfContactNeighbours.putAll(pointOfContact, singlePointNeighbours);

    }

    return singlePointOfContactNeighbours;
  }

  /**
   * Return the polygons that have ONLY ONE point of contact with the polygon AT THE GIVEN point of
   * contact.
   */
  public List<Polygon> getSinglePointOfContactNeighbours(
      ArrayListMultimap<Coord3d, Polygon> pointToPoly, Polygon p, Coord3d pointOfContact) {

    List<Polygon> singlePointNeighbours = new ArrayList<>(); // output


    List<Polygon> neighbours = pointToPoly.get(pointOfContact);

    // iterate over all neighbours
    overNeighbours: for (Polygon neighbour : neighbours) {
      if (neighbour.equals(p))
        continue; // skip itself!

      boolean hasAnotherPointOfContact = false;

      // iterate over all neighbour points
      overNeighbourPoints: for (Point ptOfNeighbour : neighbour.getPoints()) {
        // skip current point of contact in the neighbour polygon
        if (ptOfNeighbour.xyz.equals(pointOfContact)) {
          continue;
        }
        // check other points than point of contact of the neighbour polygon
        else {
          for (Point ptOfPolygon : p.getPoints()) {
            // check all points except the point of contact in the base polygon
            if (!ptOfPolygon.xyz.equals(pointOfContact)) {

              // if has not found any other point of contact yet
              if (!hasAnotherPointOfContact && ptOfPolygon.xyz.equals(ptOfNeighbour.xyz)) {
                hasAnotherPointOfContact = true;
                // no need to check other points
                break overNeighbourPoints;
              }
            }
          }
        }
      }

      if (!hasAnotherPointOfContact) {
        singlePointNeighbours.add(neighbour);
      }
    }

    // once done with all neighbours, return the single point neighbours only
    return singlePointNeighbours;
  }

  /** Return the set of {@link Coord3d} of the {@link Polygon} not in the exclusion set. */
  protected static List<Coord3d> getAllPointsExcept(Polygon p, Coord3d excluded) {
    List<Coord3d> c1 = new ArrayList<>();
    for (Point in : p.getPoints()) {
      if (!excluded.equals(in.xyz)) {
        c1.add(in.xyz);
      }
    }
    return c1;
  }


  /**
   * Indicates if point belongs to segment.
   * 
   * @param segmentStart
   * @param segmentEnd
   * @param point
   * @return
   */
  public static boolean isOnSegment(Coord3d segmentStart, Coord3d segmentEnd, Coord3d point) {
    float segmentLength = (float) segmentStart.distanceSq(segmentEnd);
    float maxError = segmentLength / 10000;
    return Math.abs(segmentLength
        - ((segmentStart.distanceSq(point) + point.distanceSq(segmentEnd)))) < maxError;
  }
}
