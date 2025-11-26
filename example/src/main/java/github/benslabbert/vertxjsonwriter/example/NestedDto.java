/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.example;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;

@JsonWriter
public record NestedDto(String name, InnerDto innerDto) {

  public static Builder builder() {
    return new AutoBuilder_NestedDto_Builder();
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

    @AutoBuilder
    public interface Builder {

      Builder name(String name);

      InnerDto build();
    }
  }
}
