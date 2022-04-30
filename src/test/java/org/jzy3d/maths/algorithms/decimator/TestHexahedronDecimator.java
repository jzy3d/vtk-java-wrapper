package org.jzy3d.maths.algorithms.decimator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.ChartFactory;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.junit.ChartTester;
import org.jzy3d.maths.Angle3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.RandomGeom;
import junit.framework.Assert;

/**
 * These tests will have the classpath to VTK native automatically added by Maven Surefire Plugin
 * when run with <code>mvn test</code>.
 * 
 * However, if you run them from an IDE, you may need to configure the Run Configuration so that VM
 * arguments are <code>
 * -Djava.library.path=./lib/vtk-Darwin-x86_64
 * </code>
 * 
 * @author Martin Pernollet
 */
public class TestHexahedronDecimator {
  RandomGeom geom = new RandomGeom();
  
  ////////////////////////////////////////////////////////////
  //
  // COLOR MATCHING BETWEEN TWO POLYGONS
  //
  ////////////////////////////////////////////////////////////

  @Test
  public void whenPolygonHaveSameColor_AndColorThresholdIsPositive_ThenColorMatchingSucceeds() {
    HexahedronDecimator decim = new HexahedronDecimator(0.0000000001, 0);

    // Given
    Polygon p1 = new Polygon();
    p1.add(new Point(new Coord3d(0, 0, 0), Color.BLUE));
    p1.add(new Point(new Coord3d(1, 0, 0), Color.WHITE)); // neighbor not included in processing
    p1.add(new Point(new Coord3d(1, 1, 0), Color.WHITE)); // neighbor not included in processing
    p1.add(new Point(new Coord3d(0, 1, 0), Color.BLUE));

    Polygon p2 = new Polygon();
    p2.add(new Point(new Coord3d(1, 0, 0), Color.BLACK)); // neighbor not included in processing
    p2.add(new Point(new Coord3d(2, 0, 0), Color.BLUE));
    p2.add(new Point(new Coord3d(2, 1, 0), Color.BLUE));
    p2.add(new Point(new Coord3d(1, 1, 0), Color.BLACK)); // neighbor not included in processing

    Set<Coord3d> neighbours = Set.of(new Coord3d(1, 0, 0), new Coord3d(1, 1, 0));

    // When/Then
    Assert.assertTrue(decim.matchColor(p1, p2, neighbours));

    // ---------------------
    // Given
    Polygon p3 = new Polygon();
    p3.add(new Point(new Coord3d(1, 0, 0), Color.BLACK)); // neighbor not included in processing
    p3.add(new Point(new Coord3d(2, 0, 0), Color.BLUE));
    p3.add(new Point(new Coord3d(2, 1, 0), Color.RED)); // only change color
    p3.add(new Point(new Coord3d(1, 1, 0), Color.BLACK)); // neighbor not included in processing

    // When/Then
    Assert.assertFalse(decim.matchColor(p1, p3, neighbours));

  }

  @Test
  public void whenPolygonHaveDifferentColors_AndColorThresholdIsPositive_ThenColorMatchingSucceeds() {
    HexahedronDecimator decim = new HexahedronDecimator(0.1, 0);

    // Given
    Polygon p1 = new Polygon();
    p1.add(new Point(new Coord3d(0, 0, 0), Color.BLUE.clone().mulSelf(0.0001f)));
    p1.add(new Point(new Coord3d(1, 0, 0), Color.WHITE)); // not included in processing
    p1.add(new Point(new Coord3d(1, 1, 0), Color.WHITE)); // not included in processing
    p1.add(new Point(new Coord3d(0, 1, 0), Color.BLUE.clone().mulSelf(0.0002f)));

    Polygon p2 = new Polygon();
    p2.add(new Point(new Coord3d(1, 0, 0), Color.BLACK)); // not included in processing
    p2.add(new Point(new Coord3d(2, 0, 0), Color.BLUE.clone().mulSelf(0.0001f)));
    p2.add(new Point(new Coord3d(2, 1, 0), Color.BLUE.clone().mulSelf(0.0002f)));
    p2.add(new Point(new Coord3d(1, 1, 0), Color.BLACK)); // not included in processing

    Set<Coord3d> neighbours = Set.of(new Coord3d(1, 0, 0), new Coord3d(1, 1, 0));

    // When/Then
    Assert.assertTrue(decim.matchColor(p1, p2, neighbours));

    // ---------------------
    // Given
    Polygon p3 = new Polygon();
    p3.add(new Point(new Coord3d(1, 0, 0), Color.BLUE)); // not included in processing
    p3.add(new Point(new Coord3d(2, 0, 0), Color.BLUE));
    p3.add(new Point(new Coord3d(2, 1, 0), Color.RED)); // only change color
    p3.add(new Point(new Coord3d(1, 1, 0), Color.BLUE)); // not included in processing

    // When/Then
    Assert.assertFalse(decim.matchColor(p1, p3, neighbours));

  }

