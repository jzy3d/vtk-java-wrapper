package vtk.rendering.jogl;

import com.jogamp.opengl.GLCapabilities;
import vtk.vtkRenderWindow;

/**
 * Overrides JOGL canvas to more cleanly deal with resources and allow to remove and re-add a
 * component from a layout.
 * 
 * @author Martin
 */
public class VTKRemoveableCanvasAWT extends vtkJoglCanvasComponent {
  protected boolean keepVTKResources = false;

  public VTKRemoveableCanvasAWT() {
    super();
  }

  public VTKRemoveableCanvasAWT(vtkRenderWindow arg0, GLCapabilities arg1) {
    super(arg0, arg1);
  }

  public VTKRemoveableCanvasAWT(vtkRenderWindow arg0) {
    super(arg0);
  }

  /**
   * Allows keeping VTK resources hence keeping the interactor.
   * 
   * This should be set to true before removing a panel that will be reused later. It should then be
   * set back to false right after addition.
   * 
   * <pre>
   * <code>
   * joglWidget.keepVTKResources(true);
   * frame.getContentPane().remove(joglWidget.getComponent());
   * joglWidget.keepVTKResources(false);
   * frame.getContentPane().add(joglWidget.getComponent(), BorderLayout.CENTER);
   * </code>
   * </pre>
   */
  public void keepVTKResources(boolean on) {
    this.keepVTKResources = on;
  }

  /**
   * Customize Delete to allow keeping VTK ressources if the panel is supposed to be re-used.
   */
  @Override
  public void Delete() {
    this.glRenderWindow.Finalize();

    if (this.keepVTKResources) {
      return;
    }
    super.Delete();
  }

}
