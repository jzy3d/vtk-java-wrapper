package vtk.processing;

import java.util.ArrayList;
import java.util.List;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.text.DrawableTextWrapper;
import org.jzy3d.plot3d.text.drawable.DrawableTextBitmap;
import vtk.VTKGeometry;
import vtk.vtkAlgorithmOutput;
import vtk.vtkCellArray;
import vtk.vtkCellArrayIterator;
import vtk.vtkContourFilter;
import vtk.vtkDataArray;
import vtk.vtkDataSet;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkStripper;

/**
 * Compute contour out of a VTK source and provide Jzy3D drawable elements
 * 
 * @see https://vtk.org/doc/nightly/html/classvtkContourFilter.html
 * @see https://kitware.github.io/vtk-examples/site/Cxx/Visualization/LabelContours/
 * 
 * @author martin
 */
public class VTKIsoLineProcessor {
  protected vtkContourFilter contours;
  protected vtkStripper contourStripper;

  protected List<DrawableTextWrapper> drawableContourLabels = new ArrayList<>();
  protected List<LineStrip> drawableContourLines = new ArrayList<>();

  protected boolean debug = false;

  protected boolean buildIsoLines = true;
  protected boolean buildIsoSurfaces = false;

  /**
   * Process contour from a range of values for a given number of contour levels.
   * 
   * @param source
   * @param n
   * @param from
   * @param to
   */
  public VTKIsoLineProcessor(vtkAlgorithmOutput source, int n, double from, double to) {
    
    // contour filter
    contours = new vtkContourFilter();
    contours.SetInputConnection(source);
    contours.GenerateValues(n, from, to);
    
    //contours.GenerateTrianglesOn();
    //contours.ComputeGradientsOn();

    contourStripper = new vtkStripper();
    contourStripper.SetInputConnection(contours.GetOutputPort());
    contourStripper.Update();
    
    // generate jzy3D drawables
    readResultAndBuildDrawables();
  }

  /**
   * Process contour from a user given list of contour levels. 
   * 
   * @param source
   * @param levels
   */
  public VTKIsoLineProcessor(vtkAlgorithmOutput source, double[] levels) {

    // contour filter
    contours = new vtkContourFilter();
    contours.SetInputConnection(source);
    
    for (int i = 0; i < levels.length; i++) {
      contours.SetValue(i, levels[i]);      
    }
    

    // transform contour result to cells for drawing
    contourStripper = new vtkStripper();
    contourStripper.SetInputConnection(contours.GetOutputPort());
    contourStripper.Update();
    
    // generate jzy3D drawables
    readResultAndBuildDrawables();
  }

  protected void readResultAndBuildDrawables() {
    
    drawableContourLabels = new ArrayList<>();
    drawableContourLines = new ArrayList<>();

    vtkDataSet dataset = contourStripper.GetOutput();
    vtkPoints points = contourStripper.GetOutput().GetPoints();
    vtkDataArray scalars = contourStripper.GetOutput().GetPointData().GetScalars();

    // -----------------------------------------------------------
    // LOAD ISO-LINES AS DRAWABLES
    
    if(buildIsoLines)
      readIsoLines(dataset, points, scalars);
    
    // -----------------------------------------------------------
    // LOAD ISO-SURFACE AS CELLS

    if(buildIsoSurfaces)
      readIsoSurfaces(dataset, points);

  }

  protected void readIsoSurfaces(vtkDataSet dataset, vtkPoints points) {
    vtkCellArray polygonCells = contourStripper.GetOutput().GetPolys();
    
    vtkCellArrayIterator polygonCellIter = polygonCells.NewIterator();

    for (polygonCellIter.GoToFirstCell(); !polygonCellIter.IsDoneWithTraversal(); polygonCellIter.GoToNextCell()) {

      vtkIdList cell = polygonCellIter.GetCurrentCell();
      
      // --------------------------------------
      // Prepare cell iteration
      
      int cellId = (int) polygonCellIter.GetCurrentCellId();
      int cellType = dataset.GetCellType(cellId);
      int cellStartPointId = (int) polygonCells.GetOffsetsArray().GetTuple1(cellId);
      int cellStopPointId = (int) polygonCells.GetOffsetsArray().GetTuple1(cellId + 1);

      if (debug)
        System.out.println(VTKGeometry.name(cellType) + " " + cellId);


      // -------------------------------------
      // Build a contour line
      
      /*LineStrip line = new LineStrip(Color.BLACK);
      line.setWidth(3);

      for (int i = cellStartPointId; i < cellStopPointId; i++) {
        int datasetPointId = (int) polygonCells.GetConnectivityArray().GetTuple1(i);

        Coord3d c = new Coord3d(points.GetData().GetTuple3(datasetPointId));

        line.add(c);
      }*/

      
    }
  }

