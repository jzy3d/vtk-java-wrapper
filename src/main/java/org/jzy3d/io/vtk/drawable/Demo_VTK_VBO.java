package org.jzy3d.io.vtk.drawable;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.AWTPainterFactory;
import org.jzy3d.chart.factories.ChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.io.vtk.drawable.VTKDrawableVBOBuilder.GeometryMode;
import org.jzy3d.io.vtk.drawable.VTKDrawableVBOBuilder.VerticeMode;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO2;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import vtk.VTKGeometry;
import vtk.VTKUtils;
import vtk.vtkUnstructuredGrid;
import vtk.vtkXMLUnstructuredGridReader;

public class Demo_VTK_VBO {
  public static void main(String[] args) {
    VTKUtils.loadVtkNativeLibraries();
    
    
    // Given
    vtkXMLUnstructuredGridReader reader = new vtkXMLUnstructuredGridReader();
    //reader.SetFileName("./src/test/resources/Enthalpy_HS_wCon_wRad_010_0.vtu");
    reader.SetFileName("./src/test/resources/out0006.000.vtu");
    reader.Update();

    vtkUnstructuredGrid grid = reader.GetOutput();
    
    
    VTKDrawableVBOBuilder b = new VTKDrawableVBOBuilder(grid, GeometryMode.MULTI_GEOMETRY, VerticeMode.REPEATED, VTKGeometry.VTK_HEXAHEDRON);
    b.setWireframeDisplayed(true);
    b.setWireframeColor(Color.BLUE);
    b.setReflectLight(true);
    
    
    
    DrawableVBO2 vbo = b.makePolygons("cn");
    vbo.setComputeNormals(false);
    vbo.setWireframeWidth(3);
    
    
    //
    //GLCapabilities caps = new GLCapabilities(GLProfile.getMaximum(true));
    GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
    
    AWTPainterFactory p = new AWTPainterFactory(caps);
    ChartFactory f = new AWTChartFactory(p);
    Chart c =  f.newChart();
    
    c.add(vbo);
    //c.add(b.getVertexIds(0, 8));
    c.addLightOnCamera();
    c.open();
    c.getMouse();
    
    
    BoundingBox3d clip = new BoundingBox3d(0f,1f,2f,3f,-2f,2f);
    //c.getScene().getGraph().setClipBox(clip);
    c.getView().setBoundManual(clip);
    
  }

}
