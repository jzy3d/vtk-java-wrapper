package org.jzy3d.io.vtk.drawable;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Normal.NormalPer;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.primitives.Geometry;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.Shape;
import vtk.VTKGeometry;
import vtk.VTKReader;
import vtk.VTKUtils;
import vtk.vtkCellArrayIterator;
import vtk.vtkDataArray;
import vtk.vtkPointData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnstructuredGrid;

/**
 * Load VTK data structures into a list of Jzy3D {@link Polygon} for direct rendering mode.
 * 
 * <img src="./doc-files/vtkLinearCells.png"/>
 * 
 * @author Martin Pernollet
 *
 */
public class VTKDrawableBuilder extends AbstractVTKDrawableBuilder implements IDrawableBuilder {
  protected static Logger log = Logger.getLogger(VTKDrawableBuilder.class);


  protected Coord3d[] coordinates;
  protected Coord3d[] normals;
  protected boolean isNormalPerPoint;

  protected boolean debug = false;


  public VTKDrawableBuilder(vtkUnstructuredGrid ugrid) {
    this(ugrid, null);
  }

  public VTKDrawableBuilder(vtkPolyData poly) {
    this(poly, null);
  }

  /**
   * 
   * @param unstructuredGrid
   * @param normalArray an array of normal that will be ignored if null
   */
  public VTKDrawableBuilder(vtkUnstructuredGrid unstructuredGrid, vtkDataArray normalArray) {
    super(unstructuredGrid);

    loadPointsAndNormals(unstructuredGrid.GetPoints(), normalArray);
  }

  /**
   * 
   * @param polygons
   * @param normalArray an array of normal that will be ignored if null
   */
  public VTKDrawableBuilder(vtkPolyData polygons, vtkDataArray normalArray) {
    super(polygons);

    loadPointsAndNormals(polygons.GetPoints(), normalArray);
  }

  protected void loadPointsAndNormals(vtkPoints points, vtkDataArray normalArray) {
    this.coordinates = VTKReader.toCoord3d(points);

    if (normalArray != null) {
      this.normals = VTKReader.toCoord3d(normalArray);
      this.isNormalPerPoint = (normals.length == coordinates.length);
      
      System.out.println("#Coords : " + coordinates.length + " #Normals : " + normals.length + " #Cells : " + cells.GetNumberOfCells() + " #Polygons (guess) : " + cells.GetNumberOfCells()*HEXAHEDRON_FACES);
    } else {
      this.normals = null;
    }
  }
  

  public Scatter makeScatter() {
    Scatter points = new Scatter();
    points.setData(coordinates);
    points.setWidth(3);
    return points;
  }
  
  public List<LineStrip> makeNormalsAsLines(float ratio) {
    List<LineStrip> lines = new ArrayList<>();

    for (int i = 0; i < coordinates.length; i++) {
      Coord3d base = coordinates[i];
      Coord3d normal = normals[i];
      normal.divSelf(normal.distance(base)*ratio);
      Coord3d extremity = base.add(normal);
      LineStrip ls = new LineStrip(new Point(base, Geometry.NORMAL_START_COLOR), new Point(extremity, Geometry.NORMAL_END_COLOR));
      lines.add(ls);
    }
    return lines;
  }

  public Shape makeShape(String property) {
    return new Shape(makePolygons(property));
  }
  
  /**
   * 
   * Traversing the cell array explained here :
   * https://vtk.org/doc/nightly/html/classvtkCellArray.html#a2841af7d1aae4c8db8544b2317dc712b
   * 
   * @param property name of the array to read for making colors
   * @return
   */
  public List<Polygon> makePolygons(String property) {

    // --------------------------------
    // Get values for this property
    vtkPointData pointData = dataset.GetPointData();

    // Loosing precision here since convert double to float
    vtkDataArray propertyArray = pointData.GetArray(property);
    if(propertyArray==null) {
      String[] a = VTKUtils.getArrayNames(pointData);
      throw new IllegalArgumentException("Property '" + property + "' not found. Use one of : " + String.join(" ", a));
    }
    int numberOfTuples = (int)pointData.GetNumberOfTuples();
    float[] coloringProperty = VTKUtils.toFloatArray(propertyArray, numberOfTuples);

    propertyRange = new Range(propertyArray.GetFiniteRange()[0], propertyArray.GetFiniteRange()[1]);


    // ----------------------------------------------------
    // Build polygons by iterating on cells

    List<Polygon> polygons = new ArrayList<>();

    /////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////

    // start iteration
    vtkCellArrayIterator it = cells.NewIterator();
    
    int k = 0;

    for (it.GoToFirstCell(); !it.IsDoneWithTraversal(); it.GoToNextCell()) {
      int cellId = (int)it.GetCurrentCellId();
      int cellType = dataset.GetCellType(cellId);
      int cellStartPointId = (int) cells.GetOffsetsArray().GetTuple1(cellId);
      int cellStopPointId = (int) cells.GetOffsetsArray().GetTuple1(cellId + 1);

      if (debug)
        debugCurrentCell(cellId, cellType, cellStartPointId, cellStopPointId);

      /////////////////////////////////////////////////////

      if (VTKGeometry.VTK_TRIANGLE == cellType) {
        addPolygon(polygons, cellId, cellStartPointId, cellStopPointId, coloringProperty);
      }

      /////////////////////////////////////////////////////

      else if (VTKGeometry.VTK_QUAD == cellType) {
        addPolygon(polygons, cellId, cellStartPointId, cellStopPointId, coloringProperty);
      }

      /////////////////////////////////////////////////////

      else if (VTKGeometry.VTK_HEXAHEDRON == cellType) {
        addHexahedronPolygons(polygons, cellId, cellStartPointId, cellStopPointId, coloringProperty);
      }

      /////////////////////////////////////////////////////

      else {
        log.error("Unsupported cell type " + cellType + " (" + VTKGeometry.name(cellType) + ")" );
      }
      
      if(k==0) {
        log.info("First geometry is " + VTKGeometry.name(cellType));
        k++;
      }
    }

    return polygons;
  }


