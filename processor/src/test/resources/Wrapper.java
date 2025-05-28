/* Licensed under Apache-2.0 2024. */
package my.test;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.Validator;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;

@JsonWriter
public record Wrapper(
    Integer number, Boolean bool, Float fl, Double dub, Short sh, Character ch, Byte b, Long l) {

  public static Builder builder() {
    return null;
  }

  public static Wrapper fromJson(JsonObject json) {
    return Wrapper_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return Wrapper_JsonWriter.toJson(this);
  }

  public static Validator getValidator() {
    return Wrapper_JsonWriter.getValidator();
  }

  public static ObjectSchemaBuilder schemaBuilder() {
    return Wrapper_JsonWriter.schemaBuilder();
  }

  @AutoBuilder
  public interface Builder {
    Builder number(Integer number);

    Builder bool(Boolean bool);

    Builder fl(Float fl);

    Builder dub(Double dub);

    Builder sh(Short sh);

    Builder ch(Character ch);

    Builder b(Byte b);

    Builder l(Long l);

    Wrapper build();
  }
}
