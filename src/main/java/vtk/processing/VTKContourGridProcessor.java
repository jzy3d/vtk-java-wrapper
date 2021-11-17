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
import vtk.vtkContour3DLinearGrid;
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
public class VTKContourGridProcessor {
  protected vtkContour3DLinearGrid contourGrid;
  protected vtkStripper contourStripper;

  protected List<DrawableTextWrapper> drawableContourLabels = new ArrayList<>();
  protected List<LineStrip> drawableContourLines = new ArrayList<>();

  protected boolean debug = false;


  public VTKContourGridProcessor(vtkAlgorithmOutput source, int n, double from, double to) {
    
    // -----------------------------------------------------------
    // Contour processing

    contourGrid = new vtkContour3DLinearGrid();
    contourGrid.SetInputConnection(source);
    contourGrid.MergePointsOn();
    contourGrid.ComputeNormalsOn();
    
    contourGrid.GenerateValues(n, from, to);

    contourStripper = new vtkStripper();
    contourStripper.SetInputConnection(contourGrid.GetOutputPort());
    contourStripper.Update();

    vtkPoints points = contourStripper.GetOutput().GetPoints();
    vtkCellArray cells = contourStripper.GetOutput().GetLines();
    vtkDataArray scalars = contourStripper.GetOutput().GetPointData().GetScalars();


    // -----------------------------------------------------------
    // Get contour data


    readResultAndBuildDrawables(points, cells, scalars);
  }

  protected void readResultAndBuildDrawables(vtkPoints points, vtkCellArray cells,
      vtkDataArray scalars) {
    
    drawableContourLabels = new ArrayList<>();
    drawableContourLines = new ArrayList<>();


    int pointThreshold = 0;

    vtkDataSet dataset = contourStripper.GetOutput();
    vtkCellArrayIterator cellIter = cells.NewIterator();

    for (cellIter.GoToFirstCell(); !cellIter.IsDoneWithTraversal(); cellIter.GoToNextCell()) {

      vtkIdList cell = cellIter.GetCurrentCell();
      if (cell.GetNumberOfIds() < pointThreshold) {
        continue;
      }

      // --------------------------------------
      // START : MY CODE
      int cellId = (int) cellIter.GetCurrentCellId();
      int cellType = dataset.GetCellType(cellId);
      int cellStartPointId = (int) cells.GetOffsetsArray().GetTuple1(cellId);
      int cellStopPointId = (int) cells.GetOffsetsArray().GetTuple1(cellId + 1);

      if (debug)
        System.out.println(VTKGeometry.name(cellType) + " " + cellId);


      LineStrip line = new LineStrip(Color.BLACK);
      line.setWidth(3);

      for (int i = cellStartPointId; i < cellStopPointId; i++) {
        int datasetPointId = (int) cells.GetConnectivityArray().GetTuple1(i);

        Coord3d c = new Coord3d(points.GetData().GetTuple3(datasetPointId));

        line.add(c);
      }

      drawableContourLines.add(line);

      if (debug) {
        System.out.println();
      }

      // END : MY CODE
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
    for (LineStrip line : drawableContourLines) {
      line.setColor(color);
    }
    return drawableContourLines;
  }

}
