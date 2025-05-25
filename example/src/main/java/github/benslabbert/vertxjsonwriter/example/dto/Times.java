/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.example.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@JsonWriter
public record Times(LocalDate date, LocalDateTime time, OffsetDateTime offsetDateTime) {

  public static Builder builder() {
    return new AutoBuilder_Times_Builder();
  }

  public static Times fromJson(JsonObject json) {
    return Times_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return Times_JsonWriter.toJson(this);
  }

  static ObjectSchemaBuilder schemaBuilder() {
    return Times_JsonWriter.schemaBuilder();
  }

  @AutoBuilder
  public interface Builder {
    Builder date(LocalDate date);

    Builder time(LocalDateTime time);

    Builder offsetDateTime(OffsetDateTime offsetDateTime);

    Times build();
  }
}
