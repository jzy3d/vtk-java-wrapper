package vtk;

public class GetVersion {
  public static void main(String[] args) {
    VTKUtils.loadVtkNativeLibraries();

    System.out.println("VTK framework version : " + new vtkVersion().GetVTKVersion());
  }

}
