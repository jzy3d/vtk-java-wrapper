package vtk.examples.gui.jogl;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import vtk.VTKUtils;
import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;
import vtk.vtkRenderWindow;
import vtk.rendering.jogl.vtkAbstractJoglComponent;
import vtk.rendering.jogl.vtkJoglCanvasComponent;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * An application that displays a 3D cone. 
 */
public class DemoVTKPanelJogl {
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
        boolean useSwing = true;
        final vtkAbstractJoglComponent<?> joglWidget = useSwing ? new vtkJoglPanelComponent() : new vtkJoglCanvasComponent();
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
        
        // Report
        if(false) {
          vtkRenderWindow rw = joglWidget.getRenderWindow();
          
          rw.Render();

          System.out.println("direct rendering = " + (rw.IsDirect() == 1));
          System.out.println("opengl supported = " + (rw.SupportsOpenGL() == 1));
          System.out.println("report = " + rw.ReportCapabilities());
          //joglWidget.getVTKLock().unlock();
          
        }
      }
    });
  }
}
