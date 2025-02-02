/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.net.URL;
import org.junit.jupiter.api.Test;

class JsonWriterProcessorTest {

  @Test
  void t1() {
    JsonObject json = new JsonObject();

    JsonArray array = new JsonArray();
    array.add(1);
    array.add(2);
    array.add(3);

    json.put("array", array);
    System.err.println(json.encode());
    JsonArray arr = json.getJsonArray("array");
    System.err.println();
    JsonArray array1 = new JsonObject("{\"array\":[1,2,3]}").getJsonArray("array");
    for (Object o : array1) {
      // java.lang.String
      System.err.println(o.getClass());
    }
  }

  @Test
  void t2() {
    JsonArray array1 = new JsonObject("{\"array\":[{\"key\": \"val\"}]}").getJsonArray("array");
    for (Object o : array1) {
      // io.vertx.core.json.JsonObject
      System.err.println(o.getClass());
    }
  }

  @Test
  void test() {
    URL resource = this.getClass().getClassLoader().getResource("Example.java");
    assertThat(resource).isNotNull();

    assertAbout(JavaSourceSubjectFactory.javaSource())
        .that(JavaFileObjects.forResource(resource))
        .processedWith(new JsonWriterProcessor())
        .compilesWithoutError();
  }
}
