package vtk.examples.gui.jogl;

import javax.swing.SwingUtilities;
import vtk.VTKReader;
import vtk.VTKUtils;
import vtk.vtkActor;
import vtk.vtkExodusIIReader;
import vtk.vtkScalarsToColors;
import vtk.vtkUnstructuredGrid;
import vtk.jogl.VTKChart;
import vtk.rendering.jogl.vtkAbstractJoglComponent;

/**
 * A VTK chart built with VTK and using JOGL for rendering.
 *  
 * @author martin
 */
public class DemoVTKChart {
  public static void main(String[] args) {
    VTKUtils.loadVtkNativeLibraries();

    final boolean usePanel = true;

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {

        // String file = "/Volumes/GoogleDrive/Shared drives/Development/FEM Enthalpy
        // method/Outputs/ExampleOutputs/Exodus/3D_Cp_HeatSource/Cp_HS_bornDead_woAdp_subDomain_New_evapBC.e-s301";
        String file =
            "/home/martin/Datasets/thermocalc/Exodus/3D_Cp_HeatSource/Cp_HS_bornDead_woAdp_subDomain_New_evapBC.e-s301";
        String propertyName = "temperature";

        vtkExodusIIReader reader = getFileReader(file, propertyName);

        // --------------

        VTKChart chart = new VTKChart();
        final vtkAbstractJoglComponent<?> canvas = chart.newCanvas(usePanel);

        // Content
        vtkActor actor = chart.createActor(reader, propertyName);
        vtkScalarsToColors vtkScalarsToColors = actor.GetMapper().GetLookupTable();
        canvas.getRenderer().AddActor(actor);

        // Chart layout
        chart.addOrientationAxis(canvas);
        chart.addColorbar(propertyName, vtkScalarsToColors, canvas);
        chart.addBox(reader.GetOutput());

        // UI part
        chart.open(this.getClass().getSimpleName());

        // Start
        canvas.resetCamera();
        canvas.getComponent().requestFocus();
      }

    });
  }
  
  
  public static vtkExodusIIReader getFileReader(String file, String propertyName) {
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

  
  
}
