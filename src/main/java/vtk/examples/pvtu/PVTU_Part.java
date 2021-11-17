package vtk.examples.pvtu;

import vtk.VTKUtils;

/**
 * Need VM arguments : -Djava.library.path=./lib/vtk-Darwin-x86_64
 * 
 * @author martin
 *
 */
public class PVTU_Part  {
  public static void main(String[] args) {
    VTKUtils.loadVtkNativeLibraries();

    String file = "./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080_1.vtu";
    String propertyName = "enthalpy";

    
    PVTU.readAndShow(file, propertyName);
  }
}
