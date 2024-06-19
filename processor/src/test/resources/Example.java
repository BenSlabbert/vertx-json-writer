/* Licensed under Apache-2.0 2024. */
package my.test;

import github.benslabbert.jsonwriter.annotation.JsonWriter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonWriter
public record Example(@NotBlank @Size(min = 1, max = 10) String name) {

  public static Builder builder() {
    return null;
  }

  public interface Builder {
    Builder name(String name);

    Example build();
  }
}
