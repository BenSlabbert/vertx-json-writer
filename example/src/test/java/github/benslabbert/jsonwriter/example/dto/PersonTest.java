/* Licensed under Apache-2.0 2024. */
package github.benslabbert.jsonwriter.example.dto;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.json.JsonObject;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PersonTest {

  @Test
  void test() {
    Set<String> fields =
        Person.missingRequiredFields(new JsonObject().put("job", new JsonObject()));

    assertThat(fields).containsExactlyInAnyOrder("name", "age", "bool", "job.name");
  }
}