  @Test
  public void whenPolygonHaveSameColor_AndColorThresholdIsNegative_ThenColorMatchingSucceeds() {
    HexahedronDecimator decim = new HexahedronDecimator(-1, 0);

    // Given
    Polygon p1 = new Polygon();
    p1.add(new Point(new Coord3d(0, 0, 0), Color.BLUE));
    p1.add(new Point(new Coord3d(1, 0, 0), Color.WHITE)); // not included in processing
    p1.add(new Point(new Coord3d(1, 1, 0), Color.WHITE)); // not included in processing
    p1.add(new Point(new Coord3d(0, 1, 0), Color.BLUE));

    Polygon p2 = new Polygon();
    p2.add(new Point(new Coord3d(1, 0, 0), Color.BLACK)); // not included in processing
    p2.add(new Point(new Coord3d(2, 0, 0), Color.BLUE));
    p2.add(new Point(new Coord3d(2, 1, 0), Color.BLUE));
    p2.add(new Point(new Coord3d(1, 1, 0), Color.BLACK)); // not included in processing

    Set<Coord3d> neighbours = Set.of(new Coord3d(1, 0, 0), new Coord3d(1, 1, 0));

    // When/Then
    Assert.assertTrue(decim.matchColor(p1, p2, neighbours));

    // ---------------------
    // Given
    Polygon p3 = new Polygon();
    p3.add(new Point(new Coord3d(1, 0, 0), Color.BLACK)); // not included in processing
    p3.add(new Point(new Coord3d(2, 0, 0), Color.BLUE));
    p3.add(new Point(new Coord3d(2, 1, 0), Color.RED)); // only change color
    p3.add(new Point(new Coord3d(1, 1, 0), Color.BLACK)); // not included in processing

    // When/Then
    Assert.assertFalse(decim.matchColor(p1, p3, neighbours));

  }


  @Test
  public void whenPolygonHaveSameColor_AndColorThreshold0_ThenColorNeverMatch() {
    HexahedronDecimator decim = new HexahedronDecimator(0, 0);

    // Given
    Polygon p1 = new Polygon();
    p1.add(new Point(new Coord3d(0, 0, 0), Color.BLUE));
    p1.add(new Point(new Coord3d(1, 0, 0), Color.BLUE)); //
    p1.add(new Point(new Coord3d(1, 1, 0), Color.BLUE)); //
    p1.add(new Point(new Coord3d(0, 1, 0), Color.BLUE));

    Polygon p2 = new Polygon();
    p2.add(new Point(new Coord3d(1, 0, 0), Color.BLUE)); //
    p2.add(new Point(new Coord3d(2, 0, 0), Color.BLUE));
    p2.add(new Point(new Coord3d(2, 1, 0), Color.BLUE));
    p2.add(new Point(new Coord3d(1, 1, 0), Color.BLUE)); //

    Set<Coord3d> neighbours = Set.of(new Coord3d(1, 0, 0), new Coord3d(1, 1, 0));

    // When/Then
    Assert.assertFalse(decim.matchColor(p1, p2, neighbours)); // FALSE!!!!
  }

  ////////////////////////////////////////////////////////////
  //
  // ANGLE MATCHING BETWEEN TWO POLYGONS
  //
  ////////////////////////////////////////////////////////////

