/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import jakarta.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;

sealed interface GenericParameterAnnotation
    permits GenericParameterAnnotation.NotNull,
        GenericParameterAnnotation.NotBlank,
        GenericParameterAnnotation.Min,
        GenericParameterAnnotation.Max,
        GenericParameterAnnotation.Size {

  record NotNull() implements GenericParameterAnnotation {
    static NotNull create() {
      return new NotNull();
    }
  }

  record NotBlank() implements GenericParameterAnnotation {
    static NotBlank create() {
      return new NotBlank();
    }
  }

  record Min(long value) implements GenericParameterAnnotation {
    static Min create(AnnotationMirror am) {
      long min = 0L;
      for (var entry : am.getElementValues().entrySet()) {
        if (entry.getKey().getSimpleName().toString().equals("value")) {
          min = (long) entry.getValue().getValue();
        }
      }
      return new Min(min);
    }
  }

  record Max(long value) implements GenericParameterAnnotation {
    static Max create(AnnotationMirror am) {
      long max = 0L;
      for (var entry : am.getElementValues().entrySet()) {
        if (entry.getKey().getSimpleName().toString().equals("value")) {
          max = (long) entry.getValue().getValue();
        }
      }
      return new Max(max);
    }
  }

  record Size(@Nullable Integer min, @Nullable Integer max) implements GenericParameterAnnotation {
    static Size create(AnnotationMirror am) {
      Integer min = null;
      Integer max = null;
      for (var entry : am.getElementValues().entrySet()) {
        int defaultValue = (int) entry.getKey().getDefaultValue().getValue();

        if (entry.getKey().getSimpleName().toString().equals("min")) {
          min = (int) entry.getValue().getValue();
          if (min == defaultValue) {
            min = null;
          }
        }
        if (entry.getKey().getSimpleName().toString().equals("max")) {
          max = (int) entry.getValue().getValue();
          if (max == defaultValue) {
            max = null;
          }
        }
      }
      return new Size(min, max);
    }
  }
}
