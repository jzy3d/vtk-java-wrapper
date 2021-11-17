package vtk;

/**
 * IDs for VTK geometries. See image below in this doc for easier understanding.
 * 
 * <img src="./doc-files/TestLinearCellDemo.png"/>
 * 
 * @see https://raw.githubusercontent.com/Kitware/vtk-examples/gh-pages/src/Testing/Baseline/Cxx/GeometricObjects/TestLinearCellDemo.png
 * @see https://github.com/Kitware/VTK/blob/master/Common/DataModel/vtkCellType.h
 */
public class VTKGeometry {
  // Linear cells
  public static final int VTK_EMPTY_CELL = 0;
  public static final int VTK_VERTEX = 1;
  public static final int VTK_POLY_VERTEX = 2;
  public static final int VTK_LINE = 3;
  public static final int VTK_POLY_LINE = 4;
  public static final int VTK_TRIANGLE = 5;
  public static final int VTK_TRIANGLE_STRIP = 6;
  public static final int VTK_POLYGON = 7;
  public static final int VTK_PIXEL = 8;
  public static final int VTK_QUAD = 9;
  public static final int VTK_TETRA = 10;
  public static final int VTK_VOXEL = 11;
  public static final int VTK_HEXAHEDRON = 12;
  public static final int VTK_WEDGE = 13;
  public static final int VTK_PYRAMID = 14;
  public static final int VTK_PENTAGONAL_PRISM = 15;
  public static final int VTK_HEXAGONAL_PRISM = 16;

  // Quadratic; isoparametric cells
  public static final int VTK_QUADRATIC_EDGE = 21;
  public static final int VTK_QUADRATIC_TRIANGLE = 22;
  public static final int VTK_QUADRATIC_QUAD = 23;
  public static final int VTK_QUADRATIC_POLYGON = 36;
  public static final int VTK_QUADRATIC_TETRA = 24;
  public static final int VTK_QUADRATIC_HEXAHEDRON = 25;
  public static final int VTK_QUADRATIC_WEDGE = 26;
  public static final int VTK_QUADRATIC_PYRAMID = 27;
  public static final int VTK_BIQUADRATIC_QUAD = 28;
  public static final int VTK_TRIQUADRATIC_HEXAHEDRON = 29;
  public static final int VTK_QUADRATIC_LINEAR_QUAD = 30;
  public static final int VTK_QUADRATIC_LINEAR_WEDGE = 31;
  public static final int VTK_BIQUADRATIC_QUADRATIC_WEDGE = 32;
  public static final int VTK_BIQUADRATIC_QUADRATIC_HEXAHEDRON = 33;
  public static final int VTK_BIQUADRATIC_TRIANGLE = 34;

  // Cubic; isoparametric cell
  public static final int VTK_CUBIC_LINE = 35;

  // Special class of cells formed by convex group of points
  public static final int VTK_CONVEX_POINT_SET = 41;

  // Polyhedron cell (consisting of polygonal faces)
  public static final int VTK_POLYHEDRON = 42;

  // Higher order cells in parametric form
  public static final int VTK_PARAMETRIC_CURVE = 51;
  public static final int VTK_PARAMETRIC_SURFACE = 52;
  public static final int VTK_PARAMETRIC_TRI_SURFACE = 53;
  public static final int VTK_PARAMETRIC_QUAD_SURFACE = 54;
  public static final int VTK_PARAMETRIC_TETRA_REGION = 55;
  public static final int VTK_PARAMETRIC_HEX_REGION = 56;

  // Higher order cells
  public static final int VTK_HIGHER_ORDER_EDGE = 60;
  public static final int VTK_HIGHER_ORDER_TRIANGLE = 61;
  public static final int VTK_HIGHER_ORDER_QUAD = 62;
  public static final int VTK_HIGHER_ORDER_POLYGON = 63;
  public static final int VTK_HIGHER_ORDER_TETRAHEDRON = 64;
  public static final int VTK_HIGHER_ORDER_WEDGE = 65;
  public static final int VTK_HIGHER_ORDER_PYRAMID = 66;
  public static final int VTK_HIGHER_ORDER_HEXAHEDRON = 67;

  // Arbitrary order Lagrange elements (formulated separated from generic higher order cells)
  public static final int VTK_LAGRANGE_CURVE = 68;
  public static final int VTK_LAGRANGE_TRIANGLE = 69;
  public static final int VTK_LAGRANGE_QUADRILATERAL = 70;
  public static final int VTK_LAGRANGE_TETRAHEDRON = 71;
  public static final int VTK_LAGRANGE_HEXAHEDRON = 72;
  public static final int VTK_LAGRANGE_WEDGE = 73;
  public static final int VTK_LAGRANGE_PYRAMID = 74;

  // Arbitrary order Bezier elements (formulated separated from generic higher order cells)
  public static final int VTK_BEZIER_CURVE = 75;
  public static final int VTK_BEZIER_TRIANGLE = 76;
  public static final int VTK_BEZIER_QUADRILATERAL = 77;
  public static final int VTK_BEZIER_TETRAHEDRON = 78;
  public static final int VTK_BEZIER_HEXAHEDRON = 79;
  public static final int VTK_BEZIER_WEDGE = 80;
  public static final int VTK_BEZIER_PYRAMID = 81;

  public static final int VTK_NUMBER_OF_CELL_TYPES = 82;
  
  
  public static String name(int id) {
    switch(id) {
      case VTK_EMPTY_CELL: return "VTK_EMPTY_CELL";
      case VTK_VERTEX: return "VTK_VERTEX";
      case VTK_POLY_VERTEX: return "VTK_POLY_VERTEX";
      case VTK_LINE: return "VTK_LINE";
      case VTK_POLY_LINE: return "VTK_POLY_LINE";
      case VTK_TRIANGLE: return "VTK_TRIANGLE";
      case VTK_TRIANGLE_STRIP: return "VTK_TRIANGLE_STRIP";
      case VTK_POLYGON: return "VTK_POLYGON";
      case VTK_PIXEL: return "VTK_PIXEL";
      case VTK_QUAD: return "VTK_QUAD";
      case VTK_TETRA: return "VTK_TETRA";
      case VTK_VOXEL: return "VTK_VOXEL";
      case VTK_HEXAHEDRON: return "VTK_HEXAHEDRON";
      case VTK_WEDGE: return "VTK_WEDGE";
      case VTK_PYRAMID: return "VTK_PYRAMID";
      case VTK_PENTAGONAL_PRISM: return "VTK_PENTAGONAL_PRISM";
      case VTK_HEXAGONAL_PRISM: return "VTK_HEXAGONAL_PRISM";
    }
    return "please code the name for " + id;
  }
}