  @Test
  public void whenPolygonsAreCoplanarNeighbours_andThresoldIs0_ThenNormalMatchingSucceeds() {
    HexahedronDecimator decim = new HexahedronDecimator(-1, 0);

    float Z = 0;

    Polygon p1 = new Polygon();
    p1.add(new Coord3d(0, 0, Z));
    p1.add(new Coord3d(1, 0, Z));
    p1.add(new Coord3d(1, 1, Z));
    p1.add(new Coord3d(0, 1, Z));

    // ---------------------------------------------------
    // Check coplanar polygon matches

    Polygon p2 = new Polygon();
    p2.add(new Coord3d(1, 0, Z)); // c0
    p2.add(new Coord3d(2, 0, Z)); // c1
    p2.add(new Coord3d(2, 1, Z)); // c2
    p2.add(new Coord3d(1, 1, Z));

    Assert.assertTrue(decim.matchNormals(p1, p2));


    // ---------------------------------------------------
    // Check points defined in reverse direction also work

    Polygon p3 = new Polygon();
    p3.add(new Coord3d(1, 1, Z)); // c0
    p3.add(new Coord3d(2, 1, Z)); // c1
    p3.add(new Coord3d(2, 0, Z)); // c2
    p3.add(new Coord3d(1, 0, Z));

    // System.out.println(decim.normal(p1));
    // System.out.println(decim.normal(p3));

    Assert.assertTrue(decim.matchNormals(p1, p3));


    // ---------------------------------------------------
    // Check non coplanar polygons are NOT matching

    Polygon p4 = new Polygon();
    p4.add(new Coord3d(1, 0, Z)); // c0
    p4.add(new Coord3d(2, 0, 1)); // c1 - shifted
    p4.add(new Coord3d(2, 1, 1)); // c2 - shifted
    p4.add(new Coord3d(1, 1, Z));

    Assert.assertFalse(decim.matchNormals(p1, p4));

  }

  @Test
  public void whenPolygonsAreCoplanarNeighbours_andThresholdPositive_ThenNormalMatchingSucceeds() {
    HexahedronDecimator decim = new HexahedronDecimator(-1, 0.1);

    float Z = 0;

    Polygon p1 = new Polygon();
    p1.add(new Coord3d(0, 0, Z));
    p1.add(new Coord3d(1, 0, Z + 0.0000001));
    p1.add(new Coord3d(1, 1, Z + 0.0000001));
    p1.add(new Coord3d(0, 1, Z));

    // ---------------------------------------------------
    // Check coplanar polygon matches

    Polygon p2 = new Polygon();
    p2.add(new Coord3d(1, 0, Z + 0.0000001)); // c0
    p2.add(new Coord3d(2, 0, Z)); // c1
    p2.add(new Coord3d(2, 1, Z)); // c2
    p2.add(new Coord3d(1, 1, Z + 0.0000001));

    Assert.assertTrue(decim.matchNormals(p1, p2));


    // ---------------------------------------------------
    // Check points defined in reverse direction also work

    Polygon p3 = new Polygon();
    p3.add(new Coord3d(1, 1, Z + 0.0000001)); // c0
    p3.add(new Coord3d(2, 1, Z)); // c1
    p3.add(new Coord3d(2, 0, Z)); // c2
    p3.add(new Coord3d(1, 0, Z + 0.0000001));

    // System.out.println(decim.normal(p1));
    // System.out.println(decim.normal(p3));

    Assert.assertTrue(decim.matchNormals(p1, p3));


    // ---------------------------------------------------
    // Check non coplanar polygons are NOT matching

    Polygon p4 = new Polygon();
    p4.add(new Coord3d(1, 0, Z)); // c0
    p4.add(new Coord3d(2, 0, Z + 1)); // c1 - shifted
    p4.add(new Coord3d(2, 1, Z + 1)); // c2 - shifted
    p4.add(new Coord3d(1, 1, Z));

    Assert.assertFalse(decim.matchNormals(p1, p4));

  }

  ////////////////////////////////////////////////////////////
  //
  // BUILD POLYGON FROM UNORDERED SET OF POINTS
  //
  ////////////////////////////////////////////////////////////



  ////////////////////////////////////////////////////////////
  //
  // MERGING POLYGONS
  //
  ////////////////////////////////////////////////////////////


