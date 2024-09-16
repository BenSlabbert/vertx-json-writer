/* Licensed under Apache-2.0 2024. */
package github.benslabbert.jsonwriter.example.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@JsonWriter
public record Times(LocalDate date, LocalDateTime time, OffsetDateTime offsetDateTime) {

  public static Builder builder() {
    return null;
  }

  @AutoBuilder
  public interface Builder {
    Builder date(LocalDate date);

    Builder time(LocalDateTime time);

    Builder offsetDateTime(OffsetDateTime offsetDateTime);

    Times build();
  }
}
