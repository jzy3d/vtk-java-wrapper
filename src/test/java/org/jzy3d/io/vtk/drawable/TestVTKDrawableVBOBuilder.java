package org.jzy3d.io.vtk.drawable;

import org.junit.BeforeClass;
import org.junit.Test;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.ChartFactory;
import org.jzy3d.io.vtk.drawable.VTKDrawableVBOBuilder.GeometryMode;
import org.jzy3d.io.vtk.drawable.VTKDrawableVBOBuilder.VerticeMode;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO2;
import junit.framework.Assert;
import vtk.VTKGeometry;
import vtk.VTKReader;
import vtk.VTKUtils;
import vtk.vtkAlgorithm;
import vtk.vtkGeometryFilter;
import vtk.vtkPolyData;
import vtk.vtkUnstructuredGrid;

/**
 * These tests will have the classpath to VTK native automatically added by Maven Surefire Plugin
 * when run with <code>mvn test</code>.
 * 
 * However, if you run them from an IDE, you may need to configure the Run Configuration so that VM
 * arguments are <code>
 * -Djava.library.path=./lib/vtk-Darwin-x86_64
 * </code>
 * 
 * @author Martin Pernollet
 */
public class TestVTKDrawableVBOBuilder {
  int HEXAHEDRON_FACES = 6;
  int QUAD_POINTS = 4;

  @BeforeClass
  public static void load() {
    VTKUtils.loadVtkNativeLibraries();
  }
  
  ///////////////////////////////////////////////////////////////////
  //
  //                            SHARED
  //
  ///////////////////////////////////////////////////////////////////


  @Test
  public void givenSharedVerticeHexahedron() {
    ChartFactory f = new AWTChartFactory();
    f.getPainterFactory().setOffscreen(500, 500);
    Chart chart = f.newChart();

    int EXPECT_CELLS = 5778;
    int EXPECT_POINTS = 9114;

    // --------------------------------------------
    // Given a file with hexahedrons inside
    
    vtkUnstructuredGrid grid = VTKReader
        .getReaderOutput("./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080.pvtu");


    Assert.assertEquals(EXPECT_CELLS, grid.GetNumberOfCells());
    Assert.assertEquals(EXPECT_POINTS, grid.GetNumberOfPoints());

    // --------------------------------------------
    // When load, make and add to chart
    
    VTKDrawableVBOBuilder builder = new VTKDrawableVBOBuilder(grid, GeometryMode.MULTI_GEOMETRY,
        VerticeMode.SHARED, VTKGeometry.VTK_HEXAHEDRON);

    DrawableVBO2 vbo = builder.makePolygons("enthalpy");

    chart.add(vbo);

    
    // --------------------------------------------
    // Then buffers have the appropriate size
    
    Assert.assertEquals(EXPECT_POINTS * 3, vbo.getVertices().capacity());
    Assert.assertEquals(EXPECT_POINTS * 3, vbo.getNormals().capacity());
    Assert.assertEquals(EXPECT_POINTS * 3, vbo.getColors().capacity());

    // input hexahedron are loaded as 6 quads in the VBO
    Assert.assertEquals(EXPECT_CELLS * HEXAHEDRON_FACES, vbo.getElementsIndices().capacity()); 
    Assert.assertEquals(EXPECT_CELLS * HEXAHEDRON_FACES, vbo.getElementsCount().capacity());
    Assert.assertEquals(QUAD_POINTS, vbo.getElementsCount().get(0)); 

    // these are not used
    Assert.assertNull(vbo.getElements());
    Assert.assertNull(vbo.getElementsLength());
    Assert.assertNull(vbo.getElementsStarts());

    
    // --------------------------------------------
    // When load another property, Then size is correct

    float[] colors = builder.getPropertyColor("marker1");

    Assert.assertEquals(EXPECT_POINTS * 3, colors.length);
    
  }
  
