package vtk.examples.gui.jogl.gif;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jzy3d.io.gif.GifExporter;
import com.jogamp.opengl.awt.GLJPanel;
import vtk.VTKUtils;
import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;
import vtk.rendering.jogl.vtkAbstractJoglComponent;
import vtk.rendering.jogl.vtkJoglCanvasComponent;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * An application that displays a 3D cone. 
 */
public class DemoVTKExportGifLive {
  public static void main(String s[]) {
    VTKUtils.loadVtkNativeLibraries();

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
        
        
        // -----------------------------------------------
        // GIF EXPORTER REGISTER
        
        GifExporter exporter = new GifExporter(new File("./target/sample.gif"));
        exporter.setDebug(true);
        
        GLJPanel panel = (GLJPanel)joglWidget.getComponent();
        panel.addGLEventListener(new GifExportListener(exporter));
        
        // ------------------------------------------------
        
        // AWT Frame
        JFrame frame = new JFrame("VTK GIF EXPORT");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(joglWidget.getComponent(), BorderLayout.CENTER);
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        
        // ------------------------------------------------
        // GIF : Terminate export on window close

        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            System.out.println("");
            exporter.terminate(100, TimeUnit.SECONDS);
          }
        });
        
        // ------------------------------------------------
      }
    });
  }
}
