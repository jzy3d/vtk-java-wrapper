package vtk.processing;

import vtk.vtkAlgorithmOutput;
import vtk.vtkCleanPolyData;
import vtk.vtkGeometryFilter;
import vtk.vtkPolyData;

/**
 * A helper class to merge duplicates points of a geometry.
 * 
 * Useful to
 * <ul>
 * <li>work with unstructured grid that has cells that are not sharing points among each other.
 * <li>work on a geometry loaded from a PVTU file (the multiple VTU files will load a concatenation
 * of cells so even if each VTU has shared points among cell, the concatenated VTUs will have non
 * merged points at their border).
 * </ul>
 * 
 * @see https://discourse.vtk.org/t/vtkgeometryfilter-and-its-evil-spawn-vtkdatasetsurfacefilter/4083/5
 */
public class VTKCommonPointsMerge {
  protected vtkGeometryFilter convertToPoly;
  protected vtkCleanPolyData cleanPolyData;


  public VTKCommonPointsMerge(vtkAlgorithmOutput output) {

    // ---------------------------------------------------------------------------------------------
    // https://vtk.org/doc/nightly/html/classvtkGeometryFilter.html
    //
    // extract boundary geometry from dataset (or convert data to polygonal type)
    //
    // vtkGeometryFilter is a general-purpose filter to extract dataset boundary geometry, topology,
    // and associated attribute data from any type of dataset. Geometry is obtained as follows: all
    // 0D, 1D, and 2D cells are extracted. All 2D faces that are used by only one 3D cell (i.e.,
    // boundary faces) are extracted. It also is possible to specify conditions on point ids, cell
    // ids, and on a bounding box (referred to as "Extent") to control the extraction process. This
    // point and cell id- and extent-based clipping is a powerful way to "see inside" datasets;
    // however it may impact performance significantly.
    //
    // This filter may also be used to convert any type of data to polygonal type. This is
    // particularly useful for surface rendering. The conversion process may be less than
    // satisfactory for some 3D datasets. For example, this filter will extract the outer surface of
    // a volume or structured grid dataset (if point, cell, and extent clipping is disabled). (For
    // structured data you may want to use vtkImageDataGeometryFilter,
    // vtkStructuredGridGeometryFilter, vtkExtractUnstructuredGrid,
    // vtkRectilinearGridGeometryFilter, or vtkExtractVOI.)
    //
    // Another important feature of vtkGeometryFilter is that it preserves topological connectivity.
    // This enables filters that depend on correct connectivity (e.g., vtkQuadricDecimation,
    // vtkFeatureEdges, etc.) to operate properly . It is possible to label the output polydata with
    // an originating cell (PassThroughCellIds) or point id (PassThroughPointIds). The output
    // precision of created points (if they need to be created) can also be specified.
    //
    // In some cases (especially for large unstructured grids) the vtkGeometryFilter can be slow.
    // Consequently the filter has an optional "fast mode" that may execute significantly faster
    // (>4-5x) than normal execution. The fast mode visits a subset of cells that may be on the
    // boundary of the dataset (and skips interior cells which contribute nothing to the output).
    // The set of subsetted cells is determined by inspecting the topological connectivity degree of
    // each point (i.e., the number of unique cells using a particular point is that point's
    // degree). With fast mode enabled, those cells connected to a point with degree <= Degree are
    // visited. Note that this approach may miss some cells which contribute boundary faces–thus the
    // output is an approximation to the normal execution of vtkGeometryFilter.
    //
    // Finally, this filter takes an optional second, vtkPolyData input. This input represents a
    // list of faces that are to be excluded from the output of vtkGeometryFilter.
    // ---------------------------------------------------------------------------------------------
    
    // This is supposed to filter outer surface, but actually does not filter anything when input is
    // made of hexahedron with non merged points but
    // converts source to polygons, which is our goal here

    
    convertToPoly = new vtkGeometryFilter();
    convertToPoly.SetInputConnection(output);

    // --------------------------------------------------------------------------------------------
    // https://vtk.org/doc/nightly/html/classvtkCleanPolyData.html
    //
    // merge duplicate points, and/or remove unused points and/or remove degenerate cells
    //
    // vtkCleanPolyData is a filter that takes polygonal data as input and generates polygonal
    // data
    // as output. vtkCleanPolyData will merge duplicate points (within specified tolerance and if
    // enabled), eliminate points that are not used in any cell, and if enabled, transform
    // degenerate cells into appropriate forms (for example, a triangle is converted into a line
    // if
    // two points of triangle are merged).
    //
    // Conversion of degenerate cells is controlled by the flags ConvertLinesToPoints,
    // ConvertPolysToLines, ConvertStripsToPolys which act cumulatively such that a degenerate
    // strip
    // may become a poly. The full set is Line with 1 points -> Vert (if ConvertLinesToPoints)
    // Poly
    // with 2 points -> Line (if ConvertPolysToLines) Poly with 1 points -> Vert (if
    // ConvertPolysToLines && ConvertLinesToPoints) Strp with 3 points -> Poly (if
    // ConvertStripsToPolys) Strp with 2 points -> Line (if ConvertStripsToPolys &&
    // ConvertPolysToLines) Strp with 1 points -> Vert (if ConvertStripsToPolys &&
    // ConvertPolysToLines && ConvertLinesToPoints)
    //
    // Cells of type VTK_POLY_LINE will be converted to a vertex only if ConvertLinesToPoints is
    // on
    // and all points are merged into one. Degenerate line segments (with two identical end
    // points)
    // will be removed.
    //
    // If tolerance is specified precisely=0.0, then vtkCleanPolyData will use the vtkMergePoints
    // object to merge points (which is faster). Otherwise the slower vtkIncrementalPointLocator
    // is
    // used. Before inserting points into the point locator, this class calls a function
    // OperateOnPoint which can be used (in subclasses) to further refine the cleaning process.
    // See
    // vtkQuantizePolyDataPoints.
    //
    // In addition, if a point global id array is available, then two points are merged if and
    // only
    // if they share the same global id.
    //
    // Note that merging of points can be disabled. In this case, a point locator will not be
    // used,
    // and points that are not used by any cells will be eliminated, but never merged.
    //
    // Warning
    // Merging points can alter topology, including introducing non-manifold forms. The tolerance
    // should be chosen carefully to avoid these problems. Subclasses should handle
    // OperateOnBounds
    // as well as OperateOnPoint to ensure that the locator is correctly initialized (i.e. all
    // modified points must lie inside modified bounds).
    // If you wish to operate on a set of coordinates that has no cells, you must add a
    // vtkPolyVertex cell with all of the points to the PolyData (or use a vtkVertexGlyphFilter)
    // before using the vtkCleanPolyData filter.
    //
    // See also https://vtk.org/doc/nightly/html/classvtkStaticCleanPolyData.html
    // ---------------------------------------------------------------------------------------------

    cleanPolyData = new vtkCleanPolyData();
    cleanPolyData.SetInputConnection(convertToPoly.GetOutputPort());
    cleanPolyData.SetTolerance(0.0);
    cleanPolyData.ToleranceIsAbsoluteOff(); // relative to bounding box

    cleanPolyData.PointMergingOn();

    cleanPolyData.ConvertLinesToPointsOff();
    cleanPolyData.ConvertPolysToLinesOff();
    cleanPolyData.ConvertStripsToPolysOff();
  }

  public void update() {
    cleanPolyData.Update();
  }

  public vtkPolyData getOutput() {
    return cleanPolyData.GetOutput();
  }

  public vtkAlgorithmOutput getOutputPort() {
    return cleanPolyData.GetOutputPort();
  }

}