  /**
   * Load an hexahedron.
   * 
   * @param polygons the list of polygons to fill
   * @param cellId id of the cell
   * @param cellStartPointId starting cell
   * @param cellStopPointId stoping cell
   * @param coloringProperty array of properties in the VTK file that should be used for coloring
   *        the faces of the hexahedron polygons
   */
  protected void addHexahedronPolygons(List<Polygon> polygons, int cellId,
      int cellStartPointId, int cellStopPointId, float[] coloringProperty) {
    
    // --------------------------------------
    // Check consistency of input
    if (cellStopPointId - cellStartPointId != HEXAHEDRON_POINTS) {
      throw new IllegalArgumentException(
          "Hexahedron supposed to have 8 points, not " + (cellStopPointId - cellStartPointId));
    }

    // --------------------------------------
    // Prepare array to load points and normals
    Point[] hexaHedronPoints = new Point[HEXAHEDRON_POINTS];
    Coord3d[] hexahedronNormals = null;

    if (this.normals != null) {
      if(isNormalPerPoint) {
        hexahedronNormals = new Coord3d[HEXAHEDRON_POINTS];
      }
      else {
        hexahedronNormals = new Coord3d[HEXAHEDRON_FACES];
      }
    }

    // --------------------------------------
    // Load coordinates, colors and normals
    for (int i = cellStartPointId; i < cellStopPointId; i++) {
      // Index in input cell and output hexahedron points
      int datasetPointId = (int) cells.GetConnectivityArray().GetTuple1(i);
      int hexahedPointId = i - cellStartPointId;

      if (debug)
        System.out.print(datasetPointId + " ");

      // Get coordinate
      float value = coloringProperty[datasetPointId];
      Coord3d coord = coordinates[datasetPointId];

      // Get color
      Color color = getValueColor(value);
      
      hexaHedronPoints[hexahedPointId] = new Point(coord, color);

      // Get normal if available
      if (this.normals != null) {
        if (isNormalPerPoint) {
          hexahedronNormals[hexahedPointId] = this.normals[datasetPointId];
        } 
        // normal per cell retrieved later
      }
    }
    
    if (debug)
      System.out.println();


    // --------------------------------------
    // Append hexahedron faces to polygon
    
    // Case of normal to be processed automatically
    if(normals==null) {
      polygons.add(newPolygon(hexaHedronPoints, 2, 3, 7, 6)); // north
      polygons.add(newPolygon(hexaHedronPoints, 0, 1, 5, 4)); // south
      polygons.add(newPolygon(hexaHedronPoints, 4, 5, 6, 7)); // left
      polygons.add(newPolygon(hexaHedronPoints, 0, 1, 2, 3)); // right
      polygons.add(newPolygon(hexaHedronPoints, 0, 3, 7, 4)); // near
      polygons.add(newPolygon(hexaHedronPoints, 1, 2, 6, 5)); // far
      
    }
    else {
      // Case of normal provided by point
      if(isNormalPerPoint) {
        polygons.add(newPolygon(hexaHedronPoints, hexahedronNormals, 2, 3, 7, 6)); // north
        polygons.add(newPolygon(hexaHedronPoints, hexahedronNormals, 0, 1, 5, 4)); // south
        polygons.add(newPolygon(hexaHedronPoints, hexahedronNormals, 4, 5, 6, 7)); // left
        polygons.add(newPolygon(hexaHedronPoints, hexahedronNormals, 0, 1, 2, 3)); // right
        polygons.add(newPolygon(hexaHedronPoints, hexahedronNormals, 0, 3, 7, 4)); // near
        polygons.add(newPolygon(hexaHedronPoints, hexahedronNormals, 1, 2, 6, 5)); // far
      }
      // Case of normal provided by polygon
      else {
        polygons.add(newPolygon(hexaHedronPoints, normals[hexaPolyId(cellId, vtkHexNorth)], 2, 3, 7, 6)); // north - toward Y max
        polygons.add(newPolygon(hexaHedronPoints, normals[hexaPolyId(cellId, vtkHexSouth)], 0, 1, 5, 4)); // south - toward Y min
        polygons.add(newPolygon(hexaHedronPoints, normals[hexaPolyId(cellId, vtkHexLeft)], 4, 5, 6, 7)); // left - toward Z max
        polygons.add(newPolygon(hexaHedronPoints, normals[hexaPolyId(cellId, vtkHexRight)], 0, 1, 2, 3)); // right - toward X min
        polygons.add(newPolygon(hexaHedronPoints, normals[hexaPolyId(cellId, vtkHexNear)], 0, 3, 7, 4)); // near
        polygons.add(newPolygon(hexaHedronPoints, normals[hexaPolyId(cellId, vtkHexFar)], 1, 2, 6, 5)); // far  
        // actual axis depends on how the cubes where exported. In our case, Libmesh seams to not provide 
        // cubes in the same direction
      }
    }
  }

  
  protected int hexaPolyId(int cellId, int polyIdInHexahedron) {
    return cellId * HEXAHEDRON_FACES + polyIdInHexahedron;
  }

  
  //////////////////////////////////////////////
  //
  //
  //
  //////////////////////////////////////////////

