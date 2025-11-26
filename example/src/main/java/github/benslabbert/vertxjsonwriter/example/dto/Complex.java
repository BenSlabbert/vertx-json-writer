/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.example.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;

@JsonWriter
public record Complex(Long longValue, long longPrimitiveValue, String stringValue) {

  public static Builder builder() {
    return new AutoBuilder_Complex_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder longValue(Long longValue);

    Builder longPrimitiveValue(long longPrimitiveValue);

    Builder stringValue(String stringValue);

    Complex build();
  }
}
