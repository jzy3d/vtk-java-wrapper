package org.jzy3d.io.vtk.drawable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Normal;
import org.jzy3d.maths.Normal.NormalMode;
import org.jzy3d.maths.Range;
import org.jzy3d.painters.Font;
import org.jzy3d.plot3d.primitives.Geometry;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO2;
import org.jzy3d.plot3d.text.DrawableTextWrapper;
import org.jzy3d.plot3d.text.renderers.jogl.JOGLTextRenderer2d;
import com.google.common.collect.ArrayListMultimap;
import vtk.VTKGeometry;
import vtk.VTKReader;
import vtk.VTKUtils;
import vtk.vtkCellArrayIterator;
import vtk.vtkDataArray;
import vtk.vtkDataObject;
import vtk.vtkPointData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnstructuredGrid;
import vtk.processing.VTKNormalProcessor.VTKNormalPerVertex;

/**
 * Load VTK data structures into a list of Jzy3D {@link DrawableVBO2}.
 * 
 * <img src="./doc-files/vtkLinearCells.png"/>
 * 
 * @author Martin Pernollet
 *
 */
public class VTKDrawableVBOBuilder extends AbstractVTKDrawableBuilder implements IDrawableBuilder {
  protected static Logger log = Logger.getLogger(VTKDrawableVBOBuilder.class);

  protected float[] coordinates;
  protected float[] normals;
  protected float[] colors;
  protected boolean isNormalPerPoint = true;

  public enum GeometryMode {
    SINGLE_GEOMETRY, MULTI_GEOMETRY
  }
  public enum VerticeMode {
    REPEATED, SHARED
  }

  protected GeometryMode geometryMode = GeometryMode.SINGLE_GEOMETRY;
  protected VerticeMode verticeMode = VerticeMode.REPEATED;
  protected int expectedGeometry = VTKGeometry.VTK_QUAD;
  protected NormalMode normalMode = NormalMode.REPEATED;


  // case of single element index, only suitable for triangle
  protected int[] elements;
  protected int singleElementCursor = 0;

  // case of no index but multiple geometries, suitable for trangle and polygons with repeated,
  // contiguous vertices, BUT NOT WORKING
  protected boolean primitiveRestart = false; // WIP

  // case of multiple element index, suitable for triangle and polygons with REPEATED, CONTIGUOUS
  // vertices
  protected int[] elementsStarts;
  protected int[] elementsLength;
  protected int multiElementCursor = 0;

  // case of multiple element index, suitable for triangle and polygons with SHARED, non contiguous
  // vertices
  protected int[][] elementsIndices;

  protected boolean debug = false;
  protected int debugMaxCell = -1; // -1 to stop avoid putting a limit on number of cells


  protected static final int dimensions = 3;

  protected int colorChannels = 3;
  protected float alpha = 1;

  // mapping point to cell, to check if they are shared or not
  protected ArrayListMultimap<Integer, Integer> pointToCell;


  // ************************** UNSTRUCTURED GRID CONSTRUCTORS ******************************* //

  /**
   * The most simple AND powerfull {@link DrawableVBO2} builder.
   * 
   * Will compute normals per vertex with VTK algorithms, always using a shared vertice mode, and
   * considering the input cell types are all similar in the dataset.
   */
  public VTKDrawableVBOBuilder(vtkUnstructuredGrid poly) {
    this(poly, processNormals(poly), GeometryMode.MULTI_GEOMETRY, VerticeMode.SHARED, null,
        getFirstCellGeometry(poly));
  }

  /**
   * Will compute normals per vertex with Jzy3D algorithms, always using a shared vertice mode, and
   * considering the input cell types are all similar in the dataset.
   */
  public VTKDrawableVBOBuilder(vtkUnstructuredGrid poly, NormalMode normalMode) {
    this(poly, null, GeometryMode.MULTI_GEOMETRY, VerticeMode.SHARED, normalMode,
        getFirstCellGeometry(poly));
  }

  public VTKDrawableVBOBuilder(vtkUnstructuredGrid ugrid, vtkDataArray normalArray,
      GeometryMode geometryMode, VerticeMode verticeMode, int vtkGeometry) {
    this(ugrid, normalArray, geometryMode, verticeMode, null, vtkGeometry);
  }

  public VTKDrawableVBOBuilder(vtkUnstructuredGrid ugrid, GeometryMode geometryMode,
      VerticeMode verticeMode, NormalMode normalMode, int vtkGeometry) {
    this(ugrid, null, geometryMode, verticeMode, normalMode, vtkGeometry);
  }

  public VTKDrawableVBOBuilder(vtkUnstructuredGrid ugrid, GeometryMode geometryMode,
      VerticeMode verticeMode, int vtkGeometry) {
    this(ugrid, null, geometryMode, verticeMode, NormalMode.REPEATED, vtkGeometry);
  }