  @Test
  public void givenSharedVerticeQuads() {
    ChartFactory f = new AWTChartFactory();
    f.getPainterFactory().setOffscreen(500, 500);
    Chart chart = f.newChart();

    int EXPECT_CELLS = 7160;
    int EXPECT_POINTS = 9114;
    int EXPECT_POINTS_POST_FILTER = 6197;
    

    // --------------------------------------------
    // Given a file with hexahedrons inside ...
    
    vtkAlgorithm reader = VTKReader
        .getReader("./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080.pvtu");

    
    // ... converted to quads
    
    vtkGeometryFilter quadConversion = new vtkGeometryFilter();
    quadConversion.SetInputConnection(reader.GetOutputPort());
    quadConversion.Update();

    vtkPolyData quads = quadConversion.GetOutput();



    Assert.assertEquals(EXPECT_CELLS, quads.GetNumberOfCells());
    Assert.assertEquals(EXPECT_POINTS_POST_FILTER, quads.GetNumberOfPoints());

    // --------------------------------------------
    // When load, make and add to chart
    
    VTKDrawableVBOBuilder builder = new VTKDrawableVBOBuilder(quads, GeometryMode.MULTI_GEOMETRY,
        VerticeMode.SHARED, VTKGeometry.VTK_QUAD);

    // --------------------------------------------
    // Then buffers have the appropriate size
    
    DrawableVBO2 vbo = builder.makePolygons("enthalpy");

    chart.add(vbo);
    Assert.assertEquals(EXPECT_POINTS_POST_FILTER * 3, vbo.getVertices().capacity());
    Assert.assertEquals(EXPECT_POINTS_POST_FILTER * 3, vbo.getNormals().capacity());
    Assert.assertEquals(EXPECT_POINTS_POST_FILTER * 3, vbo.getColors().capacity());

    // input quads are loaded as ... quads in the VBO
    Assert.assertEquals(EXPECT_CELLS , vbo.getElementsIndices().capacity());
    Assert.assertEquals(EXPECT_CELLS, vbo.getElementsCount().capacity());
    Assert.assertEquals(QUAD_POINTS , vbo.getElementsCount().get(0));

    // these are not used
    Assert.assertNull(vbo.getElements());
    Assert.assertNull(vbo.getElementsLength());
    Assert.assertNull(vbo.getElementsStarts());
    
    
    // --------------------------------------------
    // When load another property, Then size is correct

    float[] colors = builder.getPropertyColor("combo");

    Assert.assertEquals(EXPECT_POINTS_POST_FILTER * 3, colors.length);


  }
  
  
  ///////////////////////////////////////////////////////////////////
  //
  //                          REPEATED
  //
  ///////////////////////////////////////////////////////////////////
  
