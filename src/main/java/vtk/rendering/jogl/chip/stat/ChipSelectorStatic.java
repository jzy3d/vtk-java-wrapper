package vtk.rendering.jogl.chip.stat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import vtk.rendering.jogl.chip.Chip;

/**
 * 
 * Experimental DLL unloader.
 * 
 * https://web.archive.org/web/20140704120535/http://www.codethesis.com/blog/unload-java-jni-dll
 * https://web.archive.org/web/20131202084001/http://www.codeslices.net/snippets/simple-java-custom-class-loader-implementation
 * https://java2blog.com/invoke-constructor-using-reflection-java/
 * https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html
 * 
 */
public class ChipSelectorStatic extends ClassLoader {
  static ChipSelectorStatic loader;
  static Class<?> chipSelectorClass;
  static Constructor<?> chipSelectorConstructor;
  static Object chipSelectorInstance;
  static Method selectChipMethod;

  /**
   * Simple usage of the CustomClassLoader implementation
   * 
   * 
   * @param args
   * @throws ClassNotFoundException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   */
  public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {


    chipSelect(Chip.CPU);
    chipSelect(Chip.GPU);

  }

  /**
   * https://web.archive.org/web/20140704120535/http://www.codethesis.com/blog/unload-java-jni-dll
   * https://web.archive.org/web/20131202084001/http://www.codeslices.net/snippets/simple-java-custom-class-loader-implementation
   * https://java2blog.com/invoke-constructor-using-reflection-java/
   * https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html
   * https://sudonull.com/post/76671-How-to-unload-dll-from-Java-machine
   * 
   * @param chip
   * @throws ClassNotFoundException
   * @throws NoSuchMethodException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static void chipSelect(Chip chip) throws ClassNotFoundException, NoSuchMethodException,
      InstantiationException, IllegalAccessException, InvocationTargetException {
    loader = new ChipSelectorStatic();

    // Get class instance
    chipSelectorClass = loader.findClass("vtk.rendering.jogl.chip.ChipSelector");
    chipSelectorConstructor = chipSelectorClass.getConstructor();// type);
    chipSelectorInstance = chipSelectorConstructor.newInstance();

    // Get method
    Class<?>[] type = {Chip.class};
    selectChipMethod = chipSelectorClass.getMethod("use", type);

    // Invoke method
    Object[] cpuObj = {chip};
    selectChipMethod.invoke(chipSelectorInstance, cpuObj);


  }

  public static void clean() {
    // Free everything to unload DLL
    selectChipMethod = null;
    // cpuObj = null;
    // type = null;
    chipSelectorInstance = null;
    chipSelectorConstructor = null;
    chipSelectorClass = null;

    // loader.dispose();
    loader = null;
    // System.gc();

    System.gc();
    System.runFinalization();
    System.gc();
    System.runFinalization();
  }


  /************************ CLASSLOADER PART ***************************/

  /**
   * The HashMap where the classes will be cached
   */
  private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

  @Override
  public String toString() {
    return ChipSelectorStatic.class.getName();
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {

    if (classes.containsKey(name)) {
      return classes.get(name);
    }

    byte[] classData;

    try {
      classData = loadClassData(name);
    } catch (IOException e) {
      throw new ClassNotFoundException("Class [" + name + "] could not be found", e);
    }

    Class<?> c = defineClass(name, classData, 0, classData.length);
    resolveClass(c);
    classes.put(name, c);

    return c;
  }

  /**
   * Load the class file into byte array
   * 
   * @param name The name of the class e.g. com.codeslices.test.TestClass}
   * @return The class file as byte array
   * @throws IOException
   */
  private byte[] loadClassData(String name) throws IOException {
    BufferedInputStream in = new BufferedInputStream(
        ClassLoader.getSystemResourceAsStream(name.replace(".", "/") + ".class"));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int i;

    while ((i = in.read()) != -1) {
      out.write(i);
    }

    in.close();
    byte[] classData = out.toByteArray();
    out.close();

    return classData;
  }

}
