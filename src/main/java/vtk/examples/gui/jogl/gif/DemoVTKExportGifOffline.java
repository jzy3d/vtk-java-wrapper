package vtk.examples.gui.jogl.gif;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jzy3d.io.gif.AnimatedGifEncoder;
import org.jzy3d.io.gif.GifExporter;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
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
public class DemoVTKExportGifOffline {
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
        
        
        GLJPanel panel = (GLJPanel)joglWidget.getComponent();
        
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
        // Create GIF
        
        // use a listener to be notified when the window is ready for display
        panel.addGLEventListener(new GLEventListener() {
          @Override
          public void init(GLAutoDrawable arg0) {
            makeScreenshots(panel);
          }
          @Override
          public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
          }
          @Override
          public void dispose(GLAutoDrawable arg0) {
          }
          @Override
          public void display(GLAutoDrawable arg0) {
          }
        });

        

        
        // ------------------------------------------------
      }
    });
  }
  
  protected static void makeScreenshots(GLJPanel panel) {
    
    // Configure encoder
    AnimatedGifEncoder encoder = new AnimatedGifEncoder();
    encoder.start(new File("./target/sample.gif").getAbsolutePath());
    encoder.setRepeat(1000);
    encoder.setQuality(10);
    
    // Start exporting pictures, only once the panel is ready
    int nImage = 10;
    
    for (int i = 0; i < nImage; i++) {

      // Get the GL Context (which otherwise is not current since we are here
      // in the main thread, not the AWT Thread)
      GLContext context = panel.getContext();
      context.makeCurrent();
      GL gl = context.getGL();
      
      // Make screenshot
      AWTGLReadBufferUtil screenshotMaker = new AWTGLReadBufferUtil(panel.getGLProfile(), true);
      BufferedImage image = screenshotMaker.readPixelsToBufferedImage(gl, true);
      
      // Do export
      encoder.setDelay(1000+i); // can change for each image
      encoder.addFrame(image);
      
      System.out.println("Exported image " + (i+1) + "/" + nImage);
      
    }

    // Finish GIF
    try {
      encoder.finish();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    System.out.println("Finished generating gif");


  }
}
