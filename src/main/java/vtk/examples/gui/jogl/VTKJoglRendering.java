package vtk.examples.gui.jogl;

import com.google.common.base.Strings;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import vtk.*;
import vtk.rendering.jogl.vtkAbstractJoglComponent;
import vtk.rendering.jogl.vtkJoglCanvasComponent;
import vtk.rendering.jogl.vtkJoglPanelComponent;
import vtk.rendering.vtkAbstractEventInterceptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.ColorModel;
import java.util.Arrays;

public class VTKJoglRendering {
	// -----------------------------------------------------------------
	// Load VTK library and print which library was not properly loaded

	static {
		if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
			for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
				if (!lib.IsLoaded()) {
					System.out.println(lib.GetLibraryName() + " not loaded");
				}
			}
		}
		vtkNativeLibrary.DisableOutputWindow(null);
	}

	public static void main(String[] args) {
		final boolean usePanel = true;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

                //String file = "/Volumes/GoogleDrive/Shared drives/Development/FEM Enthalpy method/Outputs/ExampleOutputs/Exodus/3D_Cp_HeatSource/Cp_HS_bornDead_woAdp_subDomain_New_evapBC.e-s301";
				String file = "C:/Users/Martin/Datasets/thermocalc/Exodus/3D_Cp_HeatSource/Cp_HS_bornDead_woAdp_subDomain_New_evapBC.e-s301";
				String propertyName = "temperature";

				vtkExodusIIReader reader = (vtkExodusIIReader) VTKReader.getReader(file);
				int[] timerange = reader.GetTimeStepRange();

				vtkUnstructuredGrid[] ugrids = VTKReader.read_exodusii_grids(reader, new int[]{1, 0}, timerange[1], propertyName);

				// Fetch metadata.
				reader.UpdateInformation();

				// Read data.
				reader.Update();

				System.out.println("NumberOfTimeSteps = " + reader.GetNumberOfTimeSteps());
				System.out.println("NumberOfElementBlockArrays = " + reader.GetNumberOfElementBlockArrays());
				System.out.println("NumberOfPointResultArrays = " + reader.GetNumberOfPointResultArrays());
				System.out.println("NumberOfElementResultArrays = " + reader.GetNumberOfElementResultArrays());
				System.out.println("NumberOfGlobalResultArrays = " + reader.GetNumberOfGlobalResultArrays());

				// --------------
				vtkActor actor = new vtkActor();
				vtkMultiBlockDataSet output = reader.GetOutput();
				vtkCompositeDataIterator iter = output.NewIterator();
				vtkScalarsToColors vtkScalarsToColors = null;
				
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
					vtkScalarsToColors = mapper.GetLookupTable();
					System.out.println("Range=" + r[0] + "," + r[1]);
					actor.SetMapper(mapper);
				}

				actor.GetProperty().SetEdgeVisibility(1);

				// VTK rendering part
				final vtkAbstractJoglComponent<?> joglWidget = usePanel ? new vtkJoglPanelComponent() : new vtkJoglCanvasComponent();
				System.out.println("We are using " + joglWidget.getComponent().getClass().getName() + " for the rendering.");

				joglWidget.getRenderer().AddActor(actor);

				// Add orientation axes
				vtkAbstractJoglComponent.attachOrientationAxes(joglWidget);

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

				// Add interactive 3D Widget
				final vtkBoxRepresentation representation = new vtkBoxRepresentation();
				representation.SetPlaceFactor(1.25);
				double[] box = new double[6];
				output.GetBounds(box);
				representation.PlaceWidget(box);

				representation.VisibilityOn();
				representation.HandlesOn();

				// UI part
				JFrame frame = new JFrame("VTK with JFrame and JOGL 2.4 RC2");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().setLayout(new BorderLayout());
				frame.getContentPane().add(joglWidget.getComponent(), BorderLayout.CENTER);
				frame.setSize(1200, 600);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				joglWidget.resetCamera();
				joglWidget.getComponent().requestFocus();
			}
		});
	}
}