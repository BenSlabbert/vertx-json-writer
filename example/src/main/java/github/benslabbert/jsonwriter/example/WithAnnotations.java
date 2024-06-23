/* Licensed under Apache-2.0 2024. */
package github.benslabbert.jsonwriter.example;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

@JsonWriter
public record WithAnnotations(@NotBlank @Size(min = 1, max = 10) String name) {

  public static Builder builder() {
    return new AutoBuilder_WithAnnotations_Builder();
  }

  public static WithAnnotations fromJson(JsonObject json) {
    return WithAnnotations_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return WithAnnotations_JsonWriter.toJson(this);
  }

  public static Set<String> missingRequiredFields(JsonObject json) {
    return WithAnnotations_JsonWriter.missingRequiredFields(json);
  }

  @AutoBuilder
  public interface Builder {
    Builder name(String name);

    WithAnnotations build();
  }
}
