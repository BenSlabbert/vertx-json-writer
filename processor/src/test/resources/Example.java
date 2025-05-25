/* Licensed under Apache-2.0 2024. */
package my.test;

import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Set;

@JsonWriter
public record Example(
    @NotBlank @Size(min = 1, max = 10) String name,
    @Min(1) @Max(10) Integer value,
    LocalDate date,
    LocalDateTime time,
    OffsetDateTime offsetDateTime,
    Set<@NotNull @NotBlank @Size(min = 2) String> tags) {

  public static Builder builder() {
    return null;
  }

  public interface Builder {
    Builder name(String name);

    Builder value(int value);

    Builder date(LocalDate date);

    Builder time(LocalDateTime time);

    Builder offsetDateTime(OffsetDateTime offsetDateTime);

    Builder tags(Set<String> tags);

    Example build();
  }
}
