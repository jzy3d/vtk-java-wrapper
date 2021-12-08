package org.jzy3d.io.vtk.drawable;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Array;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.volume.Texture3D;
import com.jogamp.opengl.util.GLBuffers;
import vtk.VTKGeometry;
import vtk.VTKUtils;
import vtk.vtkCell;
import vtk.vtkDataArray;
import vtk.vtkImageData;
import vtk.vtkPointData;
import vtk.vtkPoints;

public class VTKVolumeBuilder {
  protected vtkImageData dataset;
  
  public VTKVolumeBuilder(vtkImageData imageData) {
    this.dataset = imageData;
  }
  
  public Texture3D makeVolume(String property) {
    vtkPointData pointData = dataset.GetPointData();

    vtkDataArray propertyArray = pointData.GetArray(property);
    if (propertyArray == null) {
      throw new IllegalArgumentException("Property '" + property + "' not found. Use one of : "
          + String.join(" ", VTKUtils.getArrayNames(pointData)));
    }



    int xx = dataset.GetDimensions()[0];
    int yy = dataset.GetDimensions()[1];
    int zz = dataset.GetDimensions()[2];


    int sizeofFloat = 4;

    ByteBuffer buffer = GLBuffers.newDirectByteBuffer(xx * yy * zz * sizeofFloat);

    Set<Long> cellId = new HashSet<>();

    float minV = Float.MAX_VALUE;
    float maxV = -Float.MAX_VALUE;

    // X axis
    for (int x = 0; x < xx; x++) {

      // Y axis
      for (int y = 0; y < yy; y++) {

        // Z axis
        for (int z = 0; z < zz; z++) {

          long id = dataset.GetScalarIndex(z, y, x);

          if (cellId.contains(id)) {
            throw new RuntimeException(id + " already exists");
          } else {
            cellId.add(id);
          }

          // vtkCell cell = dataset.GetCell(id);

          float value = (float) propertyArray.GetTuple1(id);

          // System.out.println("Cell " + x+","+ y+","+ z+" ("+ id + ") : " +
          // VTKGeometry.name(dataset.GetCellType(id)) + ", " + property + " = " + value);

          buffer.putFloat(value);

          if (value < minV)
            minV = value;
          if (value > maxV)
            maxV = value;

          /*
           * vtkPoints points = cell.GetPoints();
           * 
           * double[][] coords = VTKUtils.toDoubleMatrix3(points); for (int p = 0; p <
           * coords.length; p++) { Array.print("  Point " + j + " : ", coords[p]);
           * //System.out.println(coords[j] + "\t" + coords[j+1]+ "\t" + coords[j+2]); }
           */
        }
      }
    }

    double min = propertyArray.GetFiniteRange()[0];
    double max = propertyArray.GetFiniteRange()[1];

    System.out.println("Range  : " + min + ", " + max);
    System.out.println("Actual : " + minV + ", " + maxV);

    ColorMapper colorMapper =
        new ColorMapper(new ColorMapRainbow(), min, max, new Color(1, 1, 1, 1f));

    Texture3D volume =
        new Texture3D(buffer, new int[] {xx, yy, zz}, colorMapper, new BoundingBox3d(dataset.GetBounds()));

    return volume;
  }

  public static void iterateOverCells(vtkImageData dataset) {
    for (int i = 0; i < dataset.GetNumberOfCells(); i++) {
      vtkCell cell = dataset.GetCell(i);

      System.out.println("Cell " + i + " : " + VTKGeometry.name(dataset.GetCellType(i)));

      // cell.GetPointIds()
      vtkPoints points = cell.GetPoints();

      double[][] coords = VTKUtils.toDoubleMatrix3(points);
      for (int j = 0; j < coords.length; j++) {
        Array.print("  Point " + j + " : ", coords[j]);
        // System.out.println(coords[j] + "\t" + coords[j+1]+ "\t" + coords[j+2]);
      }

    }
  }

}
