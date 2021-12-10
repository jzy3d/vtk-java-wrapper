package vtk.processing;

import org.jzy3d.maths.Array;
import vtk.vtkImageData;
import vtk.vtkProbeFilter;
import vtk.vtkUnstructuredGrid;

public class VTKRegularMatrixProcessor {
  public static vtkProbeFilter getProbeFilter(vtkUnstructuredGrid ugrid, String property, int[] dims) {
    ugrid.GetPointData().SetActiveScalars(property); 
    
    double[] range = ugrid.GetScalarRange();
    double[] bounds = ugrid.GetBounds();


    // Define vtkImageData for probiing
    double[] spacing = {(bounds[1] - bounds[0]) / dims[0], (bounds[3] - bounds[2]) / dims[1], (bounds[5] - bounds[4]) / dims[2] };
    
    System.out.println("sampling.dims.x = " + dims[0] + " voxels");
    System.out.println("sampling.dims.y = " + dims[1] + " voxels");
    System.out.println("sampling.dims.z = " + dims[2] + " voxels");
    System.out.println("sampling.spacing.x = " + spacing[0]);
    System.out.println("sampling.spacing.y = " + spacing[1]);
    System.out.println("sampling.spacing.z = " + spacing[2]);
    Array.print("sampling.bounds = ", bounds);
    
    // define a structure for sampling values
    vtkImageData imageData = new vtkImageData(); ;
    imageData.SetDimensions(dims[0], dims[1], dims[2]);
    imageData.SetSpacing(spacing[0], spacing[1], spacing[2]);
    imageData.SetOrigin(bounds[0], bounds[2], bounds[4]);

    // sample data according to the image data settings
    vtkProbeFilter probe = new vtkProbeFilter();
    probe.SetSourceData(ugrid);
    probe.SetInputData(imageData);
    probe.Update();

    return probe;
  }

}
