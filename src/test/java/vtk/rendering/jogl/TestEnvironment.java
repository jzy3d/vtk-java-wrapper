package vtk.rendering.jogl;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

public class TestEnvironment {
  @Test
  public void check_Set_Get_Append_Remove() {
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

    e.appendFirst(variable, value2);

    Assert.assertEquals(seq("test_value2", "test_value1"), e.get(variable));


    // When append to an environment variable, returns the value that was set
    String value3 = "test_value3";

    e.appendLast(variable, value3);

    Assert.assertEquals(seq("test_value2", "test_value1", "test_value3"), e.get(variable));


    // When remove from an environment variable, returns the value that was set

    e.removeFrom(variable, value1);

    Assert.assertEquals(seq("test_value2", "test_value3"), e.get(variable));

  }

  public static String seq(String... strings) {
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < strings.length - 1; i++) {
      sb.append(strings[i] + File.pathSeparator);
    }
    sb.append(strings[strings.length - 1]);
    return sb.toString();
  }


  @Test
  public void removeWithSeparator() {
    Environment e = new Environment();

    // Given a variable with values separated with a path separator
    String variable = "TEST_VAR2";
    String value1 = "t1";

    e.set(variable, value1);
    e.appendLast(variable, "t2");
    e.appendLast(variable, "t3");
    e.appendLast(variable, "t4");
    e.appendLast(variable, "t5");

    Assert.assertEquals(seq("t1", "t2", "t3", "t4", "t5"), e.get(variable));

    // When remove a heading item, removes the related separator
    e.removeFrom(variable, "t1");

    Assert.assertEquals(seq("t2", "t3", "t4", "t5"), e.get(variable));


    // When remove a trailing item, removes the related separator
    e.removeFrom(variable, "t5");

    Assert.assertEquals(seq("t2", "t3", "t4"), e.get(variable));


    // When remove a trailing item, removes the related separators
    e.removeFrom(variable, "t3");

    Assert.assertEquals(seq("t2", "t4"), e.get(variable));
  }

  @Test
  public void removeNonExisting() {
    Environment e = new Environment();

    // Given a variable with values separated with a path separator
    String variable = "TEST_VAR3";
    String value1 = "t1";

    e.set(variable, value1);
    e.appendLast(variable, "t2");

    Assert.assertEquals(seq("t1", "t2"), e.get(variable));

    // When remove a heading item, removes the related separator
    e.removeFrom(variable, "t99");

    Assert.assertEquals(seq("t1", "t2"), e.get(variable));

  }

}
