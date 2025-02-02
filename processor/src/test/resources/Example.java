/* Licensed under Apache-2.0 2024. */
package my.test;

import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@JsonWriter
public record Example(
    @NotBlank @Size(min = 1, max = 10) String name,
    int value,
    LocalDate date,
    LocalDateTime time,
    OffsetDateTime offsetDateTime) {

  public static Builder builder() {
    return null;
  }

  public interface Builder {
    Builder name(String name);

    Builder value(int value);

    Builder date(LocalDate date);

    Builder time(LocalDateTime time);

    Builder offsetDateTime(OffsetDateTime offsetDateTime);

    Example build();
  }
}
