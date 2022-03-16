package windows.opengl32.classloader;

public class OpenGLWindowsLoader {
  static String path = "C:\\Windows\\System32";
  static String lib = path + "\\opengl32.dll";

  public OpenGLWindowsLoader() {}

  static {
    System.load(lib);
    // System.loadLibrary(path);
  }

  public native void print();

  public void finalize() {
    System.out.println(this.getClass().getSimpleName() + " garbage collected");
  }
}