  /**
   * A {@link DrawableVBO2} builder capable of handling various data structure inputs.
   *
   * This constructor is protected to avoid showing parameters that are not relevant together (e.g.
   * normalMode defines an automatic normal processing method, whereas normalArray allows defining
   * normals from outside).
   *
   * @param ugrid input geometry
   * @param normalArray an optional array of normal computed externally. Can be null, in that case
   *        normals are processed by a simple algorithm
   *        ({@link Normal#compute(Coord3d, Coord3d, Coord3d)}.
   * @param geometryMode {@link GeometryMode.MULTI_GEOMETRY} indicates that the input geometry is
   *        made of multiple cells that should be gathered in the same VBO but handled in a way that
   *        allows separating each cell. {@link GeometryMode.SINGLE_GEOMETRY} is a rare case.
   * @param verticeMode {@link VerticeMode.SHARED} indicates that the input geometry is made of
   *        cells that have shared points with each other and that a geometry is defined by an index
   *        indicating which points allow forming the geometry. This is better for fluid rendering.
   *        However one can provide geometries with {@link VerticeMode.REPEATED} points, which
   *        offers decent but lower performance, but offers the advantage of dealing with geometries
   *        that have an inconsistent sharing scheme (i.e. when some points appear twice).
   * @param normalMode {NormalMode.SHARED} indicates that IF the input geometry is mode of
   *        {@link VerticeMode.SHARED} points, then the normals should be processed by averaging the
   *        normals of all polygons to which a point belongs. If {@link VerticeMode.REPEATED} one
   *        should use {NormalMode.PER_VERTEX}. If {@link VerticeMode.SHARED}, using
   *        {NormalMode.PER_VERTEX} will produce weird effect as this lead to an arbitrary normal
   *        being picked.
   * @param vtkGeometry indicates the {@link VTKGeometry} that the input geometry contains.
   */
  protected VTKDrawableVBOBuilder(vtkUnstructuredGrid ugrid, vtkDataArray normalArray,
      GeometryMode geometryMode, VerticeMode verticeMode, NormalMode normalMode, int vtkGeometry) {
    this(ugrid, normalArray);

    this.geometryMode = geometryMode;
    this.verticeMode = verticeMode;
    this.normalMode = normalMode;
    this.expectedGeometry = vtkGeometry;
  }

  protected VTKDrawableVBOBuilder(vtkUnstructuredGrid unstructuredGrid, vtkDataArray normalArray) {
    super(unstructuredGrid);
    loadPointsAndNormals(unstructuredGrid.GetPoints(), normalArray);
  }


  // ************************** POLYGON DATA CONSTRUCTORS ******************************* //

  /**
   * The most simple AND powerfull {@link DrawableVBO2} builder.
   * 
   * Will compute normals per vertex with VTK algorithms, always using a shared vertice mode, and
   * considering the input cell types are all similar in the dataset.
   */
  public VTKDrawableVBOBuilder(vtkPolyData poly) {
    this(poly, processNormals(poly), GeometryMode.MULTI_GEOMETRY, VerticeMode.SHARED, null,
        getFirstCellGeometry(poly));
  }

  /**
   * Will compute normals per vertex with Jzy3D algorithms, always using a shared vertice mode, and
   * considering the input cell types are all similar in the dataset.
   */
  public VTKDrawableVBOBuilder(vtkPolyData poly, NormalMode normalMode) {
    this(poly, null, GeometryMode.MULTI_GEOMETRY, VerticeMode.SHARED, normalMode,
        getFirstCellGeometry(poly));
  }

  public VTKDrawableVBOBuilder(vtkPolyData poly, vtkDataArray normalArray,
      GeometryMode geometryMode, VerticeMode verticeMode, int vtkGeometry) {
    this(poly, normalArray, geometryMode, verticeMode, null, vtkGeometry);
  }

  public VTKDrawableVBOBuilder(vtkPolyData poly, GeometryMode geometryMode, VerticeMode verticeMode,
      int vtkGeometry) {
    this(poly, null, geometryMode, verticeMode, NormalMode.REPEATED, vtkGeometry);
  }


