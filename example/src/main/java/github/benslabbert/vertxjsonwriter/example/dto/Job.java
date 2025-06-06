/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.example.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;

@JsonWriter
public record Job(String name) {

  public static Builder builder() {
    return new AutoBuilder_Job_Builder();
  }

  public static Job fromJson(JsonObject json) {
    return Job_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return Job_JsonWriter.toJson(this);
  }

  static ObjectSchemaBuilder schemaBuilder() {
    return PrimitiveEntity_JsonWriter.schemaBuilder();
  }

  @AutoBuilder
  public interface Builder {

    Builder name(String name);

    Job build();
  }
}
