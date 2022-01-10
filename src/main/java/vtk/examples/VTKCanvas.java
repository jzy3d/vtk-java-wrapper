package vtk.examples;


import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import vtk.AxesActor;
import vtk.VTKUtils;
import vtk.vtkActor;
import vtk.vtkCanvas;
import vtk.vtkConeSource;
import vtk.vtkNativeLibrary;
import vtk.vtkObject;
import vtk.vtkPolyDataMapper;
import vtk.vtkReferenceInformation;


public class VTKCanvas extends JPanel {
  private static final long serialVersionUID = 1L;

  static {
    VTKUtils.loadVtkNativeLibraries();
  }

  public VTKCanvas() {
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
    }
  
    public static void main(String s[]) {
      VTKCanvas panel = new VTKCanvas();
      VTKCanvas panel2 = new VTKCanvas();
  
      JFrame frame = new JFrame("VTK Canvas Test");
      frame.getContentPane().setLayout(new GridLayout(2, 1));
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(panel);
      frame.getContentPane().add(panel2);
      frame.setSize(600, 600);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
  
      /*vtkReferenceInformation infos = vtkObject.JAVA_OBJECT_MANAGER.gc(true);
      System.out.println(infos);
      System.out.println(infos.listRemovedReferenceToString());
      System.out.println(infos.listKeptReferenceToString());*/
    }
  
}

