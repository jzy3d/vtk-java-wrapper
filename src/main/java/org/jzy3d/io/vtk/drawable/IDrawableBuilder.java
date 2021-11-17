package org.jzy3d.io.vtk.drawable;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.colormaps.IColorMap;
import org.jzy3d.maths.Range;


public interface IDrawableBuilder {

  int HEXAHEDRON_POINTS = 8;
  int HEXAHEDRON_FACES = 6;
  int QUAD_POINTS = 4;

  String[] getPropertyNames();

  /**
   * Return the range of values of the property that was chosen at the previous call to
   * {@link #makePolygon(float[], float, float, int, int)}
   */
  Range getPropertyRange();

  Color getWireframeColor();

  void setWireframeColor(Color wireframeColor);

  boolean isWireframeDisplayed();

  void setWireframeDisplayed(boolean wireframeDisplayed);

  boolean isFaceDisplayed();

  void setFaceDisplayed(boolean faceDisplayed);
  
  IColorMap getColormap();

  void setColormap(IColorMap colormap);
  
  Color getColor();

  void setColor(Color color);

  boolean isReflectLight();

  void setReflectLight(boolean reflectLight);

}
