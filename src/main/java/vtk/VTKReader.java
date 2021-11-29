package vtk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jzy3d.maths.Array;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.TicToc;

/** A helper class that will select the appropriate reader according to the VTK file extension. */
public class VTKReader {
  public static vtkAlgorithm getReader(String filename) {
    if(!new File(filename).exists())
      throw new RuntimeException(filename + " does not exists");

    if (filename.endsWith(".vtu")) {
      vtkXMLUnstructuredGridReader reader = new vtkXMLUnstructuredGridReader();
      reader.SetFileName(filename);
      return reader;
    } else if (filename.endsWith(".pvtu")) {
      vtkXMLPUnstructuredGridReader reader = new vtkXMLPUnstructuredGridReader();
      reader.SetFileName(filename);
      return reader;
    } else if (filename.contains(".e")) {
      vtkExodusIIReader reader = new vtkExodusIIReader();
      reader.SetFileName(filename);
      return reader;
    } else {
      throw new IllegalArgumentException("Only support VTU and PVTU and EXODUS extensions");
    }
  }

  public static vtkUnstructuredGrid getReaderOutput(String filename, int timestep) {
    if(!new File(filename).exists())
      throw new RuntimeException(filename + " does not exists");
    
    vtkAlgorithm reader = getReader(filename);
//    if (reader instanceof vtkExodusIIReader) {
//      return read_exodusii_grid((vtkExodusIIReader) reader, timestep);
//    }

    return getOutput(reader);
  }

  public static vtkUnstructuredGrid getReaderOutput(String filename) {
    vtkAlgorithm reader = getReader(filename);

    return getOutput(reader);
  }

  public static vtkUnstructuredGrid getOutput(vtkAlgorithm reader) {
    TicToc clock = new TicToc();
    clock.tic();
    reader.Update();
    clock.toc();

    if (reader instanceof vtkXMLUnstructuredGridReader) {
      return ((vtkXMLUnstructuredGridReader) reader).GetOutput();
    } else if (reader instanceof vtkXMLPUnstructuredGridReader) {
      return ((vtkXMLPUnstructuredGridReader) reader).GetOutput();
//    } else if (reader instanceof vtkExodusIIReader) {
//      return read_exodusii_grid((vtkExodusIIReader) reader, 0);
    } else {
      throw new IllegalArgumentException("Unsupported reader");
    }
  }

  public static vtkUnstructuredGrid[] read_exodusii_grid(vtkExodusIIReader reader, int[] blocks, int timestep, String propertyName) {
    // Fetch metadata.
    reader.UpdateInformation();

    // Set time step to read.
    reader.SetTimeStep(timestep);

    // Read the file.
    reader.Update();

    // read in the element blocks, make a surface filter for each, push_back into vector:
    for (int z = 0; z < reader.GetNumberOfElementBlockArrays(); z++) {
      String bname = reader.GetElementBlockArrayName(z);
      int status = blocks == null ? 1 : blocks[z];
      reader.SetElementBlockArrayStatus(bname, status);
      System.out.println("Element block " + z + "; Name = '" + bname + "'; status=" + status);
    }

    boolean found = false;
    for(int i=0; i<reader.GetNumberOfPointResultArrays(); i++) {
      if(propertyName.equals(reader.GetPointResultArrayName(i))) {
        reader.SetPointResultArrayStatus(propertyName, 1);
        found = true;
        System.out.println("Selected POINT array " + propertyName);
      }
    }
    if(!found)
      for(int i=0; i<reader.GetNumberOfElementResultArrays(); i++) {
        if(propertyName.equals(reader.GetElementResultArrayName(i))) {
          reader.SetElementResultArrayStatus(propertyName, 1);
          found = true;
          System.out.println("Selected ELEMENT array " + propertyName);
        }
      }
    if(!found)
      for(int i=0; i<reader.GetNumberOfGlobalResultArrays(); i++) {
        if(propertyName.equals(reader.GetGlobalResultArrayName(i))) {
          reader.SetGlobalResultArrayStatus(propertyName, 1);
          found = true;
          System.out.println("Selected GLOBAL array " + propertyName);
        }
      }

    if(!found) {
      throw new IllegalArgumentException("Can not load property: '"+ propertyName+"'");
    }

    vtkMultiBlockDataSet out = reader.GetOutput();

    // Loop through the blocks and search for a vtkUnstructuredGrid.
    // In Exodus, different element types are stored different meshes, with
    // point information possibly duplicated.
    List<vtkUnstructuredGrid> vtk_meshs = new ArrayList<>();

    for(int i=0; i<out.GetNumberOfBlocks(); i++) {
      vtkMultiBlockDataSet block = (vtkMultiBlockDataSet) out.GetBlock(i);

      for (int j = 0; j < block.GetNumberOfBlocks(); j++) {
        vtkDataObject sub_block = block.GetBlock(j);

        if(sub_block != null) {
          if (sub_block.IsA("vtkUnstructuredGrid") == 1)
            vtk_meshs.add((vtkUnstructuredGrid) sub_block);
        }
      }
    }
    if (vtk_meshs.isEmpty())
      throw new RuntimeException("No 'vtkUnstructuredGrid' found!");

    return vtk_meshs.toArray(new vtkUnstructuredGrid[vtk_meshs.size()]);
  }