  /**
   * A {@link DrawableVBO2} builder capable of handling various data structure inputs.
   * 
   * This constructor is protected to avoid showing parameters that are not relevant together (e.g.
   * normalMode defines an automatic normal processing method, whereas normalArray allows defining
   * normals from outside).
   * 
   * @param poly input geometry
   * @param geometryMode {@link GeometryMode.MULTI_GEOMETRY} indicates that the input geometry is
   *        made of multiple cells that should be gathered in the same VBO but handled in a way that
   *        allows separating each cell. {@link GeometryMode.SINGLE_GEOMETRY} is a rare case.
   * @param verticeMode {@link VerticeMode.SHARED} indicates that the input geometry is made of
   *        cells that have shared points with each other and that a geometry is defined by an index
   *        indicating which points allow forming the geometry. This is better for fluid rendering.
   *        However one can provide geometries with {@link VerticeMode.REPEATED} points, which
   *        offers decent but lower performance, but offers the advantage of dealing with geometries
   *        that have an inconsistent sharing scheme (i.e. when some points appear twice).
   * @param normalMode {NormalMode.SHARED} indicates that IF the input geometry is mode of
   *        {@link VerticeMode.SHARED} points, then the normals should be processed by averaging the
   *        normals of all polygons to which a point belongs. If {@link VerticeMode.REPEATED} one
   *        should use {NormalMode.PER_VERTEX}. If {@link VerticeMode.SHARED}, using
   *        {NormalMode.PER_VERTEX} will produce weird effect as this lead to an arbitrary normal
   *        being picked.
   * @param vtkGeometry indicates the {@link VTKGeometry} that the input geometry contains.
   */
  public VTKDrawableVBOBuilder(vtkPolyData poly, vtkDataArray normalArray,
      GeometryMode geometryMode, VerticeMode verticeMode, NormalMode normalMode, int vtkGeometry) {
    this(poly, normalArray);

    this.geometryMode = geometryMode;
    this.verticeMode = verticeMode;
    this.normalMode = normalMode;
    this.expectedGeometry = vtkGeometry;
  }

  protected VTKDrawableVBOBuilder(vtkPolyData polygons, vtkDataArray normalArray) {
    super(polygons);

    if (polygons.GetPoints() != null)
      loadPointsAndNormals(polygons.GetPoints(), normalArray);
  }

  // ********************************************************* //

  protected void loadPointsAndNormals(vtkPoints points, vtkDataArray normalArray) {
    this.coordinates = VTKReader.toCoordFloatArray(points);

    // log.info("Number of coordinates : " + 1f*coordinates.length/3);

    if (normalArray != null) {
      this.normals = VTKReader.toCoordFloatArray(normalArray);
      this.isNormalPerPoint = (normals.length == coordinates.length);

      log.info("#Coords : " + coordinates.length + " #Normals : " + normals.length + " #Cells : "
          + cells.GetNumberOfCells() + " #Polygons (guess) : "
          + cells.GetNumberOfCells() * HEXAHEDRON_FACES);
    } else {
      this.normals = null;
    }
  }

  protected static vtkDataArray processNormals(vtkDataObject unstructuredGrid) {
    return new VTKNormalPerVertex(unstructuredGrid).update().getOutput();
  }

  protected static int getFirstCellGeometry(vtkPolyData poly) {
    if (poly.GetNumberOfCells() > 0) {
      return poly.GetCellType(0);
    } else {
      return VTKGeometry.VTK_EMPTY_CELL;
    }
  }

  protected static int getFirstCellGeometry(vtkUnstructuredGrid ugrid) {
    if (ugrid.GetNumberOfCells() > 0) {
      return ugrid.GetCellType(0);
    } else {
      return VTKGeometry.VTK_EMPTY_CELL;
    }
  }

  // ********************************************************* //

  /*
   * not working yet public DrawableVBO2 makeNormalsAsVBO() { IGLLoader<DrawableVBO2> loader =
   * DrawableVBO2.makeLoader(Array.cloneDouble(normals), 2); DrawableVBO2 normalsVBO = new
   * DrawableVBO2(loader); normalsVBO.setGLGeometryType(GL.GL_LINE);
   * normalsVBO.setWireframeColor(Color.MAGENTA); normalsVBO.setWireframeWidth(1); return
   * normalsVBO; }
   */

  /** Build LineStrips out of normals for debugging */
  public List<LineStrip> makeNormalsAsLines(float ratio) {
    List<Coord3d> normals = Coord3d.getCoords(this.normals);
    List<Coord3d> coords = getCoord3ds();

    List<LineStrip> lines = new ArrayList<>();

    for (int i = 0; i < coords.size(); i++) {
      Coord3d base = coords.get(i);
      Coord3d normal = normals.get(i);
      normal.divSelf(normal.distance(base) * ratio);
      Coord3d extremity = base.add(normal);
      LineStrip ls = new LineStrip(new Point(base, Geometry.NORMAL_START_COLOR),
          new Point(extremity, Geometry.NORMAL_END_COLOR));
      lines.add(ls);
    }
    return lines;
  }

  /** Build a scatter of points that appear more than once in the dataset */
  public Scatter makeDoublonPointScatter() {
    return new Scatter(getCoord3dsDoublon(), Color.RED, 2);
  }


