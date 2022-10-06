package vtk.examples.gui.jogl.gif;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.jzy3d.io.gif.GifExporter;
import org.jzy3d.maths.TicToc;
import org.jzy3d.maths.Utils;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.jogamp.opengl.util.awt.Overlay;

public class GifExportListener implements GLEventListener {
  protected AWTGLReadBufferUtil screenshotMaker =
      new AWTGLReadBufferUtil(GLProfile.getGL2GL3(), true);
  protected GifExporter exporter;

  protected Overlay overlay;

  protected TicToc timer = new TicToc();

  protected java.awt.Color overlayColor = java.awt.Color.gray;
  protected java.awt.Font font = new java.awt.Font("Arial", Font.PLAIN, 14);
  
  protected int imageCount = 0;


  public GifExportListener(GifExporter exporter) {
    this.exporter = exporter;
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    timer.tic();
  }


  @Override
  public void display(GLAutoDrawable drawable) {
    exportImageIfRequired(drawable.getGL());

  }

  @Override
  public void dispose(GLAutoDrawable drawable) {}


  @Override
  public void reshape(GLAutoDrawable drawable, int arg1, int arg2, int arg3, int arg4) {
    System.err.println("Warning : GIF do not support resize");
  }

  protected void exportImageIfRequired(GL gl) {
    imageCount++;
    
    if (exporter != null) {
      // Count elapsed time
      timer.toc();
      String elapsed = Utils.num2str(timer.elapsedSecond(), 4) + " seconds";
      String nbImage = "Image " + imageCount;
      String info = nbImage + " @ " + elapsed + " | " + exporter.getDelay() + " ms delay";

      // Make screenshot
      BufferedImage i = screenshotMaker.readPixelsToBufferedImage(gl, true);

      // Add informations to image
      Graphics2D g = (Graphics2D)i.createGraphics();
      g.setColor(overlayColor);
      g.setFont(font);
      g.drawString(info, 10, i.getHeight()-10);

      // Do export
      exporter.export(i);
    }
  }


}