  public static vtkAlgorithmOutput getReaderOutputPort(String filename) {
    vtkAlgorithm reader = getReader(filename);

    return getOutputPort(reader);
  }

  public static vtkAlgorithmOutput getOutputPort(vtkAlgorithm reader) {
    TicToc clock = new TicToc();
    clock.tic();
    reader.Update();
    clock.toc();

    if (reader instanceof vtkXMLUnstructuredGridReader) {
      return ((vtkXMLUnstructuredGridReader) reader).GetOutputPort();
    } else if (reader instanceof vtkXMLPUnstructuredGridReader) {
      return ((vtkXMLPUnstructuredGridReader) reader).GetOutputPort();
    } else if (reader instanceof vtkExodusIIReader) {
      return ((vtkExodusIIReader) reader).GetOutputPort();
    } else {
      throw new IllegalArgumentException("Unsupported reader");
    }
  }

  /////////////////////////////////////////////////////
  //
  // HELPERS TO CONVERT VTK DATASTRUCTURES TO JZY3D
  //
  /////////////////////////////////////////////////////

  public static Coord3d[] toCoord3d(vtkPoints points) {

    Coord3d[] coordinates = new Coord3d[(int) points.GetNumberOfPoints()];

    vtkDataArray pointsArray = points.GetData();

    for (int i = 0; i < coordinates.length; i++) {
      double[] point = pointsArray.GetTuple3(i); // 3 dimensions
      coordinates[i] = new Coord3d(point[0], point[1], point[2]);
    }

    return coordinates;
  }

  public static double[] toCoordDoubleArray(vtkDataArray array, int size) {
    double[] outputArray = new double[size];

    for (int i = 0; i < outputArray.length; i++) {
      outputArray[i] = array.GetTuple1(i);
    }
    return outputArray;
  }

  public static int dimensions = 3;

  public static double[] toCoordDoubleArray(vtkPoints points) {
    return ((vtkDoubleArray)points.GetData()).GetJavaArray();
    /*double[] outputArray = new double[points.GetNumberOfPoints() * dimensions];
    vtkDataArray pointsArray = points.GetData();
    for (int i = 0; i < points.GetNumberOfPoints(); i++) {
      double[] point = pointsArray.GetTuple3(i); // 3 dimensions
      System.arraycopy(point, 0, outputArray, i * dimensions, dimensions);
    }
    return outputArray;*/
  }

  public static float[] toCoordFloatArray(vtkPoints points) {
    // If input is made of float
    if(points.GetData() instanceof vtkFloatArray) {
      //System.out.println("LOADING FLOAT  ");
      return ((vtkFloatArray)points.GetData()).GetJavaArray();
    }
    else if(points.GetData() instanceof vtkDoubleArray){
      //System.out.println("LOADING DOUBLE  ");
      vtkDoubleArray pointsArray = (vtkDoubleArray)points.GetData();
      return Array.cloneFloat(pointsArray.GetJavaArray());
    }
    else {
      vtkDataArray pointsArray = points.GetData();

      float[] outputArray = new float[(int) (points.GetNumberOfPoints() * dimensions)];

      for (int i = 0; i < points.GetNumberOfPoints(); i++) {
        float[] point = Array.cloneFloat(pointsArray.GetTuple3(i));
        System.arraycopy(point, 0, outputArray, i * dimensions, dimensions);
      }
      return outputArray;
    }


  }


  /**
   * Mainly for fetching normals
   *
   * @param points
   * @return
   */
  public static Coord3d[] toCoord3d(vtkDataArray points) {
    Coord3d[] coordinates = new Coord3d[(int) points.GetNumberOfTuples()];

    for (int i = 0; i < coordinates.length; i++) {
      double[] point = points.GetTuple3(i); // 3 dimensions
      coordinates[i] = new Coord3d(point[0], point[1], point[2]);
    }

    return coordinates;
  }

  public static double[] toCoordDoubleArray(vtkDataArray points) {
    double[] outputArray = new double[(int) (points.GetNumberOfTuples() * dimensions)];

    for (int i = 0; i < points.GetNumberOfTuples(); i++) {
      double[] point = points.GetTuple3(i); // 3 dimensions
      System.arraycopy(point, 0, outputArray, i * dimensions, dimensions);
    }

    return outputArray;
  }

  public static float[] toCoordFloatArray(vtkDataArray points) {
    if(points instanceof vtkFloatArray) {
      return ((vtkFloatArray)points).GetJavaArray();
    }
    else {
      float[] outputArray = new float[(int) (points.GetNumberOfTuples() * dimensions)];

      for (int i = 0; i < points.GetNumberOfTuples(); i++) {
        double[] point = points.GetTuple3(i); // 3 dimensions

        outputArray[i * dimensions + 0] = (float)point[0];
        outputArray[i * dimensions + 1] = (float)point[1];
        outputArray[i * dimensions + 2] = (float)point[2];
      }

      return outputArray;
    }
  }
}
