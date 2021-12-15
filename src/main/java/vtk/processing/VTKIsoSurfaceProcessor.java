package vtk.processing;

import java.util.HashMap;
import java.util.Map;
import org.jzy3d.maths.Array;
import vtk.vtkFlyingEdges3D;
import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkProbeFilter;
import vtk.vtkUnstructuredGrid;

public class VTKIsoSurfaceProcessor {

  public static vtkPolyData getMultiLevelContour(vtkProbeFilter source, double[] levels) {
    vtkFlyingEdges3D isoSurface = new vtkFlyingEdges3D();
    isoSurface.SetInputData(source.GetOutput());
    isoSurface.SetNumberOfContours(levels.length);

    for (int i = 0; i < levels.length; i++) {
      isoSurface.SetValue(i, levels[i]);
    }
    isoSurface.Update();
    
    System.out.println("Number of output  : " + isoSurface.GetNumberOfOutputPorts());
    System.out.println("Number of contour : " + isoSurface.GetNumberOfContours());
    System.out.println("Number of cells   : " + isoSurface.GetOutput().GetNumberOfCells());
    
    return isoSurface.GetOutput();
  }

  public static Map<Double,vtkPolyData> getSingleLevelContours(vtkProbeFilter source, double[] levels) {
    vtkFlyingEdges3D isoSurface = new vtkFlyingEdges3D();
    isoSurface.SetInputData(source.GetOutput());
    isoSurface.SetNumberOfContours(levels.length);

    
    Map<Double, vtkPolyData> splittedContour = new HashMap<>();

    for (int i = 0; i < levels.length; i++) {
      isoSurface.SetValue(0, levels[i]);
      isoSurface.Update();

      vtkPolyData polyData = new vtkPolyData();
      polyData.DeepCopy(isoSurface.GetOutput());
      splittedContour.put(levels[i], polyData);

      System.out.println("Contour " + i + " ( "+ levels[i] + ") : Number of cells : " + polyData.GetNumberOfCells());
    }
    
    return splittedContour;
  }
}
