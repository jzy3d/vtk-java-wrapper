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
    String value1 = "test_value1";
    
    e.set(variable, value1);
    
    Assert.assertEquals(value1, e.get(variable));
    
    
    // When append to an environment variable, returns the value that was set
    String value2 = "test_value2";
    
    e.appendFirst(variable, value2, ";");
    
    Assert.assertEquals("test_value2;test_value1", e.get(variable));

    
    // When append to an environment variable, returns the value that was set
    String value3 = "test_value3";
    
    e.appendLast(variable, value3, ";");
    
    Assert.assertEquals("test_value2;test_value1;test_value3", e.get(variable));

    // When remove from an environment variable, returns the value that was set
    
    e.removeFrom(variable, value1);
    
    Assert.assertEquals("test_value2;;test_value3", e.get(variable));

  }
  

}