  /**
   * Check can merge two polygons as shown below.
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
   */
  @Test
  public void canMergeTwoCoplanarSquares() throws IOException {
    // Given
    Polygon p1 = new Polygon();
    p1.add(new Point(new Coord3d(0, 0, 0), Color.BLUE)); // P1 left bottom
    p1.add(new Point(new Coord3d(1, 0, 0), Color.BLUE)); // neighbor edge will be dropped
    p1.add(new Point(new Coord3d(1, 1, 0), Color.BLUE)); // neighbor edge will be dropped
    p1.add(new Point(new Coord3d(0, 1, 0), Color.BLUE)); // P1 left top

    Polygon p2 = new Polygon();
    p2.add(new Point(new Coord3d(1, 0, 0), Color.BLUE)); // neighbor edge will be dropped
    p2.add(new Point(new Coord3d(2, 0, 0), Color.BLUE)); // P2 right bottom
    p2.add(new Point(new Coord3d(2, 1, 0), Color.BLUE)); // P2 right top
    p2.add(new Point(new Coord3d(1, 1, 0), Color.BLUE)); // neighbor edge will be dropped

    Set<Coord3d> sharedSide = Set.of(new Coord3d(1, 0, 0), new Coord3d(1, 1, 0));

    // When merging
    HexahedronDecimator decim = new HexahedronDecimator();
    Polygon merged = decim.mergeNeighbourRectangles(p1, sharedSide, p2);

    Chart c = new EmulGLChartFactory().newChart();
    c.add(merged);
    c.screenshot(new File("target/merged.png"));

    // Then
    Set<Coord3d> expected = new HashSet<>();
    expected.add(new Coord3d(0, 0, 0));
    expected.add(new Coord3d(2, 0, 0));
    expected.add(new Coord3d(2, 1, 0));
    expected.add(new Coord3d(0, 1, 0));

    Assert.assertEquals(expected, merged.getCoordSet());
    Assert.assertTrue(Angle3d.angleSumFromPointsOfNonIntersectingPolygon(merged.getPoints()));

    // ------------------------------------------
    // Given the same polygon with POINTS SHIFTED
    Polygon p3 = new Polygon();
    p3.add(new Point(new Coord3d(2, 0, 0), Color.BLUE)); // P2 right bottom
    p3.add(new Point(new Coord3d(2, 1, 0), Color.BLUE)); // P2 right top
    p3.add(new Point(new Coord3d(1, 1, 0), Color.BLUE)); // neighbor edge will be dropped
    p3.add(new Point(new Coord3d(1, 0, 0), Color.BLUE)); // neighbor edge will be dropped


    // When merging
    merged = decim.mergeNeighbourRectangles(p1, sharedSide, p3);

    // Then appropriate points are kept
    Assert.assertEquals(expected, merged.getCoordSet());
    Assert.assertTrue(Angle3d.angleSumFromPointsOfNonIntersectingPolygon(merged.getPoints()));

    // ------------------------------------------
    // Given the same polygon with POINTS SHIFTED
    Polygon p4 = new Polygon();
    p4.add(new Point(new Coord3d(2, 1, 0), Color.BLUE)); // P2 right top
    p4.add(new Point(new Coord3d(1, 1, 0), Color.BLUE)); // neighbor edge will be dropped
    p4.add(new Point(new Coord3d(1, 0, 0), Color.BLUE)); // neighbor edge will be dropped
    p4.add(new Point(new Coord3d(2, 0, 0), Color.BLUE)); // P2 right bottom

    // When merging
    merged = decim.mergeNeighbourRectangles(p1, sharedSide, p4);

    // Then appropriate points are kept
    Assert.assertEquals(expected, merged.getCoordSet());
    Assert.assertTrue(Angle3d.angleSumFromPointsOfNonIntersectingPolygon(merged.getPoints()));


    // ------------------------------------------
    // Given the same polygon with POINTS SHIFTED
    Polygon p9 = new Polygon();
    p9.add(new Point(new Coord3d(1, 0, 0), Color.BLUE)); // neighbor edge will be dropped
    p9.add(new Point(new Coord3d(1, 1, 0), Color.BLUE)); // neighbor edge will be dropped
    p9.add(new Point(new Coord3d(0, 1, 0), Color.BLUE)); // P1 left top
    p9.add(new Point(new Coord3d(0, 0, 0), Color.BLUE)); // P1 left bottom

    // When merging
    merged = decim.mergeNeighbourRectangles(p9, sharedSide, p2);

    // Then appropriate points are kept
    Assert.assertEquals(expected, merged.getCoordSet());
    Assert.assertTrue(Angle3d.angleSumFromPointsOfNonIntersectingPolygon(merged.getPoints()));

    // When merging
    merged = decim.mergeNeighbourRectangles(p9, sharedSide, p3);

    // Then appropriate points are kept
    Assert.assertEquals(expected, merged.getCoordSet());
    Assert.assertTrue(Angle3d.angleSumFromPointsOfNonIntersectingPolygon(merged.getPoints()));

    // When merging
    merged = decim.mergeNeighbourRectangles(p9, sharedSide, p4);

    // Then appropriate points are kept
    Assert.assertEquals(expected, merged.getCoordSet());
    Assert.assertTrue(Angle3d.angleSumFromPointsOfNonIntersectingPolygon(merged.getPoints()));



    // ------------------------------------------
    // Given the same polygon with POINTS SHIFTED + REVERSE DIRECTION
    Polygon p8 = new Polygon();
    p8.add(new Point(new Coord3d(1, 1, 0), Color.BLUE)); // neighbor edge will be dropped
    p8.add(new Point(new Coord3d(1, 0, 0), Color.BLUE)); // neighbor edge will be dropped
    p8.add(new Point(new Coord3d(0, 0, 0), Color.BLUE)); // P1 left bottom
    p8.add(new Point(new Coord3d(0, 1, 0), Color.BLUE)); // P1 left top

    // When merging
    merged = decim.mergeNeighbourRectangles(p8, sharedSide, p2);

    // Then appropriate points are kept
    Assert.assertEquals(expected, merged.getCoordSet());
    Assert.assertTrue(Angle3d.angleSumFromPointsOfNonIntersectingPolygon(merged.getPoints()));

    // When merging
    merged = decim.mergeNeighbourRectangles(p8, sharedSide, p3);

    // Then appropriate points are kept
    Assert.assertEquals(expected, merged.getCoordSet());
    Assert.assertTrue(Angle3d.angleSumFromPointsOfNonIntersectingPolygon(merged.getPoints()));

    // When merging
    merged = decim.mergeNeighbourRectangles(p8, sharedSide, p4);

    // Then appropriate points are kept
    Assert.assertEquals(expected, merged.getCoordSet());
    Assert.assertTrue(Angle3d.angleSumFromPointsOfNonIntersectingPolygon(merged.getPoints()));

  }

