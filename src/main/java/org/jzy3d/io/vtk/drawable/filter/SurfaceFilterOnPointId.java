package org.jzy3d.io.vtk.drawable.filter;

import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jzy3d.io.vtk.drawable.IDrawableBuilder;
import com.google.common.collect.ArrayListMultimap;
import vtk.VTKGeometry;
import vtk.vtkCellArray;
import vtk.vtkCellArrayIterator;
import vtk.vtkDataSet;

/**
 * ==================
 * WIP ONLY
 * ==================
 * 
 * Process a {@link vtkCellArray} to identify all cells that have shared points.
 * 
 * @author Martin
 *
 */
public class SurfaceFilterOnPointId {
  protected static Logger log = Logger.getLogger(SurfaceFilterOnPointId.class);

  protected vtkDataSet dataset;
  protected vtkCellArray cells;

  protected Set<Integer> keep;
  protected Set<Integer> drop;

  protected ArrayListMultimap<Integer, Integer> pointToCell = ArrayListMultimap.create();
  protected VTKCell[] parsedCells; 
  
  public SurfaceFilterOnPointId(vtkDataSet dataset, vtkCellArray cells) {
    this.dataset = dataset;
    this.cells = cells;

    pointToCell = ArrayListMultimap.create();

    vtkCellArrayIterator it = cells.NewIterator();

    parsedCells = new VTKCell[(int) cells.GetNumberOfCells()]; 
    int c = 0;
    
    for (it.GoToFirstCell(); !it.IsDoneWithTraversal(); it.GoToNextCell()) {
      int cellId = (int) it.GetCurrentCellId();
      int cellType = dataset.GetCellType(cellId);
      int cellStartPointId = (int) cells.GetOffsetsArray().GetTuple1(cellId);
      int cellStopPointId = (int) cells.GetOffsetsArray().GetTuple1(cellId + 1);
      
      //it.ge

      if (VTKGeometry.VTK_HEXAHEDRON == cellType) {
        parsedCells[c++] = addHexahedronPolygons(cellId, cellStartPointId, cellStopPointId);
      }
      else {
        log.error("Unsupported cell type " + cellType);
      }
    }
    
    this.keep = new HashSet<>();
    this.drop = new HashSet<>();

    for(Integer pointId : pointToCell.keySet()) {
      int sharedHexahedrons = pointToCell.get(pointId).size();
      if(1<=sharedHexahedrons && sharedHexahedrons<=4) {
        keep.add(pointId);
        //if(sharedHexahedrons>1)
        //System.out.println(sharedHexahedrons +" shared");
      }
      else {
        // a point inside 
        drop.add(pointId);
      }
    }
  }
  
  public class VTKCell{
    int id;
    int[] points;
  }
  

  /**
   * Load an hexahedron.
   * 
   * @param cellId id of the cell
   * @param cellStartPointId starting cell
   * @param cellStopPointId stoping cell
   */
  protected VTKCell addHexahedronPolygons(int cellId, int cellStartPointId, int cellStopPointId) {

    // --------------------------------------
    // Check consistency of input

    if (cellStopPointId - cellStartPointId != IDrawableBuilder.HEXAHEDRON_POINTS) {
      throw new IllegalArgumentException(
          "Hexahedron supposed to have 8 points, not " + (cellStopPointId - cellStartPointId));
    }

    
    VTKCell cell = new VTKCell();
    cell.id = cellId;
    cell.points = new int[IDrawableBuilder.HEXAHEDRON_POINTS];
    
    // --------------------------------------
    // Load coordinates, colors and normals

    for (int i = cellStartPointId; i < cellStopPointId; i++) {
      // Index in input cell and output hexahedron points
      int datasetPointId = (int) cells.GetConnectivityArray().GetTuple1(i);
      int hexahedPointId = i - cellStartPointId;

      pointToCell.put(datasetPointId, cellId);

      cell.points[hexahedPointId] = datasetPointId;
      
      // if (debug)
      // System.out.print(datasetPointId + " ");

      // Get coordinate
      // float value = coloringProperty[datasetPointId];
      // Coord3d coord = coordinates[datasetPointId];

      // Get color
      // Color color = colormap.getColor(0, 0, value, propertyRange.getMin(),
      // propertyRange.getMax());
      // hexaHedronPoints[hexahedPointId] = new Point(coord, color);
      // hexahedronNormals[hexahedPointId] = datasetPointId;

    }

    return cell;
  }

}
