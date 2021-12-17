package vtk.jogl;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import org.jzy3d.maths.Array;
import vtk.VTKReader;
import vtk.vtkActor;
import vtk.vtkBoxRepresentation;
import vtk.vtkCompositeDataGeometryFilter;
import vtk.vtkCompositeDataIterator;
import vtk.vtkCompositePolyDataMapper;
import vtk.vtkContour3DLinearGrid;
import vtk.vtkCutter;
import vtk.vtkDataObject;
import vtk.vtkExodusIIReader;
import vtk.vtkMultiBlockDataSet;
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

  public void addOrientationAxis(final vtkAbstractJoglComponent<?> joglWidget) {
    vtkAbstractJoglComponent.attachOrientationAxes(joglWidget);
  }

  public void addColorbar(String propertyName, vtkScalarsToColors vtkScalarsToColors,
      final vtkAbstractJoglComponent<?> joglWidget) {
    // Add Scalar bar widget
    vtkScalarBarWidget scalarBar = new vtkScalarBarWidget();
    scalarBar.SetInteractor(joglWidget.getRenderWindowInteractor());

    vtkScalarBarActor vtkScalarBarActor = scalarBar.GetScalarBarActor();
    vtkScalarBarActor.SetTitle(propertyName);
    vtkScalarBarActor.SetLookupTable(vtkScalarsToColors);
    vtkScalarBarActor.SetOrientationToHorizontal();
    vtkScalarBarActor.SetTextPositionToPrecedeScalarBar();
    vtkScalarBarActor.SetNumberOfLabels(3);

    vtkScalarBarRepresentation srep = (vtkScalarBarRepresentation) scalarBar.GetRepresentation();
    srep.SetPosition(0.5, 0.053796);
    srep.SetPosition2(0.33, 0.106455);
    scalarBar.EnabledOn();
    scalarBar.RepositionableOn();
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
      // vtkScalarsToColors = mapper.GetLookupTable();
      System.out.println("Range=" + r[0] + "," + r[1]);

      // add mapper to actor
      actor.SetMapper(mapper);
    }

    actor.GetProperty().SetEdgeVisibility(1);

    return actor;
  }

  public vtkAbstractJoglComponent<?> newCanvas() {
    return newCanvas(true);
  }

  public vtkAbstractJoglComponent<?> newCanvas(final boolean usePanel) {
    final vtkAbstractJoglComponent<?> joglWidget =
        usePanel ? new vtkJoglPanelComponent() : new vtkJoglCanvasComponent();
    System.out.println(
        "We are using " + joglWidget.getComponent().getClass().getName() + " for the rendering.");
    return joglWidget;
  }

  public void open(final vtkAbstractJoglComponent<?> joglWidget) {
    JFrame frame = new JFrame("VTK with JFrame and JOGL 2.4 RC4");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(joglWidget.getComponent(), BorderLayout.CENTER);
    frame.setSize(1200, 600);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public void addBox(vtkMultiBlockDataSet output) {
    final vtkBoxRepresentation representation = new vtkBoxRepresentation();
    representation.SetPlaceFactor(1.25);
    double[] box = new double[6];
    output.GetBounds(box);
    representation.PlaceWidget(box);

    representation.VisibilityOn();
    representation.HandlesOn();
  }


  public vtkActor createContourActor(vtkExodusIIReader reader, String propertyName,
      double[] levels) {

    vtkUnstructuredGrid grid =
        (vtkUnstructuredGrid) reader.GetOutput().NewIterator().GetCurrentDataObject();
    grid.GetPointData().SetActiveScalars(propertyName);

    vtkContour3DLinearGrid contour = new vtkContour3DLinearGrid();
    contour.SetInputData(reader.GetOutput());
    contour.SetMergePoints(1);
    contour.SetSequentialProcessing(0);
    contour.SetInterpolateAttributes(1);
    contour.SetComputeNormals(1);


    contour.SetNumberOfContours(levels.length);

    for (int i = 0; i < levels.length; i++) {
      contour.SetValue(i, levels[i]);
    }

    // System.out.println("Number of contour : " + contour.GetNumberOfContours());
    contour.Update();

    vtkCompositePolyDataMapper contourMapper = new vtkCompositePolyDataMapper();
    contourMapper.SetInputConnection(contour.GetOutputPort());
    // contMapper.ScalarVisibilityOn();

    vtkActor contourActor = new vtkActor();
    contourActor.SetMapper(contourMapper);
    contourActor.GetProperty().SetColor(.8, .4, .4);

    Array.print("Contour bounds : ", contourActor.GetBounds());
    return contourActor;
  }

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

  /**
   * Create a plane to cut,here it cuts in the XZ direction
   * 
   * @param orientation : XZ=(1,0,0);XY =(0,0,1),YZ=(0,1,0)
   */
  public vtkActor createSlice(vtkExodusIIReader reader, double[] orientation, double[] position) {

    vtkPlane plane = new vtkPlane();
    plane.SetOrigin(position);
    plane.SetNormal(orientation);

    // create cutter
    vtkCutter cutter = new vtkCutter();
    cutter.SetCutFunction(plane);
    cutter.SetInputConnection(reader.GetOutputPort());
    cutter.Update();
    
    
    vtkCompositePolyDataMapper contourMapper = new vtkCompositePolyDataMapper();
    contourMapper.SetInputConnection(cutter.GetOutputPort());

    
    vtkActor actor = new vtkActor();
    actor.SetMapper(contourMapper);
    
    return actor;
  }

}
