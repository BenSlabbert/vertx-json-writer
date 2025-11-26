/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.example.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;

@JsonWriter
public record Job(String name) {

  public static Builder builder() {
    return new AutoBuilder_Job_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder name(String name);

    Job build();
  }
}
