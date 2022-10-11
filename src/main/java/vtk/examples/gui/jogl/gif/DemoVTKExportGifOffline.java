package vtk.examples.gui.jogl.gif;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFrame;
import org.jzy3d.io.gif.AnimatedGifEncoder;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import vtk.VTKUtils;
import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;
import vtk.vtkRenderer;
import vtk.rendering.jogl.vtkAbstractJoglComponent;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * In this demo, we generate GIF with 1 second long frames but compute 3D in VTK much faster.
 * 
 * This shows how to create GIFs without doing it live.
 */
public class DemoVTKExportGifOffline {
  public static void main(String s[]) throws InterruptedException, IOException {
    VTKUtils.loadVtkNativeLibraries();

    // ------------------------------------------------
    // VTK scene
    vtkConeSource cone = new vtkConeSource();
    cone.SetResolution(8);

    vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
    coneMapper.SetInputConnection(cone.GetOutputPort());

    vtkActor actor = new vtkActor();
    actor.SetMapper(coneMapper);

    // ------------------------------------------------
    // JOGL components

    final vtkAbstractJoglComponent<?> joglWidget = new vtkJoglPanelComponent();// : new vtkJoglCanvasComponent();
    joglWidget.getRenderer().AddActor(actor);
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
    // Wait for the windows to displayed
    CountDownLatch latch = new CountDownLatch(1);
    
    // use a listener to be notified when the window is ready for display
    panel.addGLEventListener(new GLEventListener() {
      @Override
      public void init(GLAutoDrawable arg0) {
      }
      @Override
      public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
      }
      @Override
      public void dispose(GLAutoDrawable arg0) {
      }
      @Override
      public void display(GLAutoDrawable arg0) {
        latch.countDown();
      }
    });
    
    // pause until first call to GLEventListener.display() is made
    latch.await();

    // ------------------------------------------------
    // Create GIF
    
    int nImage = 10;
    //int interval = 500; // in ms, to let us time to rotate scene
    makeScreenshots(panel, nImage, joglWidget.getActiveCamera(), joglWidget.getRenderer());
    
    // ------------------------------------------------  
    
    frame.setVisible(false);
    System.exit(0);
  }
  
  protected static void makeScreenshots(GLJPanel panel, int nImage, vtkCamera camera, vtkRenderer renderer) throws IOException, InterruptedException {
    
    // Configure encoder
    AnimatedGifEncoder encoder = new AnimatedGifEncoder();
    encoder.start(new File("./target/sample.gif").getAbsolutePath());
    encoder.setRepeat(1000);
    encoder.setQuality(10);
    
    // Start exporting pictures, only once the panel is ready
    
    for (int i = 0; i < nImage; i++) {
      
      // Get the GL Context (which otherwise is not current since we are here
      // in the main thread, not the AWT Thread)
      GLContext context = panel.getContext();
      context.makeCurrent();
      GL gl = context.getGL();
      
      // Make screenshot
      AWTGLReadBufferUtil screenshotMaker = new AWTGLReadBufferUtil(panel.getGLProfile(), true);
      BufferedImage image = screenshotMaker.readPixelsToBufferedImage(gl, true);
      
      // Add info
      Graphics2D g = (Graphics2D)image.createGraphics();
      g.setColor(Color.RED);
      g.drawString("Image " + i, 10, image.getHeight() - 10);
      g.dispose();
      
      // Configure delay in the output gif
      encoder.setDelay(1000+i); // can change for each image
      
      // Do export
      encoder.addFrame(image);
      
      System.out.println("Exported image " + (i+1) + "/" + nImage);

      // Edit display to see something different at each image
      camera.Elevation(Math.PI*i/nImage);
      camera.Azimuth(Math.PI*i/nImage);
      camera.Roll(Math.PI*i/nImage);
      camera.OrthogonalizeViewUp();
      camera.ParallelProjectionOn();
      renderer.Render();
      
      //Thread.sleep(100);
    }

    // Finish GIF
    encoder.finish();
    System.out.println("Finished generating gif");


  }
}
