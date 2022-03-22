package windows.opengl32.classloader;

public class OpenGLMesaLoader_macOS {

  static String path =
      "/Users/martin/Dev/jzy3d/external/osmesa/lib/";
  static String lib = path + "/libGL.dylib";

  static {
    System.load(lib);
  }
  
  public void finalize() {
    System.out.println(this.getClass().getSimpleName() + " garbage collected");
  }
}