  protected Polygon newPolygon(Point[] pts, Coord3d[] normals, int p1, int p2, int p3, int p4) {
    Polygon p = new Polygon(wireColor, wireDisplayed, pts[p1], pts[p2], pts[p3], pts[p4]);
    configurePolygon(p);

    if (normals != null) {
      List<Coord3d> normalList = new ArrayList<>();
      normalList.add(normals[p1]);
      normalList.add(normals[p2]);
      normalList.add(normals[p3]);
      normalList.add(normals[p4]);

      p.setNormals(normalList, NormalPer.POINT);
    }
    return p;
  }
  
  protected Polygon newPolygon(Point[] pts, int p1, int p2, int p3, int p4) {
    Polygon p = new Polygon(wireColor, wireDisplayed, pts[p1], pts[p2], pts[p3], pts[p4]);
    configurePolygon(p);
    return p;
  }
  
  protected Polygon newPolygon(Point[] pts, Coord3d normals, int p1, int p2, int p3, int p4) {
    Polygon p = new Polygon(wireColor, wireDisplayed, pts[p1], pts[p2], pts[p3], pts[p4]);
    configurePolygon(p);

    if (normals != null) {
      List<Coord3d> normalList = new ArrayList<>();
      normalList.add(normals);
      p.setNormals(normalList, NormalPer.GEOMETRY);
    }
    return p;
  }


  protected void addPolygon(List<Polygon> polygons, int cellId, int cellStartPointId,
      int cellStopPointId, float[] coloringProperty) {

    Polygon polygon = new Polygon();

    // Polygon settings
    configurePolygon(polygon);

    for (int i = cellStartPointId; i < cellStopPointId; i++) {
      int pointId = (int) cells.GetConnectivityArray().GetTuple1(i);

      if (debug)
        System.out.print(pointId + " ");

      // Get coordinate
      float value = coloringProperty[pointId];
      Coord3d coord = coordinates[pointId];

      // Get color
      Color color = getValueColor(value);

      // Append to polygon
      polygon.add(new Point(coord, color), false);


    }
    polygon.updateBounds();


    if (debug)
      System.out.println();

    polygons.add(polygon);
  }

  protected Polygon makePolygon(float[] coloringProperty, Range propertyRange, int cellStart,
      int cellStop) {
    Polygon polygon = new Polygon();
    configurePolygon(polygon);
    
    for (int i = cellStart; i < cellStop; i++) {
      int pointId = (int) cells.GetConnectivityArray().GetTuple1(i);

      if (debug)
        System.out.print(pointId + " ");

      // Get coordinate
      float value = coloringProperty[pointId];
      Coord3d coord = coordinates[pointId];

      // Get color
      Color color = colormap.getColor(0, 0, value, propertyRange.getMin(), propertyRange.getMax());

      // Append to polygon
      polygon.add(new Point(coord, color), false);


    }
    polygon.updateBounds();


    if (debug)
      System.out.println();
    return polygon;
  }
  
  protected void configurePolygon(Polygon polygon) {
    polygon.setWireframeDisplayed(wireDisplayed);
    polygon.setWireframeColor(wireColor);
    polygon.setReflectLight(reflectLight);
    polygon.setFaceDisplayed(faceDisplayed);
    polygon.setPolygonWireframeDepthTrick(false);
    polygon.setPolygonOffsetFillEnable(false);
  }

  //////////////////////////////////////////////
  //
  // INTERMEDIATE JZY3D DATA MODEL
  //
  //////////////////////////////////////////////


  /** Return the points found in the input dataset in Jzy3D data model. */
  public Coord3d[] getCoordinates() {
    return coordinates;
  }

  /** Return the points found in the input dataset in Jzy3D data model. */
  public Coord3d[] getNormals() {
    return normals;
  }
}
