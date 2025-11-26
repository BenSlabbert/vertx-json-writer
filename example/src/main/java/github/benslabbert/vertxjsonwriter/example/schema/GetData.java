/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.example.schema;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import jakarta.validation.constraints.NotNull;

@JsonWriter
public record GetData(@NotNull Request request) {

  public static Builder builder() {
    return new AutoBuilder_GetData_Builder();
  }

  @AutoBuilder
  public interface Builder {
    Builder request(Request request);

    GetData build();
  }
}
