package vtk;

public class VTKUtils {
  
  
  
  /** Load VTK library and print which library was not properly loaded */
  public static void loadVtkNativeLibraries() {
    System.out.println("Reminder VM args : -Djava.library.path=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/vtk-Darwin-x86_64");
    if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
      for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
        if (!lib.IsLoaded()) {
          System.out.println(lib.GetLibraryName() + " not loaded");
        }
      }
    }
    vtkNativeLibrary.DisableOutputWindow(null);
  }
  
  public static double[] toDoubleArray(vtkDataArray array, int size) {
    double[] outputArray = new double[size];
    
    for (int i = 0; i < outputArray.length; i++) {
      outputArray[i] = array.GetTuple1(i);
    }
    return outputArray;
  }

  public static float[] toFloatArray(vtkDataArray array, int size) {
    float[] outputArray = new float[size];
    
    for (int i = 0; i < outputArray.length; i++) {
      outputArray[i] = (float)array.GetTuple1(i);
    }
    return outputArray;
  }

  
  public static long[] toLongArray(vtkDataArray array, int size) {
    long[] outputArray = new long[size];
    
    for (int i = 0; i < outputArray.length; i++) {
      
      outputArray[i] = (long)array.GetTuple1(i);
    }
    return outputArray;
  }
  
  public static int[] toIntArray(vtkDataArray array, int size) {
    int[] outputArray = new int[size];
    
    for (int i = 0; i < outputArray.length; i++) {
      outputArray[i] = (int)array.GetTuple1(i);
    }
    return outputArray;
  }
  
  public static double[][] toDoubleMatrix3(vtkPoints points) {
    double[][] coordinates = new double[(int) points.GetNumberOfPoints()][];
    
    vtkDataArray pointsArray = points.GetData();
    
    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] = pointsArray.GetTuple3(i); // 3 dimensions
    }
    
    return coordinates;
  }

  public static String[] getArrayNames(vtkPointData pointData) {
    String[] names = new String[pointData.GetNumberOfArrays()];
    
    for (int i = 0; i < names.length; i++) {
      names[i] = pointData.GetArrayName(i);
    }

    return names;
  }
  
  public static String[] getArrayNames(vtkPolyData polyData) {
    return getArrayNames(polyData.GetPointData());
  }
  
  public static void printArrayNames(vtkPolyData polyData) {
    System.out.println(String.join(" ", VTKUtils.getArrayNames(polyData)));
  }

  public static void printArrayNames(vtkUnstructuredGrid ugrid) {
    System.out.println(String.join(" ", VTKUtils.getArrayNames(ugrid.GetPointData())));
  }

}
