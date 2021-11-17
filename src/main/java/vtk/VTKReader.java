package vtk;

import org.jzy3d.maths.Array;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.TicToc;

/** A helper class that will select the appropriate reader according to the VTK file extension. */
public class VTKReader {
  public static vtkAlgorithm getReader(String filename) {
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
    vtkAlgorithm reader = getReader(filename);
    if (reader instanceof vtkExodusIIReader) {
      return read_exodusii_grid((vtkExodusIIReader) reader, timestep);
    }

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
    } else if (reader instanceof vtkExodusIIReader) {
      return read_exodusii_grid((vtkExodusIIReader) reader, 0);
    } else {
      throw new IllegalArgumentException("Unsupported reader");
    }
  }

  private static vtkUnstructuredGrid read_exodusii_grid(vtkExodusIIReader reader, int timestep) {
    // Fetch metadata.
    reader.UpdateInformation();

    // Set time step to read.
    reader.SetTimeStep(timestep);

    // Make sure the point fields are read during Update().
    for (int k=0; k<reader.GetNumberOfPointResultArrays(); k++) {
      String arr_name = reader.GetPointResultArrayName(k);
      reader.SetPointResultArrayStatus(arr_name, 1);
    }

    // Make sure the element fields are read during Update().
    for (int k=0; k<reader.GetNumberOfElementResultArrays(); k++) {
      String arr_name = reader.GetElementResultArrayName(k);
      reader.SetElementResultArrayStatus(arr_name, 1);
    }

    // Make sure all global field data is read.
    for (int k=0; k<reader.GetNumberOfGlobalResultArrays(); k++) {
      String arr_name = reader.GetGlobalResultArrayName(k);
      reader.SetGlobalResultArrayStatus(arr_name, 1);
    }

    // Read the file.
    reader.Update();
    vtkMultiBlockDataSet out = reader.GetOutput();

    // Loop through the blocks and search for a vtkUnstructuredGrid.
    // In Exodus, different element types are stored different meshes, with
    // point information possibly duplicated.
    vtkUnstructuredGrid vtk_mesh = null;

    for(int i=0; i<out.GetNumberOfBlocks(); i++) {
      vtkMultiBlockDataSet block = (vtkMultiBlockDataSet) out.GetBlock(i);
      
      for (int j = 0; j < block.GetNumberOfBlocks(); j++) {
        vtkDataObject sub_block = block.GetBlock(j);
        
        if(sub_block != null) {
          if (vtk_mesh != null)
            throw new RuntimeException("More than one 'vtkUnstructuredGrid' found!");
          if (sub_block.IsA("vtkUnstructuredGrid") == 1)
            vtk_mesh = (vtkUnstructuredGrid) sub_block;
        }
      }
    }
    if (vtk_mesh == null)
      throw new RuntimeException("No 'vtkUnstructuredGrid' found!");

    // Cut off trailing '_' from array names.
//    for(int k=0; k<vtk_mesh.GetPointData().GetNumberOfArrays(); k++) {
//      array = vtk_mesh.GetPointData().GetArray(k);
//      array_name = array.GetName();
//      if array_name[-1] == '_':
//        array.SetName(array_name[0:-1])

    // time_values = reader.GetOutputInformation(0).Get(
    //     vtkStreamingDemandDrivenPipeline.TIME_STEPS()
    //     )

    return vtk_mesh;
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
