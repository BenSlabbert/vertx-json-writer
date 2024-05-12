/* Licensed under Apache-2.0 2024. */
package github.benslabbert.jsonwriter.example.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.processor.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record Complex(Long longValue, long longPrimitiveValue, String stringValue) {

  public static Builder builder() {
    return new AutoBuilder_Complex_Builder();
  }

  public static Complex fromJson(JsonObject json) {
    return Complex_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return Complex_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder longValue(Long longValue);

    Builder longPrimitiveValue(long longPrimitiveValue);

    Builder stringValue(String stringValue);

    Complex build();
  }
}
