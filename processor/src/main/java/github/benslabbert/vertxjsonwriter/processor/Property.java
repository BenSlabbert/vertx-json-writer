/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import javax.lang.model.type.TypeKind;

record Property(
    String name,
    boolean nullable,
    boolean isComplex,
    String className,
    TypeKind kind,
    boolean notBlank,
    Min min,
    Max max,
    Size size,
    List<GenericParameterAnnotation> genericParameterAnnotations) {

  @Nullable Integer getSizeMin() {
    return null == size || size.min() == 0 ? null : size.min();
  }

  @Nullable Integer getSizeMax() {
    return null == size || size.max() == Integer.MAX_VALUE ? null : size.max();
  }

  @Nullable Long getMinValue() {
    return null == min ? null : min.value();
  }

  @Nullable Long getMaxValue() {
    return null == max ? null : max.value();
  }

  boolean isString() {
    return kind == TypeKind.DECLARED && className.equals(String.class.getCanonicalName());
  }

  boolean isBoolean() {
    return kind == TypeKind.DECLARED && className.equals(Boolean.class.getCanonicalName());
  }

  boolean isLong() {
    return kind == TypeKind.DECLARED && className.equals(Long.class.getCanonicalName());
  }

  boolean isInteger() {
    return kind == TypeKind.DECLARED && className.equals(Integer.class.getCanonicalName());
  }

  boolean isDouble() {
    return kind == TypeKind.DECLARED && className.equals(Double.class.getCanonicalName());
  }

  boolean isFloat() {
    return kind == TypeKind.DECLARED && className.equals(Float.class.getCanonicalName());
  }

  boolean isLocalDate() {
    return kind == TypeKind.DECLARED && className.equals(LocalDate.class.getCanonicalName());
  }

  boolean isLocalDateTime() {
    return kind == TypeKind.DECLARED && className.equals(LocalDateTime.class.getCanonicalName());
  }

  boolean isOffsetDateTime() {
    return kind == TypeKind.DECLARED && className.equals(OffsetDateTime.class.getCanonicalName());
  }

  String getGenericType() {
    return className.substring(className.indexOf('<') + 1, className.indexOf('>'));
  }
}
