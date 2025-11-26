/* Licensed under Apache-2.0 2024. */
package my.test;

import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;

@JsonWriter
public record Nested(String name, Inner inner) {

  public static Builder builder() {
    return null;
  }

  public interface Builder {
    Builder name(String name);

    Builder inner(Inner inner);

    Nested build();
  }

  @JsonWriter
  public record Inner(String innerName) {

    public static Builder builder() {
      return null;
    }

    public interface Builder {
      Builder innerName(String innerName);

      Inner build();
    }
  }
}
