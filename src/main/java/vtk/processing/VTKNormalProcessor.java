package vtk.processing;

import vtk.vtkAlgorithmOutput;
import vtk.vtkDataArray;
import vtk.vtkDataObject;
import vtk.vtkGeometryFilter;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;

/**
 * A simple wrapper on two algorithm allowing to compute normals out of an
 * {@link vtkAlgorithmOutput}
 * 
 * @see https://vtk.org/doc/nightly/html/classvtkPolyDataNormals.html (pay attention to the actual
 *      version number of doc. We worked this with 9.0.3)
 * 
 * @author martin
 *
 */
public class VTKNormalProcessor {
  protected vtkGeometryFilter outerSurfFilter;
  protected vtkPolyDataNormals normalsCompute;

  protected boolean perVertex;


  protected VTKNormalProcessor(vtkAlgorithmOutput output, vtkDataObject data, boolean perVertex) {
    this.perVertex = perVertex;

    // This is supposed to filter outer surface, but actually does not filtler anything but 
    // converts source to polygons, which is our goal here
    outerSurfFilter = new vtkGeometryFilter();
    if(output!=null) {
      outerSurfFilter.SetInputConnection(output);
    }
    else if(data!=null) {
      outerSurfFilter.SetInputData(data);
    }

    outerSurfFilter.Update();

    // -------------------------------------------
    // This will process normals 
    
    normalsCompute = new vtkPolyDataNormals();
    normalsCompute.SetInputConnection(outerSurfFilter.GetOutputPort());

    if (perVertex) {
      normalsCompute.ComputePointNormalsOn();
      normalsCompute.ComputeCellNormalsOff();
    } else {
      normalsCompute.ComputePointNormalsOff();
      normalsCompute.ComputeCellNormalsOn();
    }

    /**
     * Turn on/off the enforcement of consistent polygon ordering.
     */
    normalsCompute.ConsistencyOn();

    /**
     * This assumes a completely closed surface (i.e. no boundary edges) and no non-manifold edges.
     * If these constraints do not hold, all bets are off. This option adds some computational
     * complexity, and is useful if you don't want to have to inspect the rendered image to
     * determine whether to turn on the FlipNormals flag. However, this flag can work with the
     * FlipNormals flag, and if both are set, all the normals in the output will point "inward".
     */
    normalsCompute.AutoOrientNormalsOn();

    /**
     * Flipping reverves the meaning of front and back for Frontface and Backface culling in
     * vtkProperty. Flipping modifies both the normal direction and the order of a cell's points.
     */
    normalsCompute.FlipNormalsOff();

    /**
     * Specify the angle that defines a sharp edge.
     * 
     * If the difference in angle across neighboring polygons is greater than this value, the shared
     * edge is considered "sharp".
     */
    normalsCompute.SetFeatureAngle(180);

    /**
     * Turn on/off the splitting of sharp edges.
     */
    normalsCompute.SplittingOff();

  }

  public VTKNormalProcessor update() {
    normalsCompute.Update();
    return this;
  }

  /**
   * Returns normals per vertex or per cell according to the input settings. In case of hexahedron
   * cells, the number of normals is not 1 per hexahedron but 1 per polygon.
   * 
   * @return
   */
  public vtkDataArray getOutput() {
    if (perVertex) {
      return normalsCompute.GetOutput().GetPointData().GetNormals();
    } else {
      return normalsCompute.GetOutput().GetCellData().GetNormals();
    }
  }
  
  public vtkAlgorithmOutput getOutputPort() {
    return normalsCompute.GetOutputPort();
  }


  public vtkPolyData getInputAsPolyDataWithNormals() {
      return normalsCompute.GetOutput();
  }

  public vtkAlgorithmOutput getInputAsPolyDataWithNormalsPort() {
    return normalsCompute.GetOutputPort();
}

  public vtkPolyData getInputAsPolyData() {
    return outerSurfFilter.GetOutput();
  }

  public vtkAlgorithmOutput getInputAsPolyDataPort() {
    return outerSurfFilter.GetOutputPort();
  }

  ///////////////////////////////////////
  
  public static class VTKNormalPerVertex extends VTKNormalProcessor{
    public VTKNormalPerVertex(vtkAlgorithmOutput output) {
      super(output, null, true);
    }
    
    public VTKNormalPerVertex(vtkDataObject output) {
      super(null, output, true);
    }
  }


  public static class VTKNormalPerPolygon extends VTKNormalProcessor{
    public VTKNormalPerPolygon(vtkAlgorithmOutput output) {
      super(output, null, false);
    }
    
    public VTKNormalPerPolygon(vtkDataObject output) {
      super(null, output, false);
    }
  }
}
