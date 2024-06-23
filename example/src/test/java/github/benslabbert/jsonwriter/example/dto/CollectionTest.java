/* Licensed under Apache-2.0 2024. */
package github.benslabbert.jsonwriter.example.dto;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CollectionTest {

  @Test
  void test() {
    Set<String> fields = Collection.missingRequiredFields(new JsonObject());

    assertThat(fields)
        .containsExactlyInAnyOrder(
            "name",
            "strings",
            "booleans",
            "integers",
            "longs",
            "floats",
            "doubles",
            "ages",
            "truisms",
            "jobList",
            "jobSet",
            "jobCollection");
  }

  @Test
  void testEmptyList() {
    Set<String> fields =
        Collection.missingRequiredFields(
            new JsonObject().put("jobList", new JsonArray().add(new JsonObject())));

    assertThat(fields)
        .containsExactlyInAnyOrder(
            "name",
            "strings",
            "booleans",
            "integers",
            "longs",
            "floats",
            "doubles",
            "ages",
            "truisms",
            "jobList[0].name",
            "jobSet",
            "jobCollection");
  }

  @Test
  void testNullList() {
    Set<String> fields =
        Collection.missingRequiredFields(
            new JsonObject().put("jobList", new JsonArray().add(null)));

    assertThat(fields)
        .containsExactlyInAnyOrder(
            "name",
            "strings",
            "booleans",
            "integers",
            "longs",
            "floats",
            "doubles",
            "ages",
            "truisms",
            "jobSet",
            "jobCollection");
  }
}
