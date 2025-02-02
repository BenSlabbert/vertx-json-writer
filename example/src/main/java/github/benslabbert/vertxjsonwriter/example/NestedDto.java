/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.example;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record NestedDto(String name, InnerDto innerDto) {

  public static Builder builder() {
    return new AutoBuilder_NestedDto_Builder();
  }

  public static NestedDto fromJson(JsonObject json) {
    return NestedDto_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return NestedDto_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder name(String name);

    Builder innerDto(InnerDto innerDto);

    NestedDto build();
  }

  @JsonWriter
  public record InnerDto(String name) {

    public static Builder builder() {
      return new AutoBuilder_NestedDto_InnerDto_Builder();
    }

    public static InnerDto fromJson(JsonObject json) {
      return NestedDto_InnerDto_JsonWriter.fromJson(json);
    }

    public JsonObject toJson() {
      return NestedDto_InnerDto_JsonWriter.toJson(this);
    }

    @AutoBuilder
    public interface Builder {

      Builder name(String name);

      InnerDto build();
    }
  }
}
