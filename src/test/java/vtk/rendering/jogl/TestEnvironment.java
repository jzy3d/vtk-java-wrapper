package vtk.rendering.jogl;

import org.junit.Assert;
import org.junit.Test;

public class TestEnvironment {
  @Test
  public void setGet() {
    Environment e = new Environment();
    
    // When get a non existing variable, returns a null value
    String variable = "TEST_VAR";
    
    Assert.assertNull(e.get(variable));

    // When set an environment variable, returns the value that was set
    String value = "test_value";
    
    e.set(variable, value);
    
    Assert.assertEquals(value, e.get(variable));
  }
}
