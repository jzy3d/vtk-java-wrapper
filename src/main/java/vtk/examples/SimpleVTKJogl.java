package vtk.examples;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import vtk.VTKUtils;
import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkNativeLibrary;
import vtk.vtkPanel;
import vtk.vtkPolyDataMapper;
import vtk.rendering.jogl.vtkAbstractJoglComponent;
import vtk.rendering.jogl.vtkJoglCanvasComponent;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * An application that displays a 3D cone. 
 */
public class SimpleVTKJogl {

  // -----------------------------------------------------------------
  // Load VTK library and print which library was not properly loaded
  static {
    VTKUtils.loadVtkNativeLibraries();
  }

  public static void main(String s[]) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {

        // VTK scene
        vtkConeSource cone = new vtkConeSource();
        cone.SetResolution(8);

        vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
        coneMapper.SetInputConnection(cone.GetOutputPort());

        vtkActor actor = new vtkActor();
        actor.SetMapper(coneMapper);

        // JOGL components
        boolean usePanel = false;
        final vtkAbstractJoglComponent<?> joglWidget = usePanel ? new vtkJoglPanelComponent() : new vtkJoglCanvasComponent();
        System.out.println("We are using " + joglWidget.getComponent().getClass().getName() + " for the rendering.");

        joglWidget.getRenderer().AddActor(actor);
        
        // AWT Frame
        JFrame frame = new JFrame("SimpleVTK");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(joglWidget.getComponent(), BorderLayout.CENTER);
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
      }
    });
  }
}
