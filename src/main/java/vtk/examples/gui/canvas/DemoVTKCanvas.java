package vtk.examples.gui.canvas;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import vtk.AxesActor;
import vtk.VTKUtils;
import vtk.vtkActor;
import vtk.vtkCanvas;
import vtk.vtkConeSource;
import vtk.vtkPNGWriter;
import vtk.vtkPolyDataMapper;
import vtk.vtkWindowToImageFilter;

/**
 * 
 * <ul>
 * <li>Case 1 : GPU rendering
 * <li>Case 2 : CPU rendering onscreen
 * <li>Case 3 : CPU rendering offscreen
 * </ul>
 * 
 * <h2>CPU rendering onscreen</h2>
 * 
 * LD_LIBRARY_PATH=/home/martin/Dev/jzy3d/external/osmesa:$LD_LIBRARY_PATH
 * LIBGL_ALWAYS_SOFTWARE=true
 * 
 * Must use appropriate native lib folder
 * <ul>
 * <li>For onscreen CPU or GPU rendering : -Djava.library.path=/home/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/vtk-Linux-x86_64 
 * <li>For offscreen CPU rendering : -Djava.library.path=/home/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/vtk-mesa-Linux-x86_64
 * </ul>
 * 
 * @author martin
 *
 */
public class DemoVTKCanvas extends JPanel {
  private static final long serialVersionUID = 1L;

  public static boolean onscreen = true;
  
  public static boolean export = !onscreen;
  public static boolean report = true;// && onscreen; // crashing on ubuntu if offscreen
  
  static {
    VTKUtils.loadVtkNativeLibraries();
  }

  public DemoVTKCanvas(boolean report, boolean export) {
      setLayout(new BorderLayout());
      // Create the buttons.
      vtkCanvas renWin = new vtkCanvas();
      
      add(renWin, BorderLayout.CENTER);
      vtkConeSource cone = new vtkConeSource();
      cone.SetResolution(8);
      vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
      coneMapper.SetInputConnection(cone.GetOutputPort());
  
      vtkActor coneActor = new vtkActor();
      coneActor.SetMapper(coneMapper);
  
      renWin.GetRenderer().AddActor(coneActor);
      AxesActor aa = new AxesActor(renWin.GetRenderer());
      renWin.GetRenderer().AddActor(aa);
      
      // Report about current mode indicating below info (but may crash)
      // direct rendering = false
      // opengl supported = true
      if(report)
        renWin.Report();
      
      // Export window to image
      if(export) {
        vtkWindowToImageFilter w2if = new vtkWindowToImageFilter();
        w2if.SetInput(renWin.GetRenderWindow());
        
        w2if.Update();

        String imagePath = "./vtkCanvas.png";
        vtkPNGWriter writer = new vtkPNGWriter();
        writer.SetFileName(imagePath);
        writer.SetInputConnection(w2if.GetOutputPort());
        writer.Write();        
        
        System.out.println("Exported image to " + imagePath);
      }
    }
  
    public static void main(String s[]) {
      DemoVTKCanvas panel = new DemoVTKCanvas(report, export);
      DemoVTKCanvas panel2 = new DemoVTKCanvas(report, export);
  
      JFrame frame = new JFrame("VTK Canvas Test");
      frame.getContentPane().setLayout(new GridLayout(2, 1));
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(panel);
      frame.getContentPane().add(panel2);
      frame.setSize(600, 600);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
  
      
      VTKUtils.printEnv();
    }
  
}

