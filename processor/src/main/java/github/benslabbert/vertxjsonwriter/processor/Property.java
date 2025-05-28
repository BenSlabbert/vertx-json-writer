/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
    List<GenericParameterAnnotation> genericParameterAnnotations) {}
