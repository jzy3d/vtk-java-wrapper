package vtk.examples.exodus;

import org.jzy3d.maths.Array;
import vtk.*;


/**
 *
 * @author Minh Do
 *
 */
public class ExodusMultipleBlocks {
  public static void main(String[] args) {
    VTKUtils.loadVtkNativeLibraries();

    //String file = "G:/Shared drives/Development/FEM Enthalpy method/Outputs/ExampleOutputs/Exodus/3D_Cp_HeatSource/Cp_HS_bornDead_woAdp_subDomain_New_evapBC.e-s301";
    String file = "/Users/martin/Datasets/thermocalc/Exodus/3D_Cp_HeatSource/Cp_HS_bornDead_woAdp_subDomain_New_evapBC.e-s301";

    String propertyName = "temperature";

    readAndShow(file, propertyName);
  }

  public static void readAndShow(String file, String propertyName) {
    vtkExodusIIReader reader = (vtkExodusIIReader) VTKReader.getReader(file);
    int[] timerange = reader.GetTimeStepRange();

    Array.print("Exodus time range: ", timerange);

    vtkUnstructuredGrid[] ugrids = VTKReader.read_exodusii_grids(reader, new int[]{1, 1}, timerange[1], propertyName);

    // Fetch metadata.
    reader.UpdateInformation();

    // Read data.
    reader.Update();

    System.out.println( "NumberOfTimeSteps = " + reader.GetNumberOfTimeSteps() );
    System.out.println( "NumberOfElementBlockArrays = " + reader.GetNumberOfElementBlockArrays());
    System.out.println( "NumberOfPointResultArrays = " + reader.GetNumberOfPointResultArrays());
    System.out.println( "NumberOfElementResultArrays = " + reader.GetNumberOfElementResultArrays());
    System.out.println( "NumberOfGlobalResultArrays = " + reader.GetNumberOfGlobalResultArrays());

    // --------------
    vtkActor actor = new vtkActor();
    vtkMultiBlockDataSet output = reader.GetOutput();
    vtkCompositeDataIterator iter = output.NewIterator();
    for (iter.InitTraversal(); iter.IsDoneWithTraversal()==0; iter.GoToNextItem()) {
      vtkDataObject dObj = iter.GetCurrentDataObject();
      vtkUnstructuredGrid ugrid = (vtkUnstructuredGrid) dObj;
//    for(vtkUnstructuredGrid ugrid : ugrids) {
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
      System.out.println("Range="+r[0]+","+r[1]);
      actor.SetMapper(mapper);
    }

    actor.GetProperty().SetEdgeVisibility(1);


    // Renderer
    vtkRenderer ren = new vtkRenderer();
    vtkRenderWindow renWin = new vtkRenderWindow();
    renWin.AddRenderer(ren);
    renWin.SetSize(1200,600);
    renWin.SetWindowName("ReadExodusData");

    ren.AddViewProp(actor);

    // Create the graphics structure. The renderer renders into the render
    // window. The render window interactor captures mouse events and will
    // perform appropriate camera or actor manipulation depending on the
    // nature of the events.
    vtkRenderWindowInteractor iren = new vtkRenderWindowInteractor();
    iren.SetRenderWindow(renWin);

    // This allows the interactor to initalize itself. It has to be
    // called before an event loop.
    iren.Initialize();

    // We'll zoom in a little by accessing the camera and invoking a "Zoom"
    // method on it.
    ren.ResetCamera();
    vtkCamera cam = ren.GetActiveCamera();
    cam.Zoom(1.5);

    renWin.Render();

    // Start the event loop.
    iren.Start();
  }
}
