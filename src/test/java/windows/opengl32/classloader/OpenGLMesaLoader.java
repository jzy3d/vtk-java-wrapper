package windows.opengl32.classloader;

public class OpenGLMesaLoader {
  static String path =
      "C:\\Users\\Martin\\Dev\\jzy3d\\private\\vtk-java-wrapper\\lib\\9.1.0\\mesa-Windows-x86_64";
  static String lib = path + "/opengl32.dll";// "\\opengl32.dll";

  static {
    System.load(lib);
    // System.loadLibrary(path);
  }

  //public native void print();

  public void finalize() {
    System.out.println(this.getClass().getSimpleName() + " garbage collected");
  }
}