  /**
   * Check can merge two polygons as shown below.
   * 
   * Input
   * 
   * <pre>
   * o---------------o-------o
   * |               |       |
   * |               |       |
   * |               |       |
   * o---------------o-------o
   * </pre>
   * 
   * 
   * Output
   * 
   * <pre>
   * o-----------------------o
   * |                       |
   * |                       |
   * |                       |
   * o-----------------------o
   * </pre>
   */
  @Test
  public void canMergeOneRectangleAndOneSquare() {
    // Given
    Polygon p1 = new Polygon();
    p1.add(new Point(new Coord3d(0, 0, 0), Color.BLUE)); // P1 left bottom
    p1.add(new Point(new Coord3d(2, 0, 0), Color.BLUE)); // neighbor edge will be dropped
    p1.add(new Point(new Coord3d(2, 1, 0), Color.BLUE)); // neighbor edge will be dropped
    p1.add(new Point(new Coord3d(0, 1, 0), Color.BLUE)); // P1 left top

    Polygon p2 = new Polygon();
    p2.add(new Point(new Coord3d(2, 0, 0), Color.BLUE)); // neighbor edge will be dropped
    p2.add(new Point(new Coord3d(3, 0, 0), Color.BLUE)); // P2 right bottom
    p2.add(new Point(new Coord3d(3, 1, 0), Color.BLUE)); // P2 right top
    p2.add(new Point(new Coord3d(2, 1, 0), Color.BLUE)); // neighbor edge will be dropped

    Set<Coord3d> sharedSide = Set.of(new Coord3d(2, 0, 0), new Coord3d(2, 1, 0));

    // When merging
    HexahedronDecimator decim = new HexahedronDecimator();
    Polygon merged = decim.mergeNeighbourRectangles(p1, sharedSide, p2);


    // Then
    Set<Coord3d> expected = new HashSet<>();
    expected.add(new Coord3d(0, 0, 0));
    expected.add(new Coord3d(3, 0, 0));
    expected.add(new Coord3d(3, 1, 0));
    expected.add(new Coord3d(0, 1, 0));

    Assert.assertEquals(expected, merged.getCoordSet());
  }


  /**
   * Merge a sequence of polygons and interrupts when color changes.
   * 
   * Input
   * 
   * <pre>
   * o-------o-------o-------o-------o-------o
   * |       |       |       |       |       |
   * |   B   |   B   |   B   |   R   |   R   |
   * |       |       |       |       |       |
   * o-------o-------o-------o-------o-------o
   * </pre>
   * 
   * 
   * Output
   * 
   * <pre>
   * o-----------------------o---------------o
   * |                       |               |
   * |           B           |       R       |
   * |                       |               |
   * o-----------------------o---------------o
   * </pre>
   */
  @Test
  public void whenSequenceOfColor_mergeLineStrip() {
    List<Polygon> polys = new ArrayList<>();
    polys.add(geom.poly(0, 0, 0, true, Color.BLUE));
    polys.add(geom.poly(1, 0, 0, true, Color.BLUE));
    polys.add(geom.poly(2, 0, 0, true, Color.BLUE));
    polys.add(geom.poly(3, 0, 0, true, Color.RED));
    polys.add(geom.poly(4, 0, 0, true, Color.RED));


    ChartTester tester = new ChartTester();
    Chart c = newDebugChart();
    c.add(polys);

    tester.setTextInvisible(true);
    tester.assertSimilar(c, tester.path("whenSequenceOfColor_mergeLineStrip_In.png"));
    c.remove(polys);


    HexahedronDecimator decim = new HexahedronDecimator();
    List<Polygon> merged = decim.mergeNeighbours(polys);

    c.add(merged);

    tester.assertSimilar(c,
        tester.path("whenSequenceOfColor_mergeLineStrip_Out.png"));

    Assert.assertEquals(2, merged.size());
  }

