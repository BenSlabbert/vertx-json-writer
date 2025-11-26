/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.example;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonWriter
public record WithAnnotations(@NotBlank @Size(min = 1, max = 10) String name) {

  public static Builder builder() {
    return new AutoBuilder_WithAnnotations_Builder();
  }

  @AutoBuilder
  public interface Builder {
    Builder name(String name);

    WithAnnotations build();
  }
}