  /**
   * 
   * Traversing the cell array explained here :
   * https://vtk.org/doc/nightly/html/classvtkCellArray.html#a2841af7d1aae4c8db8544b2317dc712b
   * 
   * @param property name of the array to read for making colors
   * @return
   */
  public DrawableVBO2 makePolygons(String property) {

    // ----------------------------------------------------
    // Get values for this property

    vtkPointData pointData = dataset.GetPointData();

    vtkDataArray propertyArray = pointData.GetArray(property);
    if (propertyArray == null) {
      throw new IllegalArgumentException("Property '" + property + "' not found. Use one of : "
          + String.join(" ", VTKUtils.getArrayNames(pointData)));
    }
    
    int numberOfTuples = (int) pointData.GetNumberOfTuples();
    float[] coloringProperty = VTKUtils.toFloatArray(propertyArray, numberOfTuples);

    propertyRange = new Range(propertyArray.GetFiniteRange()[0], propertyArray.GetFiniteRange()[1]);

    colors = new float[numberOfTuples * colorChannels];

    log.info("Number of colors : " + 1f * colors.length / colorChannels);


    // ----------------------------------------------------
    // Build geometry arrays by iterating on cells

    int pointsPerGeometry = -1;

    if (VTKGeometry.VTK_QUAD == expectedGeometry
        || VTKGeometry.VTK_HEXAHEDRON == expectedGeometry) {
      pointsPerGeometry = 4; // hexahedron will be built as a collection of quads
    } else if (VTKGeometry.VTK_TRIANGLE == expectedGeometry) {
      pointsPerGeometry = 3;
    } else if (VTKGeometry.VTK_EMPTY_CELL == expectedGeometry) {
      log.warn("Empty dataset, returning null");
      return null;
    } else {
      throw new IllegalArgumentException("Unsupported geometry type : " + expectedGeometry + " "
          + VTKGeometry.name(expectedGeometry));
    }

    // primitive restart is an opengl mode where geometries are separated by a flag values
    // it is not used but kept for later. reasons are discussed here :
    // https://community.khronos.org/t/using-glprimitiverestartindex-to-declare-multiple-geometries-in-the-same-vbo/107810/9
    if (primitiveRestart)
      pointsPerGeometry++;

    pointToCell = ArrayListMultimap.create();

    int cellNumber = (int) cells.GetNumberOfCells();

    if (debugMaxCell > 0) {
      cellNumber = debugMaxCell;

      debugShrinkCoordinates(cellNumber);
    }

    // initialize arrays according to the expected geometry
    initializeElementArrays(pointsPerGeometry, cellNumber);

    // Start iteration
    vtkCellArrayIterator it = cells.NewIterator();

    int k = 0;

    for (it.GoToFirstCell(); !it.IsDoneWithTraversal(); it.GoToNextCell()) {
      int cellId = (int) it.GetCurrentCellId();
      int cellType = dataset.GetCellType(cellId);
      int cellStartPointId = (int) cells.GetOffsetsArray().GetTuple1(cellId);
      int cellStopPointId = (int) cells.GetOffsetsArray().GetTuple1(cellId + 1);

      if (debug)
        debugCurrentCell(cellId, cellType, cellStartPointId, cellStopPointId);

      // Notify of the first geometry type in dataset
      if (k == 0) {
        log.info("First geometry is " + VTKGeometry.name(cellType));
      }

      // Verify ALL geometry type at parsing
      if (cellType != expectedGeometry) {
        log.warn("Expect geometry " + VTKGeometry.name(expectedGeometry) + " but found "
            + VTKGeometry.name(cellType));
      }

      if (VTKGeometry.VTK_QUAD == cellType || VTKGeometry.VTK_TRIANGLE == cellType) {
        addPolygon(cellId, cellStartPointId, cellStopPointId, coloringProperty);
      } else if (VTKGeometry.VTK_HEXAHEDRON == cellType) {
        addHexahedronPolygons(cellId, cellStartPointId, cellStopPointId, coloringProperty);
      } else {
        log.error("Unsupported cell type " + cellType + " (" + VTKGeometry.name(cellType) + ")");
      }

      k++;

      if (k == cellNumber) {
        log.warn("REACHED " + k + " CELLS");
        break;
      }

    }

    // ----------------------------------------------------

    int nShared = checkNumberOfSharedPoints();

    if (nShared > 0 && VerticeMode.REPEATED.equals(verticeMode)) {
      log.error("===================");
      log.error("Found " + nShared + " shared points. Current vertice mode : " + verticeMode);
      log.error("===================");
    }


    // ----------------------------------------------------
    // Build final VBO

    DrawableVBO2 drawable = null;

    if (VerticeMode.REPEATED.equals(verticeMode)) {

      if (GeometryMode.SINGLE_GEOMETRY.equals(geometryMode)) {
        drawable = new DrawableVBO2(coordinates, elements, pointsPerGeometry, colors);
      } else if (GeometryMode.MULTI_GEOMETRY.equals(geometryMode)) {
        drawable = new DrawableVBO2(coordinates, elementsStarts, elementsLength, colors);
      }
    }

    else if (VerticeMode.SHARED.equals(verticeMode)) {
      // either normals are defined automatically according to a mode SHARED or REPEATED ...
      if (normalMode != null) {
        drawable = new DrawableVBO2(coordinates, elementsIndices, colors, normalMode);
      }
      // ... either they were defined externally
      else if (normals != null) {
        drawable = new DrawableVBO2(coordinates, elementsIndices, colors, normals);
      }
    }
    drawable.setColorChannels(colorChannels);

    drawable.setWireframeDisplayed(wireDisplayed);
    drawable.setWireframeColor(wireColor);
    drawable.setReflectLight(reflectLight);
    // polygon.setPolygonWireframeDepthTrick(false);
    // polygon.setPolygonOffsetFillEnable(false);

    return drawable;
  }

