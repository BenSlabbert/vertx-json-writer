/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.example.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;

@JsonWriter
public record PrimitiveEntity(
    String name,
    int number,
    boolean bool,
    float fl,
    double dub,
    short sh,
    char ch,
    byte b,
    long l) {

  public static PrimitiveEntity.Builder builder() {
    return new AutoBuilder_PrimitiveEntity_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder name(String name);

    Builder number(int number);

    Builder bool(boolean bool);

    Builder fl(float fl);

    Builder dub(double dub);

    Builder sh(short sh);

    Builder ch(char ch);

    Builder b(byte b);

    Builder l(long l);

    PrimitiveEntity build();
  }
}