  @Test
  public void givenRepeatedVerticeHexahedron() {
    ChartFactory f = new AWTChartFactory();
    f.getPainterFactory().setOffscreen(500, 500);
    Chart chart = f.newChart();

    int EXPECT_CELLS = 5778;
    int EXPECT_POINTS = 9114;

    // --------------------------------------------
    // Given a file with hexahedrons inside
    
    vtkUnstructuredGrid grid = VTKReader
        .getReaderOutput("./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080.pvtu");


    Assert.assertEquals(EXPECT_CELLS, grid.GetNumberOfCells());
    Assert.assertEquals(EXPECT_POINTS, grid.GetNumberOfPoints());

    // --------------------------------------------
    // When load, make and add to chart
    
    VTKDrawableVBOBuilder builder = new VTKDrawableVBOBuilder(grid, GeometryMode.MULTI_GEOMETRY,
        VerticeMode.REPEATED, VTKGeometry.VTK_HEXAHEDRON);

    DrawableVBO2 vbo = builder.makePolygons("enthalpy");

    Assert.assertEquals(3, builder.getColorChannels());

    chart.add(vbo);

    
    // --------------------------------------------
    // Then buffers have the appropriate size
    
    Assert.assertEquals(EXPECT_POINTS * 3, vbo.getVertices().capacity());
    Assert.assertEquals(EXPECT_POINTS * 3, vbo.getNormals().capacity());
    Assert.assertEquals(EXPECT_POINTS * 3, vbo.getColors().capacity());

    
    // input quads are loaded as ... quads in the VBO
    Assert.assertEquals(EXPECT_CELLS*HEXAHEDRON_FACES, vbo.getElementsStarts().capacity()); // ??
    Assert.assertEquals(EXPECT_CELLS*HEXAHEDRON_FACES, vbo.getElementsLength().capacity()); // ??
    Assert.assertEquals(QUAD_POINTS , vbo.getElementsLength().get(0));

    // these are not used
    Assert.assertNull(vbo.getElements());
    Assert.assertNull(vbo.getElementsIndices()); 
    Assert.assertNull(vbo.getElementsCount());
    
    
    // --------------------------------------------
    // When load another property, Then size is correct

    float[] colors = builder.getPropertyColor("temp");

    Assert.assertEquals(EXPECT_POINTS * 3, colors.length);

    
  }
  
  
  @Test
  public void givenRepeatedVerticeQuads() {
    ChartFactory f = new AWTChartFactory();
    f.getPainterFactory().setOffscreen(500, 500);
    Chart chart = f.newChart();

    int EXPECT_CELLS = 7160;
    int EXPECT_POINTS = 9114;
    int EXPECT_POINTS_POST_FILTER = 6197;


    // --------------------------------------------
    // Given a file with hexahedrons inside ...
    
    vtkAlgorithm reader = VTKReader
        .getReader("./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080.pvtu");

    
    // ... converted to quads
    
    vtkGeometryFilter quadConversion = new vtkGeometryFilter();
    quadConversion.SetInputConnection(reader.GetOutputPort());
    quadConversion.Update();

    vtkPolyData quads = quadConversion.GetOutput();
    

    Assert.assertEquals(EXPECT_CELLS, quads.GetNumberOfCells());
    Assert.assertEquals(EXPECT_POINTS_POST_FILTER, quads.GetNumberOfPoints());

    // --------------------------------------------
    // When load, make and add to chart
    
    VTKDrawableVBOBuilder builder = new VTKDrawableVBOBuilder(quads, GeometryMode.MULTI_GEOMETRY,
        VerticeMode.REPEATED, VTKGeometry.VTK_QUAD);

    // --------------------------------------------
    // Then buffers have the appropriate size
    
    DrawableVBO2 vbo = builder.makePolygons("enthalpy");
    
    Assert.assertEquals(3, builder.getColorChannels());

    chart.add(vbo);

    Assert.assertEquals(EXPECT_POINTS_POST_FILTER * 3, vbo.getVertices().capacity());
    Assert.assertEquals(EXPECT_POINTS_POST_FILTER * 3, vbo.getNormals().capacity());
    Assert.assertEquals(EXPECT_POINTS_POST_FILTER * 3, vbo.getColors().capacity());

    // input quads are loaded as ... quads in the VBO
    Assert.assertEquals(EXPECT_CELLS*QUAD_POINTS, vbo.getElementsStarts().capacity()); // ??
    Assert.assertEquals(EXPECT_CELLS*QUAD_POINTS , vbo.getElementsLength().capacity()); // ??
    Assert.assertEquals(QUAD_POINTS , vbo.getElementsLength().get(0));

    // these are not used
    Assert.assertNull(vbo.getElements());
    Assert.assertNull(vbo.getElementsIndices()); 
    Assert.assertNull(vbo.getElementsCount());

    
    // --------------------------------------------
    // When load another property, Then size is correct

    float[] colors = builder.getPropertyColor("marker2");

    Assert.assertEquals(EXPECT_POINTS_POST_FILTER * 3, colors.length);

  }
  
  ///////////////////////////////////////////////////////////////////
  //
  //                          ALPHA
  //
  ///////////////////////////////////////////////////////////////////

