package vtk;

import java.io.File;
import java.util.Map;
import org.jzy3d.os.OperatingSystem;
import vtk.rendering.jogl.Environment;
import vtk.rendering.jogl.OS;

public class VTKUtils {



  /** Load VTK library and print which library was not properly loaded */
  public static void loadVtkNativeLibraries() {

    String path = "/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib";
    String version = "9.1.0";
    String platform = "vtk-Darwin-arm64";
    
    File file = new File(path + "/" + version + "/" + platform);
    
    String absolutePath = file.getAbsolutePath();
    
    
    if(!loadVtkNativeLibraries(null)){
      System.out.println("Suggest -Djava.library.path=" + absolutePath);
      pathConfigurationReport();
    }
  }

  public static void pathConfigurationReport() {
    System.out.println("Current -Djava.library.path=" + System.getProperty("java.library.path"));
    
    OperatingSystem os = new OperatingSystem();


    if(os.isMac()) {
      printEnv("PATH", ":");
      printEnv("DYLD_LIBRARY_PATH", ":");
    }
    else if(os.isUnix()) {
      printEnv("LD_LIBRARY_PATH", ":");
    }
    else if(os.isWindows()) {
      printEnv("PATH", ";");
    }
    else {
      System.out.println("Undetected OS !!");
    }
    
    System.out.println(os);
  }
  
  protected static void appendSystemPathToJavaLibPath() {
    String javaLibPathKey = "java.library.path";
    String javaLibraryPath = System.getProperty(javaLibPathKey);

    System.err.println("=========================");
    System.err.println("Now " + javaLibPathKey + "=" + javaLibraryPath);
    System.err.println(" ");

    if(OS.isWindows()) {
      Environment env = new Environment();
      env.get("PATH");
      javaLibraryPath += File.pathSeparator + env.get("PATH");
      System.setProperty(javaLibPathKey, javaLibraryPath);
      System.err.println("Now " + javaLibPathKey + "=" + javaLibraryPath);
    }
    System.err.println("=========================");
  }

  /**
   * Load libraries by providing an explicit path where the library stand.
   * 
   * May be null, in that case, will load from PATH or java.library.path
   *  
   * To set lib path: -Djava.library.path=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-arm64");
   *
   * @param path
   */
  public static boolean loadVtkNativeLibraries(String path) {

    boolean success;
    
    if(path!=null) {
      success = LoadAllNativeLibraries(path);
    }
    else {
      success = vtkNativeLibrary.LoadAllNativeLibraries();
    }
    
    if (!success) {
      for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
        if (!lib.IsLoaded()) {
          System.out.println(lib.GetLibraryName() + " not loaded");
        }
      }
    }
    
    if(success)
      vtkNativeLibrary.DisableOutputWindow(null);
    
    return success;
  }

  public static boolean LoadAllNativeLibraries(String path) {
    boolean isEveryThingLoaded = true;
    for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
      try {
        LoadLibrary(lib, path);
        //lib.LoadLibrary();
      } catch (UnsatisfiedLinkError e) {
        isEveryThingLoaded = false;
        e.printStackTrace();
      }
    }

    return isEveryThingLoaded;
  }

  public static void LoadLibrary(vtkNativeLibrary lib, String path) throws UnsatisfiedLinkError {
    if (!lib.IsLoaded()) {
      File libPath = new File(path, "lib" + lib.GetLibraryName() + ".jnilib");
      if (libPath.exists()) {
        try {
          Runtime.getRuntime().load(libPath.getAbsolutePath());
          return;
        } catch (UnsatisfiedLinkError e) {
          System.err.println("Failed to load lib at " + libPath.getAbsolutePath());
          e.printStackTrace();
        }
      }
      else {
        throw new RuntimeException("Library file not found at " + libPath.getAbsolutePath());
      }
    }
    System.loadLibrary(lib.GetLibraryName());
  }
  
  public static void printEnv() {
    Map<String, String> env = System.getenv();

    for (Map.Entry<String, String> entry : env.entrySet()) {
        System.out.println(entry.getKey() + " : " + entry.getValue());
    }
  }
  
  public static void printEnv(String var) {
    printEnv(var, null);
  }

  public static void printEnv(String var, String splitWith) {
    Map<String, String> env = System.getenv();

    boolean found = false;
    
    for (Map.Entry<String, String> entry : env.entrySet()) {
      if(entry.getKey().toLowerCase().equals(var.toLowerCase())) {
        found = true;
        
        if(splitWith==null) {
          System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        else {
          System.out.println(entry.getKey() + " : ");

          String[] values = entry.getValue().split(splitWith);

          for(String value: values) {
            System.out.println(" " + value);
          }

        }

      }
    }
    
    if(!found) {
      System.out.println("Undefined environment variable " + var);
    }
  }


  ///////////////////////////

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
      outputArray[i] = (float) array.GetTuple1(i);
    }
    return outputArray;
  }


  public static long[] toLongArray(vtkDataArray array, int size) {
    long[] outputArray = new long[size];

    for (int i = 0; i < outputArray.length; i++) {

      outputArray[i] = (long) array.GetTuple1(i);
    }
    return outputArray;
  }

  public static int[] toIntArray(vtkDataArray array, int size) {
    int[] outputArray = new int[size];

    for (int i = 0; i < outputArray.length; i++) {
      outputArray[i] = (int) array.GetTuple1(i);
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