  /**
   * Initialize array sizes according to the selected type of geometry, repetition pattern, and
   * input data size.
   * 
   * @param pointsPerGeometry
   * @param cellNumber
   */
  protected void initializeElementArrays(int pointsPerGeometry, int cellNumber) {

    if (VTKGeometry.VTK_HEXAHEDRON == expectedGeometry) {
      // an hexahedron will be drawn has a collection of 6 quads

      if (GeometryMode.SINGLE_GEOMETRY.equals(geometryMode)) {
        elements = new int[HEXAHEDRON_FACES * pointsPerGeometry * cellNumber];
      }

      else {
        if (VerticeMode.REPEATED.equals(verticeMode)) {
          elementsStarts = new int[HEXAHEDRON_FACES * cellNumber];
          elementsLength = new int[HEXAHEDRON_FACES * cellNumber];
        } else if (VerticeMode.SHARED.equals(verticeMode)) {
          elementsIndices = new int[HEXAHEDRON_FACES * cellNumber][pointsPerGeometry];
        }
      }
    }

    else if (VTKGeometry.VTK_QUAD == expectedGeometry
        || VTKGeometry.VTK_TRIANGLE == expectedGeometry) {

      if (GeometryMode.SINGLE_GEOMETRY.equals(geometryMode)) {
        elements = new int[pointsPerGeometry * cellNumber];
      }

      else {
        if (VerticeMode.REPEATED.equals(verticeMode)) {
          elementsStarts = new int[pointsPerGeometry * cellNumber];
          elementsLength = new int[pointsPerGeometry * cellNumber];
        } else if (VerticeMode.SHARED.equals(verticeMode)) {
          elementsIndices = new int[cellNumber][pointsPerGeometry];
        }
      }

    }

    else {
      throw new IllegalArgumentException("Not expected " + VTKGeometry.name(expectedGeometry));
    }
  }


  public int checkNumberOfSharedPoints() {
    int nShared = 0;
    for (Integer pointId : pointToCell.keySet()) {
      int sharedHexahedrons = pointToCell.get(pointId).size();

      if (sharedHexahedrons > 1) {
        nShared++;
      }
      // System.out.println(sharedHexahedrons +" shared");

    }
    return nShared;
  }

  //////////////////////////////////////////////
  //
  // BUILDING TRIANGLES, QUADS, POLYGONS
  //
  //////////////////////////////////////////////

  /**
   * Load an polygon by feeding the <code>colors</code> and <code>geometries</code> internal arrays
   * that are later used to build the VBO.
   * 
   * @param cellId id of the cell
   * @param cellStartPointId starting cell
   * @param cellStopPointId stoping cell
   * @param coloringProperty array of properties in the VTK file that should be used for coloring
   *        the faces of the hexahedron polygons
   */
  protected void addPolygon(int cellId, int cellStartPointId, int cellStopPointId,
      float[] coloringProperty) {

    int datasetStartPointId = -1;

    for (int i = cellStartPointId; i < cellStopPointId; i++) {
      int datasetPointId = (int) cells.GetConnectivityArray().GetTuple1(i);


      // Get coordinate
      float value = coloringProperty[datasetPointId];

      // Get color
      Color color = getValueColor(value);

      appendColor(datasetPointId, color);

      // Append to a single geometry
      if (GeometryMode.SINGLE_GEOMETRY.equals(geometryMode)) {
        append(datasetPointId);

        if (debug) {
          System.out.print(datasetPointId + " (" + coordinates[datasetPointId * 3 + 0] + ","
              + coordinates[datasetPointId * 3 + 1] + "," + coordinates[datasetPointId * 3 + 2]
              + ")");
        }
      }

      // Prepare append to a multi-geometry
      else if (GeometryMode.MULTI_GEOMETRY.equals(geometryMode)) {
        if (VerticeMode.REPEATED.equals(verticeMode)) {
          if (i == cellStartPointId) { // will store it later out of the loop
            datasetStartPointId = datasetPointId;
          }
        } else if (VerticeMode.SHARED.equals(verticeMode)) {
          elementsIndices[multiElementCursor][i - cellStartPointId] = datasetPointId;

        }
      }

      // Keep track of shared points
      pointToCell.put(datasetPointId, cellId);
    }

    // Append to multi-geometry
    if (GeometryMode.MULTI_GEOMETRY.equals(geometryMode)) {
      if (VerticeMode.REPEATED.equals(verticeMode)) {
        elementsStarts[multiElementCursor] = datasetStartPointId;
        elementsLength[multiElementCursor] = cellStopPointId - cellStartPointId;

        if (debug) {
          System.out.print("start at : " + elementsStarts[multiElementCursor] + " with "
              + elementsLength[multiElementCursor] + " points");
        }

        multiElementCursor++;
      } else if (VerticeMode.SHARED.equals(verticeMode)) {

        if (debug) {
          System.out.print("has " + elementsIndices[multiElementCursor].length + " points : ");
          for (int j = 0; j < elementsIndices[multiElementCursor].length; j++) {
            System.out.print(elementsIndices[multiElementCursor][j] + " ");
          }
        }

        multiElementCursor++;
      }
    }

    if (debug)
      System.out.println();
  }

