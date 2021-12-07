package vtk.examples.pvtu;

import java.util.ArrayList;
import java.util.List;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.ChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.factories.DepthPeelingPainterFactory;
import org.jzy3d.io.vtk.drawable.VTKDrawableVBOBuilder;
import org.jzy3d.plot3d.primitives.CoplanarityManager;
import org.jzy3d.plot3d.primitives.Drawable;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO2;
import vtk.VTKReader;
import vtk.VTKUtils;
import vtk.vtkAlgorithm;
import vtk.vtkAlgorithmOutput;
import vtk.vtkCleanPolyData;
import vtk.vtkGeometryFilter;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;
import vtk.vtkUnstructuredGrid;
import vtk.processing.VTKContourGridProcessor;
import vtk.processing.VTKContourProcessor;

/**
 * Need VM arguments : -Djava.library.path=/Users/martin/Dev/jzy3d/private/vtk-java-wrapper/lib/9.1.0/vtk-Darwin-x86_64
 * 
 * @author martin
 *
 */
public class PVTU {
  // see https://kitware.github.io/vtk-examples/site/Cxx/Visualization/LabelContours/
  public static void main(String[] args) {
    VTKUtils.loadVtkNativeLibraries();

    String file = "./src/test/resources/Enthalpy_Cylinder/Enthalpy_HS_cylinder_080.pvtu";
    String propertyName = "enthalpy";

    readAndShow(file, propertyName);
  }

  public static void readAndShow(String file, String propertyName) {
    vtkAlgorithm reader = VTKReader.getReader(file);
    vtkUnstructuredGrid ugrid = VTKReader.getOutput(reader);

    ugrid.GetPointData().SetActiveScalars(propertyName); // IMPORTANT TO GET CONTOUR PROCESSED!!!

    // ----------------------------------------------
    // Prepare the grid for contour processing

    boolean mergeDupplicatesAndProcessNormals = true;
    boolean filterPostDeduplicate = true;
    vtkPolyData data;
    vtkAlgorithmOutput dataPort;

    if (mergeDupplicatesAndProcessNormals) {
      // This is supposed to filter outer surface, but actually does not filtler anything but
      // converts source to polygons, which is our goal here
      vtkGeometryFilter convertToPoly = new vtkGeometryFilter();
      convertToPoly.SetInputConnection(reader.GetOutputPort());

      // This is supposed to merge the duplicated point
      vtkCleanPolyData cleanPolyData = new vtkCleanPolyData();
      cleanPolyData.SetInputConnection(convertToPoly.GetOutputPort());
      cleanPolyData.SetTolerance(0.0);
      cleanPolyData.ToleranceIsAbsoluteOff(); // relative to bounding box
      cleanPolyData.PointMergingOn();
      cleanPolyData.ConvertLinesToPointsOff();
      cleanPolyData.ConvertPolysToLinesOff();
      cleanPolyData.ConvertStripsToPolysOff();

      // Now that points are merged, try to apply geometry filter a second time
      // to remove internal faces - but they still appear
      vtkGeometryFilter outerSurfFilter = new vtkGeometryFilter();
      if (filterPostDeduplicate)
        outerSurfFilter.SetInputConnection(cleanPolyData.GetOutputPort());

      // Normals are important here for the contour processor to work properly
      vtkPolyDataNormals normalsCompute = new vtkPolyDataNormals();
      normalsCompute.SetInputConnection(
          filterPostDeduplicate ? outerSurfFilter.GetOutputPort() : cleanPolyData.GetOutputPort());
      normalsCompute.ComputePointNormalsOn();
      normalsCompute.ComputeCellNormalsOff();
      normalsCompute.ConsistencyOn();
      normalsCompute.AutoOrientNormalsOn();
      normalsCompute.FlipNormalsOff();
      normalsCompute.SetFeatureAngle(180);
      normalsCompute.SplittingOff();

      normalsCompute.Update();

      data = normalsCompute.GetOutput();
      dataPort = normalsCompute.GetOutputPort();
    } else {
      // Simply make conversion to polygon
      vtkGeometryFilter convertToPoly = new vtkGeometryFilter();
      convertToPoly.SetInputConnection(reader.GetOutputPort());
      data = convertToPoly.GetOutput();
      dataPort = convertToPoly.GetOutputPort();
    }

    // ----------------------------------------------
    // Process contour

    int n = 5; // including min/max
    double min = data.GetScalarRange()[0];
    double max = data.GetScalarRange()[1];

    data.GetPointData().SetActiveScalars(propertyName);

    VTKContourProcessor contour = new VTKContourProcessor(dataPort, n, min, max);

    //VTKContourGridProcessor contour = new VTKContourGridProcessor(dataPort, n, min, max);

    System.out.println("contour.n=" + n);
    System.out.println("contour.min=" + min);
    System.out.println("contour.max=" + max);
    System.out.println("contour.lines=" + contour.getNumberOfContourLines());

    // ----------------------------------------------
    // Draw

    ChartFactory f = new AWTChartFactory(new DepthPeelingPainterFactory());
    //ChartFactory f = new AWTChartFactory();
    Chart chart = f.newChart();
    chart.getView().setAxisDisplayed(true);
    chart.getView().setSquared(false);

    // TODO : check if we can avoid processing normal 2 times
    VTKDrawableVBOBuilder builderVBO = new VTKDrawableVBOBuilder(data);
    builderVBO.setAlpha(0.1f);

    DrawableVBO2 polygonsVBO = builderVBO.makePolygons(propertyName); // main shape
    Scatter scatter = builderVBO.makeDoublonPointScatter(); // duplicate vertices in red

    List<Drawable> drawables = new ArrayList<>();
    drawables.addAll(contour.getDrawableContourLines(Color.GRAY)); // contour lines
    drawables.addAll(contour.getDrawableContourLabels(Color.GRAY)); // contour labels
    drawables.add(scatter);

    CoplanarityManager coplanarDrawables = new CoplanarityManager(drawables, polygonsVBO); // cleanly
                                                                                           // draw
                                                                                           // coplanar
                                                                                           // stuffs

    chart.getScene().getGraph().addGraphListener(() -> {
      System.out.println("MOD  : " + chart.getView().getBoundsMode());
      System.out.println("ALL  : " + chart.getView().getBounds());
      System.out.println("COP  : " + coplanarDrawables.getBounds());
      System.out.println("VBO  : " + polygonsVBO.getBounds());
      System.out.println("SCA  : " + scatter.getBounds());
    });

    
    //chart.add(builderVBO.makeNormalsAsLines(100));
    chart.add(coplanarDrawables);
    chart.open();
    chart.getMouse();
  }
}
