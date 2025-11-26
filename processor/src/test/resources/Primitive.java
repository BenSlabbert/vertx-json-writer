/* Licensed under Apache-2.0 2024. */
package my.test;

import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;

@JsonWriter
public record Primitive(
    int number, boolean bool, float fl, double dub, short sh, char ch, byte b, long l) {

  public static Builder builder() {
    return null;
  }

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
