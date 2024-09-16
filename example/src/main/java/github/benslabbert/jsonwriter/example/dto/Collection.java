/* Licensed under Apache-2.0 2024. */
package github.benslabbert.jsonwriter.example.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Set;

@JsonWriter
public record Collection(
    String name,
    List<String> strings,
    List<Boolean> booleans,
    List<Integer> integers,
    List<Long> longs,
    List<Float> floats,
    List<Double> doubles,
    Set<Integer> ages,
    java.util.Collection<Boolean> truisms,
    List<Job> jobList,
    Set<Job> jobSet,
    java.util.Collection<Job> jobCollection) {

  public static Builder builder() {
    return new AutoBuilder_Collection_Builder();
  }

  public static Collection fromJson(JsonObject json) {
    return Collection_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return Collection_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder name(String name);

    Builder strings(List<String> strings);

    Builder booleans(List<Boolean> booleans);

    Builder integers(List<Integer> integers);

    Builder longs(List<Long> longs);

    Builder floats(List<Float> floats);

    Builder doubles(List<Double> doubles);

    Builder jobList(List<Job> jobList);

    Builder jobSet(Set<Job> jobSet);

    Builder jobCollection(java.util.Collection<Job> jobCollection);

    Builder ages(Set<Integer> ages);

    Builder truisms(java.util.Collection<Boolean> truisms);

    Collection build();
  }
}