  protected void append(int p1) {
    elements[singleElementCursor++] = p1;
  }


  //////////////////////////////////////////////
  //
  // BUILDING HEXAHDRONS
  //
  //////////////////////////////////////////////


  /**
   * Load an hexahedron by feeding the <code>colors</code> and <code>geometries</code> internal
   * arrays that are later used to build the VBO.
   * 
   * @param cellId id of the cell
   * @param cellStartPointId starting cell
   * @param cellStopPointId stoping cell
   * @param coloringProperty array of properties in the VTK file that should be used for coloring
   *        the faces of the hexahedron polygons
   */
  protected void addHexahedronPolygons(int cellId, int cellStartPointId, int cellStopPointId,
      float[] coloringProperty) {

    // --------------------------------------
    // Check consistency of input
    if (cellStopPointId - cellStartPointId != HEXAHEDRON_POINTS) {
      throw new IllegalArgumentException(
          "Hexahedron supposed to have 8 points, not " + (cellStopPointId - cellStartPointId));
    }

    // --------------------------------------
    // Prepare array to load points and normals
    int[] hexaHedronPoints = new int[HEXAHEDRON_POINTS];

    // --------------------------------------
    // Load coordinates, colors and normals

    for (int i = cellStartPointId; i < cellStopPointId; i++) {
      // Index in input cell and output hexahedron points
      int datasetPointId = (int) cells.GetConnectivityArray().GetTuple1(i);
      int hexahedPointId = i - cellStartPointId;

      if (debug)
        debugDatasetPointIdAndCoords(datasetPointId);

      // Get color
      Color color = getValueColor(coloringProperty[datasetPointId]);

      // Append to colors
      appendColor(datasetPointId, color);

      hexaHedronPoints[hexahedPointId] = datasetPointId;
    }

    /*
     * colors[0] = 1; colors[1] = 0; colors[2] = 0; // R colors[3] = 0; colors[4] = 1; colors[5] =
     * 0; // G colors[6] = 0; colors[7] = 0; colors[8] = 1; // B colors[9] = 1; colors[10] = 1;
     * colors[11] = 1; // W
     * 
     * colors[12] = 1; colors[13] = 0; colors[14] = 1; // MAGENTA colors[15] = 0; colors[16] = 1;
     * colors[17] = 1; // CYAN colors[18] = 1; colors[19] = 1; colors[20] = 0; // YELLOW colors[21]
     * = 1; colors[22] = 1; colors[23] = 1;
     */

    if (debug)
      System.out.println();


    // --------------------------------------
    // Append hexahedron faces to polygon

    appendIndices(hexaHedronPoints, 2, 3, 7, 6); // north
    appendIndices(hexaHedronPoints, 0, 1, 5, 4); // south
    appendIndices(hexaHedronPoints, 4, 5, 6, 7); // left
    appendIndices(hexaHedronPoints, 0, 1, 2, 3); // right
    appendIndices(hexaHedronPoints, 0, 3, 7, 4); // near
    appendIndices(hexaHedronPoints, 1, 2, 6, 5); // far

  }

  protected void appendColor(int datasetPointId, Color color) {
    colors[datasetPointId * colorChannels + 0] = color.r;
    colors[datasetPointId * colorChannels + 1] = color.g;
    colors[datasetPointId * colorChannels + 2] = color.b;

    if (colorChannels == 4) {
      colors[datasetPointId * colorChannels + 3] = alpha;
    }
  }


