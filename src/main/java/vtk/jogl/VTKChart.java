package vtk.jogl;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import vtk.VTKReader;
import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkBoxRepresentation;
import vtk.vtkColorTransferFunction;
import vtk.vtkCompositeDataGeometryFilter;
import vtk.vtkCompositeDataIterator;
import vtk.vtkCompositePolyDataMapper;
import vtk.vtkContour3DLinearGrid;
import vtk.vtkContourFilter;
import vtk.vtkCutter;
import vtk.vtkDataObject;
import vtk.vtkExodusIIReader;
import vtk.vtkGeometryFilter;
import vtk.vtkLookupTable;
import vtk.vtkMultiBlockDataSet;
import vtk.vtkNamedColors;
import vtk.vtkPlane;
import vtk.vtkPolyDataMapper;
import vtk.vtkScalarBarActor;
import vtk.vtkScalarBarRepresentation;
import vtk.vtkScalarBarWidget;
import vtk.vtkScalarsToColors;
import vtk.vtkUnstructuredGrid;
import vtk.rendering.jogl.vtkAbstractJoglComponent;
import vtk.rendering.jogl.vtkJoglCanvasComponent;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class VTKChart {
  protected vtkNamedColors colors = new vtkNamedColors();
  
  protected vtkAbstractJoglComponent<?> canvas;
  
  protected boolean useSwing;

  /* ************************************************************ */
  /*                                                              */
  /*                          GEOMETRIES                          */
  /*                                                              */
  /* ************************************************************ */

  public void add(vtkActor actor) {
    getCanvas().getRenderer().AddActor(actor);
  }

  
  public vtkActor add(vtkExodusIIReader reader, String propertyName) {
    vtkActor actor = createActor(reader, propertyName);
    add(actor);
    return actor;
  }

  public vtkActor add(vtkUnstructuredGrid ugrid, String propertyName) {
    vtkActor actor = createActor(ugrid, propertyName);
    add(actor);
    return actor;
  }

  /**
   * FIXME : iteration implemented for single element list
   */
  public vtkActor createActor(vtkExodusIIReader reader, String propertyName) {
    vtkActor actor = new vtkActor();
    vtkCompositeDataIterator iter = reader.GetOutput().NewIterator();

    for (iter.InitTraversal(); iter.IsDoneWithTraversal() == 0; iter.GoToNextItem()) {
      vtkDataObject dObj = iter.GetCurrentDataObject();
      vtkUnstructuredGrid ugrid = (vtkUnstructuredGrid) dObj;
      ugrid.GetPointData().SetActiveScalars(propertyName);

      // Create Geometry
      vtkCompositeDataGeometryFilter geometry = new vtkCompositeDataGeometryFilter();
      geometry.SetInputConnection(0, reader.GetOutputPort(0));
      geometry.Update();

      // Mapper
      vtkPolyDataMapper mapper = new vtkPolyDataMapper();
      mapper.SetInputConnection(geometry.GetOutputPort());
      mapper.SelectColorArray(propertyName);
      mapper.InterpolateScalarsBeforeMappingOn();

      double[] r = ugrid.GetScalarRange();
      mapper.SetScalarRange(r);

      // add mapper to actor
      actor.SetMapper(mapper);
    }

    actor.GetProperty().SetEdgeVisibility(1);

    return actor;
  }
  
  public vtkActor createActor(vtkUnstructuredGrid ugrid, String propertyName) {
    vtkActor actor = new vtkActor();

    // Filter outer surface
    vtkGeometryFilter geometry = new vtkGeometryFilter();
    geometry.SetInputData(ugrid);
    geometry.Update();
    

    // Mapper
    vtkPolyDataMapper mapper = new vtkPolyDataMapper();
    mapper.SetInputConnection(geometry.GetOutputPort());
    mapper.SelectColorArray(propertyName);
    mapper.InterpolateScalarsBeforeMappingOn();

    double[] r = ugrid.GetScalarRange();
    mapper.SetScalarRange(r);
    // vtkScalarsToColors = mapper.GetLookupTable();
    System.out.println("Range=" + r[0] + "," + r[1]);

    // add mapper to actor
    actor.SetMapper(mapper);

    actor.GetProperty().SetEdgeVisibility(1);

    return actor;
  }
  
  /* ************************************************************ */
  /*                                                              */
  /*             CONTOUR (vtkContour3DLinearGrid)                 */
  /*                                                              */
  /* ************************************************************ */

  public vtkActor addContour3D(vtkExodusIIReader reader, String propertyName,
      double[] levels, double[] color) {

    vtkActor actor = createContour3DActor(reader, propertyName, levels, color);
    add(actor);
    return actor;
  }

  public vtkActor addContour3D(vtkUnstructuredGrid ugrid, String propertyName,
      double[] levels, double[] color) {

    vtkActor actor = createContour3DActor(ugrid, propertyName, levels, color);
    add(actor);
    return actor;
  }


  public vtkActor createContour3DActor(vtkExodusIIReader reader, String propertyName,
      double[] levels, double[] color) {

    setActiveScalar(reader, propertyName);
    vtkContour3DLinearGrid contour = createContour3DFilter(reader, levels);
    vtkActor contourActor = createActorForCompositePolyData(contour.GetOutputPort());
    
    contourActor.GetProperty().SetColor(color);

    return contourActor;
  }


  public vtkActor createContour3DActor(vtkUnstructuredGrid ugrid, String propertyName,
      double[] levels, double[] color) {
    
    vtkContour3DLinearGrid contour = createContour3DFilter(ugrid, levels);

    vtkCompositePolyDataMapper contourMapper = new vtkCompositePolyDataMapper();
    contourMapper.SetInputConnection(contour.GetOutputPort());

    vtkActor contourActor = new vtkActor();
    contourActor.SetMapper(contourMapper);
    contourActor.GetProperty().SetColor(color);

    return contourActor;
  }

  public vtkContour3DLinearGrid createContour3DFilter(vtkExodusIIReader reader, double[] levels) {
    vtkContour3DLinearGrid contour = initContour3DFilter(levels);
    contour.SetInputData(reader.GetOutput());
    contour.Update();
    return contour;
  }
  
  public vtkContour3DLinearGrid createContour3DFilter(vtkUnstructuredGrid ugrid, double[] levels) {
    vtkContour3DLinearGrid contour = initContour3DFilter(levels);
    contour.SetInputData(ugrid);
    contour.Update();
    return contour;
  }

  public vtkContour3DLinearGrid initContour3DFilter(double[] levels) {
    vtkContour3DLinearGrid contour = new vtkContour3DLinearGrid();
    contour.SetMergePoints(1);
    contour.SetSequentialProcessing(0);
    contour.SetInterpolateAttributes(1);
    contour.SetComputeNormals(1);

    contour.SetNumberOfContours(levels.length);

    for (int i = 0; i < levels.length; i++) {
      contour.SetValue(i, levels[i]);
    }

    return contour;
  }

  /* ************************************************************ */
  /*                                                              */
  /*                  CONTOUR (vtkContourFilter)                  */
  /*                                                              */
  /* ************************************************************ */

  
  public vtkActor addContour(vtkExodusIIReader reader, String propertyName, double[] levels, vtkLookupTable colormap, double[] scalarRange) {
    vtkActor actor = createContourActor(reader, propertyName, levels, colormap, scalarRange);
    
    add(actor);
    
    return actor;
  }
  
  public vtkActor createContourActor(vtkExodusIIReader reader, String propertyName, double[] levels, vtkLookupTable colormap, double[] scalarRange) {
    vtkActor contourActor = createContourActor(reader, propertyName, levels);
    
    contourActor.GetMapper().SetLookupTable(colormap);
    contourActor.GetMapper().SetScalarRange(scalarRange);
    
    return contourActor;
  }

  public vtkActor createContourActor(vtkExodusIIReader reader, String propertyName,
      double[] levels) {
    return createContourActor(reader, propertyName, levels, null);
  }
  
  public vtkActor createContourActor(vtkExodusIIReader reader, String propertyName,
      double[] levels, double[] color) {

    setActiveScalar(reader, propertyName);

    vtkContourFilter contour = initContourFilter(levels);
    contour.SetInputData(reader.GetOutput());
    contour.Update();

    vtkActor contourActor = createActorForCompositePolyData(contour.GetOutputPort());
    
    if(color!=null)
      contourActor.GetProperty().SetColor(color);

    //Array.print("Contour bounds : ", contourActor.GetBounds());
    return contourActor;
  }

  public vtkContourFilter initContourFilter(double[] levels) {
    vtkContourFilter contour = new vtkContourFilter();
    contour.SetComputeNormals(1);

    contour.SetNumberOfContours(levels.length);

    for (int i = 0; i < levels.length; i++) {
      contour.SetValue(i, levels[i]);
    }

    return contour;
  }
  

  

  /* ************************************************************ */
  /*                                                              */
  /*                             SLICES                           */
  /*                                                              */
  /* ************************************************************ */

  public vtkActor[] addContourSlice(vtkExodusIIReader reader, double[] orientation, double[] position, double[] levels, vtkLookupTable colormap, double[] scalarRange, double[] lineColor, int lineWidth) {
    vtkActor[] actors = createContourSliceActor(reader, orientation, position, levels, colormap, scalarRange, lineColor, lineWidth);
    
    add(actors[0]);
    add(actors[1]);

    return actors;
  
  }
  
  public vtkActor[] createContourSliceActor(vtkExodusIIReader reader, double[] orientation, double[] position, double[] levels, vtkLookupTable colormap, double[] scalarRange, double[] lineColor, int lineWidth) {
  
    vtkCutter cutter = createSliceFilter(reader, orientation, position);

    vtkActor sliceActor3 = createActorForCompositePolyData(cutter.GetOutputPort());
    sliceActor3.GetMapper().SetLookupTable(colormap);
    sliceActor3.GetMapper().SetScalarRange(scalarRange);

    // slice 3 / iso lines
    vtkContourFilter contour = initContourFilter(levels);
    contour.SetInputConnection(cutter.GetOutputPort());
    contour.Update();

    vtkActor sliceContour3 = createActorForCompositePolyData(contour.GetOutputPort());
    sliceContour3.GetMapper().SetLookupTable(createColormapSingleColor(lineColor));
    sliceContour3.GetProperty().SetColor(lineColor);
    sliceContour3.GetProperty().SetLineWidth(lineWidth);

    vtkActor[] actors = new vtkActor[2];
    actors[0] = sliceActor3;
    actors[1] = sliceContour3;
    
    return actors;
  }


  public vtkActor addSlice(vtkExodusIIReader reader, double[] orientation, double[] position, vtkLookupTable colormap, double[] scalarRange) {
    vtkActor actor = createSliceActor(reader, orientation, position, colormap, scalarRange);
    
    add(actor);
    
    return actor;
  }
  
  public vtkActor createSliceActor(vtkExodusIIReader reader, double[] orientation, double[] position, vtkLookupTable colormap, double[] scalarRange) {
    vtkActor  slice = createSliceActor(reader, orientation, position);
  
    slice.GetMapper().SetLookupTable(colormap);
    slice.GetMapper().SetScalarRange(scalarRange);

    return slice;
  }
  
  /**
   * Create a plane to cut,here it cuts in the XZ direction
   * 
   * @param orientation : XZ=(1,0,0);XY =(0,0,1),YZ=(0,1,0)
   */
  public vtkActor createSliceActor(vtkExodusIIReader reader, double[] orientation, double[] position) {

    vtkCutter cutter = createSliceFilter(reader, orientation, position);
    
    vtkActor actor = createActorForCompositePolyData(cutter.GetOutputPort());
    
    return actor;
  }

  public vtkCutter createSliceFilter(vtkExodusIIReader reader, double[] orientation,
      double[] position) {
    vtkPlane plane = new vtkPlane();
    plane.SetOrigin(position);
    plane.SetNormal(orientation);

    // create cutter
    vtkCutter cutter = new vtkCutter();
    cutter.SetCutFunction(plane);
    cutter.SetInputConnection(reader.GetOutputPort());
    cutter.Update();
    return cutter;
  }
  
  /* ************************************************************ */
  /*                                                              */
  /*                            LAYOUT                            */
  /*                                                              */
  /* ************************************************************ */


  public void addOrientationAxis() {
    addOrientationAxis(getCanvas());
  }
  
  public void addOrientationAxis(final vtkAbstractJoglComponent<?> canvas) {
    vtkAbstractJoglComponent.attachOrientationAxes(canvas);
  }

  public vtkScalarBarActor addColorbar(String propertyName, vtkActor actor) {
    return addColorbar(propertyName, actor.GetMapper().GetLookupTable(), getCanvas());
  }

  public vtkScalarBarActor addColorbar(String propertyName, vtkScalarsToColors colormap) {
    return addColorbar(propertyName, colormap, getCanvas());
  }
  
  public vtkScalarBarActor addColorbar(String propertyName, vtkScalarsToColors vtkScalarsToColors,
      vtkAbstractJoglComponent<?> joglWidget) {
    // Add Scalar bar widget
    vtkScalarBarWidget scalarBar = new vtkScalarBarWidget();
    scalarBar.SetInteractor(joglWidget.getRenderWindowInteractor());

    vtkScalarBarActor vtkScalarBarActor = scalarBar.GetScalarBarActor();
    vtkScalarBarActor.SetTitle(propertyName);
    vtkScalarBarActor.SetLookupTable(vtkScalarsToColors);
    vtkScalarBarActor.SetOrientationToHorizontal();
    vtkScalarBarActor.SetTextPositionToPrecedeScalarBar();
    vtkScalarBarActor.SetNumberOfLabels(3);

    vtkScalarBarRepresentation colormap = (vtkScalarBarRepresentation) scalarBar.GetRepresentation();
    colormap.SetPosition(0.5, 0.053796);
    colormap.SetPosition2(0.33, 0.106455);
    scalarBar.EnabledOn();
    scalarBar.RepositionableOn();
    
    return vtkScalarBarActor;
  }

  public vtkBoxRepresentation addBox(vtkMultiBlockDataSet output) {
    double[] box = new double[6];
    
    output.GetBounds(box);
    
    return addBox(box);
  }

  public vtkBoxRepresentation addBox(double[] box) {
    final vtkBoxRepresentation representation = new vtkBoxRepresentation();
    representation.SetPlaceFactor(1.25);
    representation.PlaceWidget(box);

    representation.VisibilityOn();
    representation.HandlesOn();
    
    return representation;
  }
  
  public vtkNamedColors getColors() {
    return colors;
  }
  
  public static vtkLookupTable createColormapRGB() {
    vtkColorTransferFunction transfert = new vtkColorTransferFunction();
    transfert.SetColorSpaceToRGB();
    transfert.AddRGBPoint(0.00, 0.000, 0.000, 1.000); // blue
    transfert.AddRGBPoint(0.25, 0.000, 1.000, 1.000); 
    transfert.AddRGBPoint(0.50, 0.000, 1.000, 0.000); // green
    transfert.AddRGBPoint(0.75, 1.000, 1.000, 0.000);
    transfert.AddRGBPoint(1.00, 1.000, 0.000, 0.000); // red
    
    int nColor = 100;
    
    vtkLookupTable lookupTable = new vtkLookupTable();

    lookupTable.SetNumberOfTableValues(nColor);
    
    for (int i = 0; i < nColor; i++) {
      double[] rgb = new double[3];
      transfert.GetColor(1D*i/nColor,rgb);
      double[] rgba = {rgb[0], rgb[1], rgb[2], 1};
      lookupTable.SetTableValue(i, rgba);
    }
    
    lookupTable.SetRampToLinear();
    lookupTable.Build();
    return lookupTable;
  }
  
  public static vtkLookupTable createColormapSingleColor(double[] color) {
    vtkLookupTable whiteTable = new vtkLookupTable();

    whiteTable.SetNumberOfTableValues(1);
    whiteTable.SetTableValue(0, color);
    
    return whiteTable;
  }
  
  /* ************************************************************ */
  /*                                                              */
  /*                        COMPONENTS                            */
  /*                                                              */
  /* ************************************************************ */


  public vtkAbstractJoglComponent<?> getCanvas() {
    if(canvas==null)
      canvas = newCanvas(useSwing);
    return canvas;
  }

  public vtkAbstractJoglComponent<?> newCanvas(final boolean useSwing) {
    final vtkAbstractJoglComponent<?> joglWidget =
        useSwing ? new vtkJoglPanelComponent() : new vtkJoglCanvasComponent();
    System.out.println(
        "We are using " + joglWidget.getComponent().getClass().getName() + " for the rendering.");
    return joglWidget;
  }

  public void open(String title) {
    open(title, getCanvas());
  }
  
  public void open(String title, final vtkAbstractJoglComponent<?> joglWidget) {
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(joglWidget.getComponent(), BorderLayout.CENTER);
    frame.setSize(1200, 600);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /* ************************************************************ */
  /*                                                              */
  /*                              IO                              */
  /*                                                              */
  /* ************************************************************ */

  
  public vtkExodusIIReader getExodusIIReader(String file, String propertyName) {
    vtkExodusIIReader reader = (vtkExodusIIReader) VTKReader.getReader(file);
    int[] timerange = reader.GetTimeStepRange();

    // Despite not retrieving the grids, this is useful to make the slab colored
    VTKReader.read_exodusii_grids(reader, new int[] {1, 0}, timerange[1], propertyName);

    // Fetch metadata.
    reader.UpdateInformation();

    // Read data.
    reader.Update();

    System.out.println("NumberOfTimeSteps = " + reader.GetNumberOfTimeSteps());
    System.out.println("NumberOfElementBlockArrays = " + reader.GetNumberOfElementBlockArrays());
    System.out.println("NumberOfPointResultArrays = " + reader.GetNumberOfPointResultArrays());
    System.out.println("NumberOfElementResultArrays = " + reader.GetNumberOfElementResultArrays());
    System.out.println("NumberOfGlobalResultArrays = " + reader.GetNumberOfGlobalResultArrays());
    return reader;
  }
  
  /* ************************************************************ */
  /*                                                              */
  /*                           HELPERS                            */
  /*                                                              */
  /* ************************************************************ */

  public void setActiveScalar(vtkExodusIIReader reader, String propertyName) {
    setActiveScalar((vtkUnstructuredGrid) reader.GetOutput().NewIterator().GetCurrentDataObject(), propertyName);
  }

  public void setActiveScalar(vtkUnstructuredGrid grid, String propertyName) {
    grid.GetPointData().SetActiveScalars(propertyName);
  }

  public vtkActor createActorForCompositePolyData(vtkAlgorithmOutput port) {
    vtkCompositePolyDataMapper mapper = new vtkCompositePolyDataMapper();
    mapper.SetInputConnection(port);
    
    vtkActor actor = new vtkActor();
    actor.SetMapper(mapper);
    return actor;
  }


}
