package org.example.dto;

import com.google.auto.value.AutoBuilder;
import io.vertx.core.json.JsonObject;
import org.example.processor.annotation.JsonWriter;

@JsonWriter
public record Job(String name) {

  public static Builder builder() {
    return new AutoBuilder_Job_Builder();
  }

  public static Job fromJson(JsonObject json) {
    return Job_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return Job_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder name(String name);

    Job build();
  }
}
