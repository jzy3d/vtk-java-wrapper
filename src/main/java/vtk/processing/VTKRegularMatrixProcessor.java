package vtk.processing;

import org.jzy3d.maths.Array;
import vtk.vtkImageData;
import vtk.vtkProbeFilter;
import vtk.vtkUnstructuredGrid;

/**
 * Allows building a regular 3D matrix out of an {@link vtkUnstructuredGrid} which allows processing
 * contour or build a volume from the input data.
 * 
 * @author martin
 */
public class VTKRegularMatrixProcessor {
  public static vtkProbeFilter getProbeFilter(vtkUnstructuredGrid ugrid, String property,
      int[] dims) {
    return getProbeFilter(ugrid, property, dims, 0);
  }

  /**
   * A probe filter allows building a 3D grid made of voxels that can later be used to process
   * contour OR build a volume of the input data.
   * 
   * @param ugrid the input data
   * @param property the property on which voxels should have their value based
   * @param dims the number of voxel for each dimensions (dims[0] for X, dims[1] for Y, dims[2] for
   *        Z)
   * @param growRatio the input bounds growth applied to ensure all points lead to a voxel
   * @return
   */
  public static vtkProbeFilter getProbeFilter(vtkUnstructuredGrid ugrid, String property,
      int[] dims, double growRatio) {
    ugrid.GetPointData().SetActiveScalars(property);

    double[] range = ugrid.GetScalarRange();
    double[] bounds = ugrid.GetBounds();

    if (growRatio > 0) {
      growBounds(bounds, growRatio);
    }

    // Define vtkImageData for probiing
    double[] spacing = {(bounds[1] - bounds[0]) / dims[0], (bounds[3] - bounds[2]) / dims[1],
        (bounds[5] - bounds[4]) / dims[2]};

    System.out.println("sampling.dims.x = " + dims[0] + " voxels");
    System.out.println("sampling.dims.y = " + dims[1] + " voxels");
    System.out.println("sampling.dims.z = " + dims[2] + " voxels");
    System.out.println("sampling.spacing.x = " + spacing[0]);
    System.out.println("sampling.spacing.y = " + spacing[1]);
    System.out.println("sampling.spacing.z = " + spacing[2]);
    Array.print("sampling.bounds = ", bounds);

    // define a structure for sampling values
    vtkImageData imageData = new vtkImageData();;
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

  private static void growBounds(double[] bounds, double ratio) {
    for (int i = 0; i < 6; i += 2) {
      double range = bounds[i + 1] - bounds[i];
      bounds[i] = bounds[i] - ratio * range;
      bounds[i + 1] = bounds[i + 1] + ratio * range;
    }
  }

}