  /**
   * Merge a sequence of polygons and interrupts when color OR angle changes.
   * 
   * Input
   * 
   * <pre>
   *     o-------o-------o-------o-------o-------o
   *    /| R     | R     | R     | B     | B    /|
   *   o |       |       |       |       |     o |
   *  / B                                     / R|
   * o-------o-------o-------o-------o-------o | o
   * |       |       |       |       |       |R /
   * |   B   |   B   |   B   |   R   |   R   | o
   * |       |       |       |       |       |/
   * o-------o-------o-------o-------o-------o
   * </pre>
   * 
   * 
   * Output
   * 
   * <pre>
   *     o-----------------------o---------------o
   *    /| R                     | B            /|
   *   / |                       |             / |
   *  / B                                     / R|
   * o-----------------------o---------------o   o
   * |                       |               |  /
   * |           B           |       R       | /
   * |                       |               |/
   * o-----------------------o---------------o
   * </pre>
   */
  @Test
  public void whenSequenceOfColorAndCorners_mergeLineLoop() {
    ChartTester tester = new ChartTester();
    Chart c = newDebugChart();

    // -------------------------------
    // Given
    List<Polygon> in = new ArrayList<>();

    // front side
    in.add(geom.poly(0, 0, 0, true, Color.BLUE)); // go right
    in.add(geom.poly(1, 0, 0, true, Color.BLUE));
    in.add(geom.poly(2, 0, 0, true, Color.BLUE));
    in.add(geom.poly(3, 0, 0, true, Color.RED));
    in.add(geom.poly(4, 0, 0, true, Color.RED));

    // right side
    in.add(geom.poly(5, 0, 0, false, Color.RED)); // go deep
    in.add(geom.poly(5, 0, 1, false, Color.RED));

    // back side
    in.add(geom.poly(0, 0, 2, true, Color.RED)); // go right
    in.add(geom.poly(1, 0, 2, true, Color.RED));
    in.add(geom.poly(2, 0, 2, true, Color.RED));
    in.add(geom.poly(3, 0, 2, true, Color.BLUE));
    in.add(geom.poly(4, 0, 2, true, Color.BLUE));

    // left side
    in.add(geom.poly(0, 0, 0, false, Color.BLUE)); // go deep
    in.add(geom.poly(0, 0, 1, false, Color.BLUE));

    // ---------------
    // Then
    int EXPECT_INPUT_CELLS = 14;

    Assert.assertEquals(EXPECT_INPUT_CELLS, in.size());

    // ---------------
    // Then
    c.add(in);

    String imgIn = "whenSequenceOfColorAndCorners_mergeLineLoop_In.png";

    tester.setTextInvisible(true);
    tester.assertSimilar(c, tester.path(imgIn));
    c.remove(in);

    // ------------------------------
    // Given
    HexahedronDecimator decim = new HexahedronDecimator();

    // ---------------
    // When
    decim.initNeighbourhoodInternal(in);

    // <<<< debug
    // showNeighbours(c, decim, Color.YELLOW);
    // String imgIn = "whenSequenceOfColorAndCorners_mergeLineLoop_In.png";
    // tester.assertSimilar(c, tester.path(imgIn);
    // debug >>>>

    // Then
    int EXPECT_SHARED_SIDES = EXPECT_INPUT_CELLS * 1; // LEFT AND RIGHT ARE SHARED
    int EXPECT_NOT_SHARED_SIDES = EXPECT_INPUT_CELLS * 2; // TOP AND BOTTOM ARE NOT SHARED

    Assert.assertEquals(EXPECT_SHARED_SIDES + EXPECT_NOT_SHARED_SIDES,
        decim.getNeighbourhoodSides().size());

    // ---------------
    // When
    List<Polygon> out = decim.mergeNeighbours(in);

    c.add(out);

    // Then
    int EXPECT_OUTPUT_CELLS = 6;
    int EXPECT_OUTPUT_SIDES = EXPECT_OUTPUT_CELLS * 1; /* LEFT AND RIGHT */
    // TOP AND BOTTOM HAVE BEEN DELETED

    Assert.assertEquals(EXPECT_OUTPUT_CELLS, out.size());
    Assert.assertEquals(EXPECT_OUTPUT_SIDES, decim.getNeighbourhoodSides().size());


    // <<<< debug
    showNeighbours(c, decim, Color.YELLOW);
    // debug >>>>

    String imgOut = "whenSequenceOfColorAndCorners_mergeLineLoop_Out.png";
    tester.assertSimilar(c, tester.path(imgOut));
  }

