package org.jzy3d.maths.algorithms.decimator;

import java.util.Set;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Polygon;

public interface NextCellFinder {
  public NextCellResult getNextCell(Polygon current);
  
  public class NextCellResult{
    protected Polygon polygon;
    protected Set<Coord3d> side;
    protected boolean valid = true;
    protected String info;
    
    public NextCellResult(Polygon polygon, Set<Coord3d> side) {
      this.polygon = polygon;
      this.side = side;
    }
    
    public NextCellResult(Polygon polygon, Set<Coord3d> side, boolean valid, String info) {
      this.polygon = polygon;
      this.side = side;
      this.valid = valid;
      this.info = info;
    }

    public Polygon getPolygon() {
      return polygon;
    }

    public Set<Coord3d> getSide() {
      return side;
    }

    public boolean isValid() {
      return valid;
    }

    public void setValid(boolean valid) {
      this.valid = valid;
    }

    public String getInfo() {
      return info;
    }

    public void setInfo(String info) {
      this.info = info;
    }
    
    
  }
}