  @Test
  public void givenNonOpaqueSetting_Quads() {
    ChartFactory f = new AWTChartFactory();
    f.getPainterFactory().setOffscreen(500, 500);
    Chart chart = f.newChart();

    int EXPECT_POINTS = 9114;
    int EXPECT_POINTS_POST_FILTER = 6197;

    
    float ALPHA = 0.5f;
    int ALPHA_INDEX = 3;

    // --------------------------------------------
    // Given a file with hexahedrons inside
    
    vtkAlgorithm reader = VTKReader
        .getReader("./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080.pvtu");

    
    // ... converted to quads
    
    vtkGeometryFilter quadConversion = new vtkGeometryFilter();
    quadConversion.SetInputConnection(reader.GetOutputPort());
    quadConversion.Update();

    vtkPolyData quads = quadConversion.GetOutput();

    
    // --------------------------------------------
    // When load, make and add to chart
    
    VTKDrawableVBOBuilder builder = new VTKDrawableVBOBuilder(quads);

    Assert.assertEquals(3, builder.getColorChannels()); // does not contains alpha yet

    builder.setAlpha(0.5f);

    Assert.assertEquals(4, builder.getColorChannels()); // contains alpha

    DrawableVBO2 vbo = builder.makePolygons("enthalpy");

    chart.add(vbo);
    
    // --------------------------------------------
    // Then buffers have the appropriate size
    
    Assert.assertEquals(EXPECT_POINTS_POST_FILTER * 4, vbo.getColors().capacity()); // contains alpha

    Assert.assertEquals(ALPHA, vbo.getColors().get(0+ALPHA_INDEX)); // alpha properly set for a few point
    Assert.assertEquals(ALPHA, vbo.getColors().get(4+ALPHA_INDEX)); // alpha properly set for a few point
    Assert.assertEquals(ALPHA, vbo.getColors().get(8+ALPHA_INDEX)); // alpha properly set for a few point

    Assert.assertEquals(4, vbo.getColorChannels()); // contains alpha AFTER being loaded
    
    // --------------------------------------------
    // When load another property, Then size is correct

    float[] colors = builder.getPropertyColor("marker1");

    Assert.assertEquals(EXPECT_POINTS_POST_FILTER * 4, colors.length); // contains alpha
    
    Assert.assertEquals(ALPHA, colors[0+ALPHA_INDEX]); // alpha properly set for a few point
    Assert.assertEquals(ALPHA, colors[4+ALPHA_INDEX]); // alpha properly set for a few point
    Assert.assertEquals(ALPHA, colors[8+ALPHA_INDEX]); // alpha properly set for a few point
    
  }
  
  @Test
  public void givenNonOpaqueSetting_Hexahedrons() {
    ChartFactory f = new AWTChartFactory();
    f.getPainterFactory().setOffscreen(500, 500);
    Chart chart = f.newChart();

    int EXPECT_POINTS = 9114;
    
    float ALPHA = 0.5f;
    int ALPHA_INDEX = 3;

    // --------------------------------------------
    // Given a file with hexahedrons inside
    
    vtkUnstructuredGrid grid = VTKReader
        .getReaderOutput("./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080.pvtu");

    // --------------------------------------------
    // When load, make and add to chart
    
    VTKDrawableVBOBuilder builder = new VTKDrawableVBOBuilder(grid);

    Assert.assertEquals(3, builder.getColorChannels()); // does not contains alpha yet

    builder.setAlpha(0.5f);

    Assert.assertEquals(4, builder.getColorChannels()); // contains alpha

    DrawableVBO2 vbo = builder.makePolygons("enthalpy");

    chart.add(vbo);
    
    // --------------------------------------------
    // Then buffers have the appropriate size
    
    Assert.assertEquals(EXPECT_POINTS * 4, vbo.getColors().capacity()); // contains alpha

    Assert.assertEquals(ALPHA, vbo.getColors().get(0+ALPHA_INDEX)); // alpha properly set for a few point
    Assert.assertEquals(ALPHA, vbo.getColors().get(4+ALPHA_INDEX)); // alpha properly set for a few point
    Assert.assertEquals(ALPHA, vbo.getColors().get(8+ALPHA_INDEX)); // alpha properly set for a few point

    Assert.assertEquals(4, vbo.getColorChannels()); // contains alpha AFTER being loaded
    
    // --------------------------------------------
    // When load another property, Then size is correct

    float[] colors = builder.getPropertyColor("marker1");

    Assert.assertEquals(EXPECT_POINTS * 4, colors.length); // contains alpha
    
    Assert.assertEquals(ALPHA, colors[0+ALPHA_INDEX]); // alpha properly set for a few point
    Assert.assertEquals(ALPHA, colors[4+ALPHA_INDEX]); // alpha properly set for a few point
    Assert.assertEquals(ALPHA, colors[8+ALPHA_INDEX]); // alpha properly set for a few point

    
  }
}