  protected void showNeighbours(Chart c, HexahedronDecimator decim, Color color) {
    for (Set<Coord3d> side : decim.getNeighbourhoodSides()) {
      LineStrip ls = new LineStrip();
      ls.addAllPoints(side);
      ls.setColor(color);
      c.add(ls);
    }
  }

  /**
   * This test has two rows similar to the one created in
   * {@link #whenSequenceOfColorAndCorners_mergeLineLoop()}
   * 
   * Output
   * 
   * <pre>
   *     o-----------------------o---------------o
   *    /                                       /|
   *   /                                       / |
   *  /                                       / R|
   * o-----------------------o---------------o   o
   * |                       |               |  /|
   * |           B           |       R       | / |
   * |                       |               |/ R|
   * o-----------------------o---------------o   o
   * |                       |               |  /
   * |           B           |       R       | /
   * |                       |               |/
   * o-----------------------o---------------o
   * </pre>
   */
  @Test
  public void whenSequenceOfColorAndCorners_mergeLineLoop2() {
    ChartTester tester = new ChartTester();
    tester.setTextInvisible(true);
    
    Chart c = newDebugChart();
   

    // --------------------------
    // Given
    List<Polygon> in = new ArrayList<>();

    // first row
    in.add(geom.poly(0, 0, 0, true, Color.BLUE)); // go right
    in.add(geom.poly(1, 0, 0, true, Color.BLUE));
    in.add(geom.poly(2, 0, 0, true, Color.BLUE));
    in.add(geom.poly(3, 0, 0, true, Color.RED));
    in.add(geom.poly(4, 0, 0, true, Color.RED));
    in.add(geom.poly(5, 0, 0, false, Color.RED)); // go deep
    in.add(geom.poly(5, 0, 1, false, Color.RED));
    in.add(geom.poly(0, 0, 2, true, Color.RED)); // go right
    in.add(geom.poly(1, 0, 2, true, Color.RED));
    in.add(geom.poly(2, 0, 2, true, Color.RED));
    in.add(geom.poly(3, 0, 2, true, Color.BLUE));
    in.add(geom.poly(4, 0, 2, true, Color.BLUE));
    in.add(geom.poly(0, 0, 0, false, Color.BLUE)); // go deep
    in.add(geom.poly(0, 0, 1, false, Color.BLUE));

    // second row
    in.add(geom.poly(0, 1, 0, true, Color.BLUE)); // go right
    in.add(geom.poly(1, 1, 0, true, Color.BLUE));
    in.add(geom.poly(2, 1, 0, true, Color.RED));
    in.add(geom.poly(3, 1, 0, true, Color.RED));
    in.add(geom.poly(4, 1, 0, true, Color.RED));
    in.add(geom.poly(5, 1, 0, false, Color.BLUE)); // go deep
    in.add(geom.poly(5, 1, 1, false, Color.BLUE));
    in.add(geom.poly(0, 1, 2, true, Color.RED)); // go right
    in.add(geom.poly(1, 1, 2, true, Color.RED));
    in.add(geom.poly(2, 1, 2, true, Color.BLUE));
    in.add(geom.poly(3, 1, 2, true, Color.BLUE));
    in.add(geom.poly(4, 1, 2, true, Color.BLUE));
    in.add(geom.poly(0, 1, 0, false, Color.RED)); // go deep
    in.add(geom.poly(0, 1, 1, false, Color.RED));


    // ---------------
    // Then
    int EXPECT_INPUT_CELLS = 14 * 2;

    Assert.assertEquals(EXPECT_INPUT_CELLS, in.size());

    c.add(in);
    String imgIn = "whenSequenceOfColorAndCorners_mergeLineLoop2_In.png";
    tester.assertSimilar(c, tester.path(imgIn));
    c.remove(in);

    // ---------------
    // Given
    HexahedronDecimator decim = new HexahedronDecimator();

    // ---------------
    // When
    decim.initNeighbourhoodInternal(in);

    // <<<< debug 
    // showNeighbours(c, decim, Color.YELLOW);
    // String imgIn = "whenSequenceOfColorAndCorners_mergeLineLoop_In.png";
    // tester.assertSimilar(c, tester.path(imgIn);
    // debug >>>>

    // ---------------
    // Then
    int EXPECT_SHARED_SIDES = EXPECT_INPUT_CELLS * 1; // LEFT AND RIGHT ARE SHARED
    int EXPECT_NOT_SHARED_SIDES = (int) (EXPECT_INPUT_CELLS * 1.5); // TOP AND BOTTOM ARE NOT SHARED
                                                                    // BUT MIDDLE IS

    Assert.assertEquals(EXPECT_SHARED_SIDES + EXPECT_NOT_SHARED_SIDES,
        decim.getNeighbourhoodSides().size());

    // ---------------
    // When

    List<Polygon> out = decim.mergeNeighbours(in);


    // ---------------
    // Then
    int EXPECT_OUTPUT_CELLS = 6 * 2;
    int EXPECT_OUTPUT_SIDES = EXPECT_OUTPUT_CELLS * 1; /* LEFT AND RIGHT */
    // TOP AND BOTTOM HAVE BEEN DELETED

    Assert.assertEquals(EXPECT_OUTPUT_CELLS, out.size());
    Assert.assertEquals(EXPECT_OUTPUT_SIDES, decim.getNeighbourhoodSides().size());

    c.add(out);
    String imgOut = "whenSequenceOfColorAndCorners_mergeLineLoop2_Out.png";
    tester.assertSimilar(c, tester.path(imgOut));
  }