  protected vtkCellArray readIsoLines(vtkDataSet dataset, vtkPoints points, vtkDataArray scalars) {
    vtkCellArray lineCells = contourStripper.GetOutput().GetLines();
    vtkCellArrayIterator lineCellIter = lineCells.NewIterator();

    
    for (lineCellIter.GoToFirstCell(); !lineCellIter.IsDoneWithTraversal(); lineCellIter.GoToNextCell()) {

      vtkIdList cell = lineCellIter.GetCurrentCell();
      
      // --------------------------------------
      // Prepare cell iteration
      
      int cellId = (int) lineCellIter.GetCurrentCellId();
      int cellType = dataset.GetCellType(cellId);
      int cellStartPointId = (int) lineCells.GetOffsetsArray().GetTuple1(cellId);
      int cellStopPointId = (int) lineCells.GetOffsetsArray().GetTuple1(cellId + 1);

      if (debug)
        System.out.println(VTKGeometry.name(cellType) + " " + cellId);


      // -------------------------------------
      // Build a contour line
      
      LineStrip line = new LineStrip(Color.BLACK);
      line.setWidth(3);

      for (int i = cellStartPointId; i < cellStopPointId; i++) {
        int datasetPointId = (int) lineCells.GetConnectivityArray().GetTuple1(i);

        Coord3d c = new Coord3d(points.GetData().GetTuple3(datasetPointId));

        line.add(c);
      }

      drawableContourLines.add(line);

      if (debug) {
        System.out.println();
      }

      // -------------------------------------
      // Compute the point id to hold the label (Mid point)
      int samplePtIdx = (int) (cell.GetNumberOfIds() / 2);
      int midPointId = (int) cell.GetId(samplePtIdx);
      double[] midPoint = new double[3];
      points.GetPoint(midPointId, midPoint);

      if (debug) {
        System.out
            .println("\tmidPoint is " + midPointId + " with coordinate " + ", " + midPoint[0] + ", "
                + midPoint[1] + ", " + midPoint[2] + " and value " + scalars.GetTuple1(midPointId));
      }

      DrawableTextBitmap text = new DrawableTextBitmap("" + scalars.GetTuple1(midPointId),
          new Coord3d(midPoint), Color.WHITE);

      drawableContourLabels.add(text);
    }
    return lineCells;
  }

  public int getNumberOfContourLines() {
    return (int) contourStripper.GetOutput().GetNumberOfLines();
  }

  public List<DrawableTextWrapper> getDrawableContourLabels() {
    return drawableContourLabels;
  }

  public List<DrawableTextWrapper> getDrawableContourLabels(Color color) {
    for(DrawableTextWrapper text: drawableContourLabels) {
      text.setColor(color);
    }
    return drawableContourLabels;
  }

  public List<LineStrip> getDrawableContourLines() {
    return drawableContourLines;
  }

  public List<LineStrip> getDrawableContourLines(Color color) {
    return getDrawableContourLines(color, -1);
  }

  public List<LineStrip> getDrawableContourLines(int lineWidth) {
    return getDrawableContourLines(null, lineWidth);
  }

  public List<LineStrip> getDrawableContourLines(Color color, int lineWidth) {
    for (LineStrip line : drawableContourLines) {
      if(color!=null)
        line.setColor(color);
      if(lineWidth>0)
        line.setWidth(lineWidth);
    }
    return drawableContourLines;
  }

}
