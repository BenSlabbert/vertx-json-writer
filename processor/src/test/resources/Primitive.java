/* Licensed under Apache-2.0 2024. */
package my.test;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.Validator;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;

@JsonWriter
public record Primitive(
    int number, boolean bool, float fl, double dub, short sh, char ch, byte b, long l) {

  public static Builder builder() {
    return null;
  }

  public static Primitive fromJson(JsonObject json) {
    return Primitive_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return Primitive_JsonWriter.toJson(this);
  }

  public static Validator getValidator() {
    return Primitive_JsonWriter.getValidator();
  }

  public static ObjectSchemaBuilder schemaBuilder() {
    return Primitive_JsonWriter.schemaBuilder();
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

    Primitive build();
  }
}
