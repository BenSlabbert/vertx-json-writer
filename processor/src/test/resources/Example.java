/* Licensed under Apache-2.0 2024. */
package my.test;

import github.benslabbert.jsonwriter.processor.annotation.JsonWriter;

@JsonWriter
public record Example(String name) {

  public static Builder builder() {
    return null;
  }

  public interface Builder {
    Builder name(String name);

    Example build();
  }
}
