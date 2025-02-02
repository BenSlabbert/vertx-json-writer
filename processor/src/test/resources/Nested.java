/* Licensed under Apache-2.0 2024. */
package my.test;

import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record Nested(String name, Inner inner) {

  public static Builder builder() {
    return null;
  }

  public interface Builder {
    Builder name(String name);

    Builder inner(Inner inner);

    Nested build();
  }

  @JsonWriter
  public record Inner(String innerName) {

    public static Inner fromJson(JsonObject json) {
      return new Inner("innerName");
    }

    public JsonObject toJson() {
      return new JsonObject();
    }

    public static Builder builder() {
      return null;
    }

    public interface Builder {
      Builder innerName(String innerName);

      Inner build();
    }
  }
}
