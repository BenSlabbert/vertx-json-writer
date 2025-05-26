/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.example.schema;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.Validator;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import jakarta.annotation.Nullable;
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
public record Request(
    @NotNull @NotBlank @Size(min = 2, max = 10) String data,
    @Nullable @Size(max = 66) String other,
    @NotNull @Min(1) @Max(10) Integer integer,
    @NotNull @Min(20) @Max(100) Double number,
    @Nullable Boolean bool,
    @Nullable LocalDate localDate,
    @Nullable LocalDateTime localDateTime,
    @Nullable OffsetDateTime offsetDateTime,
    @Nullable @Size(min = 1) Set<@NotNull @Min(2) @Max(13) Integer> ages,
    @Nullable @Size(min = 2) Set<@NotNull @NotBlank @Size(min = 3) String> tags,
    @Nullable @Size(max = 25) String description) {

  public static Builder builder() {
    return new AutoBuilder_Request_Builder();
  }

  public static Request fromJson(JsonObject json) {
    return Request_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return Request_JsonWriter.toJson(this);
  }

  public static Validator getValidator() {
    return Request_JsonWriter.getValidator();
  }

  static ObjectSchemaBuilder schemaBuilder() {
    return Request_JsonWriter.schemaBuilder();
  }

  @AutoBuilder
  public interface Builder {
    Builder data(String data);

    Builder other(@Nullable String other);

    Builder integer(Integer integer);

    Builder number(Double number);

    Builder bool(@Nullable Boolean bool);

    Builder localDate(@Nullable LocalDate localDate);

    Builder localDateTime(@Nullable LocalDateTime localDateTime);

    Builder offsetDateTime(@Nullable OffsetDateTime offsetDateTime);

    Builder ages(@Nullable Set<Integer> ages);

    Builder tags(@Nullable Set<String> tags);

    Builder description(@Nullable String description);

    Request build();
  }
}
