/* Licensed under Apache-2.0 2024. */
package github.benslabbert.jsonwriter.example.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.processor.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record Person(String name, int age, boolean bool, Job job) {

  public static Person.Builder builder() {
    return new AutoBuilder_Person_Builder();
  }

  public static Person fromJson(JsonObject json) {
    return Person_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return Person_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder name(String name);

    Builder age(int age);

    Builder bool(boolean bool);

    Builder job(Job job);

    Person build();
  }
}
