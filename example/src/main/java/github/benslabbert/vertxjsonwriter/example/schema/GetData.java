/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.example.schema;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.Validator;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import jakarta.validation.constraints.NotNull;

@JsonWriter
public record GetData(@NotNull Request request) {

  public static Builder builder() {
    return new AutoBuilder_GetData_Builder();
  }

  public static GetData fromJson(JsonObject json) {
    return GetData_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return GetData_JsonWriter.toJson(this);
  }

  public static Validator getValidator() {
    return GetData_JsonWriter.getValidator();
  }

  static ObjectSchemaBuilder schemaBuilder() {
    return GetData_JsonWriter.schemaBuilder();
  }

  @AutoBuilder
  public interface Builder {
    Builder request(Request request);

    GetData build();
  }
}