  protected void appendIndices(int[] hexahedronPoints, int p1, int p2, int p3, int p4) {

    // Case of single indexed array
    if (elements != null) {
      elements[singleElementCursor++] = hexahedronPoints[p1];
      elements[singleElementCursor++] = hexahedronPoints[p2];
      elements[singleElementCursor++] = hexahedronPoints[p3];
      elements[singleElementCursor++] = hexahedronPoints[p4];

      if (primitiveRestart)
        elements[singleElementCursor++] = DrawableVBO2.PRIMITIVE_RESTART_VALUE;
    }

    // Case of multi indexed array
    else if (elementsIndices != null) {
      elementsIndices[multiElementCursor][0] = hexahedronPoints[p1];
      elementsIndices[multiElementCursor][1] = hexahedronPoints[p2];
      elementsIndices[multiElementCursor][2] = hexahedronPoints[p3];
      elementsIndices[multiElementCursor][3] = hexahedronPoints[p4];
      multiElementCursor++;
    }

    // Cas of multi array without index, assuming all points are contiguous
    else if (elementsStarts != null && elementsLength != null) {
      elementsLength[multiElementCursor] = 4;
      elementsStarts[multiElementCursor] = hexahedronPoints[p1];
      multiElementCursor++;
    }
  }



  /**
   * Read the property based on its name and return a color array based on the colormap.
   * 
   * This will update the internal property range and colors array.
   * 
   * @param property
   * @return
   */
  public float[] getPropertyColor(String property) {

    // ----------------------------------------------------
    // Get values for this property

    vtkPointData pointData = dataset.GetPointData();

    // Loosing precision here since convert double to float
    vtkDataArray propertyArray = pointData.GetArray(property);
    if (propertyArray == null) {
      String[] a = VTKUtils.getArrayNames(pointData);
      throw new IllegalArgumentException(
          "Property '" + property + "' not found. Use one of : " + String.join(" ", a));
    }
    int numberOfTuples = (int) pointData.GetNumberOfTuples();
    float[] coloringProperty = VTKUtils.toFloatArray(propertyArray, numberOfTuples);

    propertyRange = new Range(propertyArray.GetFiniteRange()[0], propertyArray.GetFiniteRange()[1]);

    colors = new float[numberOfTuples * colorChannels];


    // ----------------------------------------------------
    // Build geometry arrays by iterating on cells

    int cellNumber = (int) cells.GetNumberOfCells();

    if (debugMaxCell > 0) {
      cellNumber = debugMaxCell;

      debugShrinkCoordinates(cellNumber);
    }

    // Start iteration
    vtkCellArrayIterator it = cells.NewIterator();

    int k = 0;

    for (it.GoToFirstCell(); !it.IsDoneWithTraversal(); it.GoToNextCell()) {
      int cellId = (int) it.GetCurrentCellId();
      int cellType = dataset.GetCellType(cellId);
      int cellStartPointId = (int) cells.GetOffsetsArray().GetTuple1(cellId);
      int cellStopPointId = (int) cells.GetOffsetsArray().GetTuple1(cellId + 1);

      if (debug)
        debugCurrentCell(cellId, cellType, cellStartPointId, cellStopPointId);

      // Notify of the first geometry type in dataset
      if (k == 0) {
        log.info("First geometry is " + VTKGeometry.name(cellType));
      }

      // Verify ALL geometry type at parsing
      if (cellType != expectedGeometry) {
        log.warn("Expect geometry " + VTKGeometry.name(expectedGeometry) + " but found "
            + VTKGeometry.name(cellType));
      }

      if (VTKGeometry.VTK_QUAD == cellType || VTKGeometry.VTK_TRIANGLE == cellType) {
        addPolygonColors(cellId, cellStartPointId, cellStopPointId, coloringProperty);
      } else if (VTKGeometry.VTK_HEXAHEDRON == cellType) {
        addHexahedronColors(cellId, cellStartPointId, cellStopPointId, coloringProperty);
      } else {
        log.error("Unsupported cell type " + cellType + " (" + VTKGeometry.name(cellType) + ")");
      }

      k++;

      if (k == cellNumber) {
        log.warn("REACHED " + k + " CELLS");
        break;
      }
    }

    return colors;
  }


  /**
   * @param cellId id of the cell
   * @param cellStartPointId starting cell
   * @param cellStopPointId stoping cell
   * @param coloringProperty array of properties in the VTK file that should be used for coloring
   *        the faces of the hexahedron polygons
   */
  protected void addPolygonColors(int cellId, int cellStartPointId, int cellStopPointId,
      float[] coloringProperty) {

    for (int i = cellStartPointId; i < cellStopPointId; i++) {
      int datasetPointId = (int) cells.GetConnectivityArray().GetTuple1(i);

      // Get color
      Color color = getValueColor(coloringProperty[datasetPointId]);

      // Append to colors
      appendColor(datasetPointId, color);
    }

    if (debug)
      System.out.println();
  }


