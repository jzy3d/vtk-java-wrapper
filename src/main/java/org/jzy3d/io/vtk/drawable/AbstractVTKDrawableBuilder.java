package org.jzy3d.io.vtk.drawable;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.colors.colormaps.IColorMap;
import org.jzy3d.maths.Array;
import org.jzy3d.maths.Range;
import vtk.VTKGeometry;
import vtk.VTKUtils;
import vtk.vtkCellArray;
import vtk.vtkDataSet;
import vtk.vtkPolyData;
import vtk.vtkUnstructuredGrid;

public class AbstractVTKDrawableBuilder implements IDrawableBuilder{
  protected static final int vtkHexNear = 0;
  protected static final int vtkHexFar = 1;
  protected static final int vtkHexSouth = 2;
  protected static final int vtkHexNorth = 3;
  protected static final int vtkHexRight = 4;
  protected static final int vtkHexLeft = 5;

  protected vtkDataSet dataset;
  protected vtkCellArray cells;
  protected Range propertyRange;
  
  protected IColorMap colormap = new ColorMapRainbow();
  protected Color color = null;
  
  protected Color wireColor = null;
  protected boolean wireDisplayed = false;
  protected boolean reflectLight = false;
  protected boolean faceDisplayed = false;

  public AbstractVTKDrawableBuilder(vtkDataSet dataset) {
    this.dataset = dataset;
    
    if(dataset instanceof vtkUnstructuredGrid) {
      this.cells = ((vtkUnstructuredGrid)dataset).GetCells();
    }
    else if(dataset instanceof vtkPolyData) {
      this.cells = ((vtkPolyData)dataset).GetPolys();
    }
  }

  
  @Override
  public String[] getPropertyNames() {
    return VTKUtils.getArrayNames(dataset.GetPointData());
  }


  /**
   * Return the range of values of the property that was chosen at the previous call to
   * {@link #makePolygon(float[], float, float, int, int)}
   */
  @Override
  public Range getPropertyRange() {
    return propertyRange;
  }


  /**
   * This array is usefull, because it combines CONNECTIVITY and OFFSET as follow :
   * 
   * <code>
   * |cell1Size|pointId1|...|pointIdN| cell2Size|pointId1|...|pointIdN| ...
   * </code>
   * 
   * It may be used for debugging OR as an alternative way of building polygons out of cells.
   */
  public void printCellData() {
    int[] types = VTKUtils.toIntArray(cells.GetData(), (int)cells.GetNumberOfCells());
    Array.print(types);
  }
  
  protected void debugCurrentCell(int cellId, int cellType, int cellStart, int cellStop) {
    System.out.print(
        "Cell " + cellId + " | " + VTKGeometry.name(cellType) + " | " + cellStart + "->" + cellStop + " | ");
  }

  /** Return a color for this value, either from colormap is defined, or flat global color if defined, or gray if none defined.*/
  protected Color getValueColor(float value) {
    if(colormap!=null) {
      return colormap.getColor(0, 0, value, propertyRange.getMin(), propertyRange.getMax());
    }
    else if(this.color!=null){
      return this.color;
    }
    else {
      return Color.GRAY.clone();
    }
  }



  //////////////////////////////////////////////
  //
  //
  //
  //////////////////////////////////////////////

  @Override
  public Color getWireframeColor() {
    return wireColor;
  }

  @Override
  public void setWireframeColor(Color wireframeColor) {
    this.wireColor = wireframeColor;
  }

  @Override
  public boolean isWireframeDisplayed() {
    return wireDisplayed;
  }

  @Override
  public void setWireframeDisplayed(boolean wireframeDisplayed) {
    this.wireDisplayed = wireframeDisplayed;
  }
  
  @Override
  public boolean isFaceDisplayed() {
    return faceDisplayed;
  }

  @Override
  public void setFaceDisplayed(boolean faceDisplayed) {
    this.faceDisplayed = faceDisplayed;
  }

  @Override
  public IColorMap getColormap() {
    return colormap;
  }

  @Override
  public void setColormap(IColorMap colormap) {
    this.colormap = colormap;
  }
  
  @Override
  public Color getColor() {
    return color;
  }

  @Override
  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public boolean isReflectLight() {
    return reflectLight;
  }

  @Override
  public void setReflectLight(boolean reflectLight) {
    this.reflectLight = reflectLight;
  }

  public vtkDataSet getVTKDataset() {
    return dataset;
  }

  public vtkCellArray getVTKCells() {
    return cells;
  }
  
  
}