  ////////////////////////////////////////////////////////////
  //
  // HELPERS IN DECIMATOR
  //
  ////////////////////////////////////////////////////////////

  @Test
  public void isPointIdSequence() {
    HexahedronDecimator decim = new HexahedronDecimator();

    Assert.assertTrue(decim.isPointIdSequence(0, 1));
    Assert.assertTrue(decim.isPointIdSequence(1, 2));
    Assert.assertTrue(decim.isPointIdSequence(2, 3));
    Assert.assertTrue(decim.isPointIdSequence(3, 0));

    Assert.assertTrue(decim.isPointIdSequence(1, 0));
    Assert.assertTrue(decim.isPointIdSequence(2, 1));
    Assert.assertTrue(decim.isPointIdSequence(3, 2));
    Assert.assertTrue(decim.isPointIdSequence(0, 3));

    Assert.assertFalse(decim.isPointIdSequence(0, 2));
    Assert.assertFalse(decim.isPointIdSequence(1, 3));
    Assert.assertFalse(decim.isPointIdSequence(2, 0));
    Assert.assertFalse(decim.isPointIdSequence(3, 1));

    Assert.assertFalse(decim.isPointIdSequence(2, 0));
    Assert.assertFalse(decim.isPointIdSequence(3, 1));
    Assert.assertFalse(decim.isPointIdSequence(0, 2));
    Assert.assertFalse(decim.isPointIdSequence(1, 3));
  }

  @Test
  public void getSide() {
    Polygon p = geom.poly(0, 0, 0, true, Color.BLUE);

    HexahedronDecimator decim = new HexahedronDecimator();

    // When/Then
    Set<Coord3d> side0 = decim.getSide(p, 0);
    Assert.assertTrue(side0.contains(new Coord3d(0, 0, 0)));
    Assert.assertTrue(side0.contains(new Coord3d(1, 0, 0)));

    // When/Then
    Set<Coord3d> side1 = decim.getSide(p, 1);
    Assert.assertTrue(side1.contains(new Coord3d(1, 0, 0)));
    Assert.assertTrue(side1.contains(new Coord3d(1, 1, 0)));

    // When/Then
    Set<Coord3d> side2 = decim.getSide(p, 2);
    Assert.assertTrue(side2.contains(new Coord3d(1, 1, 0)));
    Assert.assertTrue(side2.contains(new Coord3d(0, 1, 0)));

    // When/Then
    Set<Coord3d> side3 = decim.getSide(p, 3);
    Assert.assertTrue(side3.contains(new Coord3d(0, 1, 0)));
    Assert.assertTrue(side3.contains(new Coord3d(0, 0, 0)));

  }

  ////////////////////////////////////////////////////////////
  //
  // UTILS
  //
  ////////////////////////////////////////////////////////////


  public static Chart newDebugChart() {
    ChartFactory f = new EmulGLChartFactory();
    f.getPainterFactory().setOffscreen(600, 400);
    Chart c = f.newChart();
    return c;
  }
}