  /**
   * @param cellId id of the cell
   * @param cellStartPointId starting cell
   * @param cellStopPointId stoping cell
   * @param coloringProperty array of properties in the VTK file that should be used for coloring
   *        the faces of the hexahedron polygons
   */
  protected void addHexahedronColors(int cellId, int cellStartPointId, int cellStopPointId,
      float[] coloringProperty) {

    // --------------------------------------
    // Check consistency of input

    if (cellStopPointId - cellStartPointId != HEXAHEDRON_POINTS) {
      throw new IllegalArgumentException(
          "Hexahedron supposed to have 8 points, not " + (cellStopPointId - cellStartPointId));
    }

    // --------------------------------------
    // Load color

    for (int i = cellStartPointId; i < cellStopPointId; i++) {
      // Index in input cell and output hexahedron points
      int datasetPointId = (int) cells.GetConnectivityArray().GetTuple1(i);

      if (debug)
        debugDatasetPointIdAndCoords(datasetPointId);

      // Get color
      Color color = getValueColor(coloringProperty[datasetPointId]);

      appendColor(datasetPointId, color);
    }

    if (debug)
      System.out.println();
  }


  //////////////////////////////////////////////
  //
  // INTERMEDIATE JZY3D DATA MODEL
  //
  //////////////////////////////////////////////


  /** Return the points found in the input dataset in float[]. */
  public float[] getCoordinates() {
    return coordinates;
  }

  /** Return the points found in the input dataset in Jzy3D data model. */
  public List<Coord3d> getCoord3ds() {
    List<Coord3d> coords = new ArrayList<>();
    for (int i = 0; i < coordinates.length - 3; i += 3) {
      coords.add(new Coord3d(coordinates[i], coordinates[i + 1], coordinates[i + 2]));
    }
    return coords;
  }

  /** Computes points that are duplicated in the coordinates dataset */
  public List<Coord3d> getCoord3dsDoublon() {
    List<Coord3d> coords = getCoord3ds();
    Set<Coord3d> uniquePoints = new HashSet<>(coords);
    return new ArrayList<>(CollectionUtils.subtract(coords, uniquePoints));
  }


  //////////////////////////////////////////////
  //
  // DEBUG
  //
  //////////////////////////////////////////////


  public boolean isDebugMaxCell() {
    return debugMaxCell > 0;
  }

  public int getDebugMaxCell() {
    return debugMaxCell;
  }

  public void setDebugMaxCell(int debugMaxCell) {
    this.debugMaxCell = debugMaxCell;
  }

  public List<DrawableTextWrapper> debugVertexId_WithDrawables() {
    if (VTKGeometry.VTK_QUAD == expectedGeometry)
      return debugVertexId_WithDrawables(0, debugMaxCell * QUAD_POINTS);
    else if (VTKGeometry.VTK_HEXAHEDRON == expectedGeometry)
      return debugVertexId_WithDrawables(0, debugMaxCell * HEXAHEDRON_POINTS);
    else
      return null;
  }

  public List<DrawableTextWrapper> debugVertexId_WithDrawables(int form, int to) {
    List<DrawableTextWrapper> li = new ArrayList<>();
    for (int i = form; i < to; i++) {
      Coord3d c = new Coord3d(coordinates[i * 3], coordinates[i * 3 + 1], coordinates[i * 3 + 2]);
      DrawableTextWrapper b =
          new DrawableTextWrapper(i + "", c, Color.ORANGE, new JOGLTextRenderer2d());

      b.setDefaultFont(new Font("Arial", 50));
      li.add(b);
    }
    return li;
  }

  /** Reduce coordinates to the number of debug cell. */
  protected void debugShrinkCoordinates(int cellNumber) {
    int size = cellNumber * 3;

    if (VTKGeometry.VTK_QUAD == expectedGeometry)
      size *= QUAD_POINTS;
    else if (VTKGeometry.VTK_HEXAHEDRON == expectedGeometry)
      size *= HEXAHEDRON_POINTS;

    float[] shrinkedCoordinates = new float[size];
    System.arraycopy(coordinates, 0, shrinkedCoordinates, 0, size);
    coordinates = shrinkedCoordinates;
  }

  protected void debugDatasetPointIdAndCoords(int datasetPointId) {
    System.out.print(datasetPointId + " ");

    // System.out.print(datasetPointId + " (" + coordinates[datasetPointId] + ","
    // + coordinates[datasetPointId] + "," + coordinates[datasetPointId] + ") ");
  }


  //////////////////////////////////////////////
  //
  // SETTINGS
  //
  //////////////////////////////////////////////

  public float getAlpha() {
    return alpha;
  }

  public void setAlpha(float alpha) {
    this.alpha = alpha;

    if (alpha != 1) {
      colorChannels = 4;
    } else {
      colorChannels = 3;
    }
  }

  public int getColorChannels() {
    return colorChannels;
  }


}
