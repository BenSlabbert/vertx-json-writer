/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import com.palantir.javapoet.TypeName;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PropertyBuilder {

  private static final String NOT_NULL = "jakarta.validation.constraints.NotNull";
  private static final String NOT_BLANK = "jakarta.validation.constraints.NotBlank";
  private static final String MIN = "jakarta.validation.constraints.Min";
  private static final String MAX = "jakarta.validation.constraints.Max";
  private static final String SIZE = "jakarta.validation.constraints.Size";

  private PropertyBuilder() {}

  static List<Property> getProperties(Element e) {
    List<Property> properties = new ArrayList<>();

    // RecordComponentElement does not work here as the annotations do not have
    // @Target(ElementType.RECORD_COMPONENT) as their target
    List<? extends Element> recordComponents =
        e.getEnclosedElements().stream().filter(f -> f.getKind() == ElementKind.FIELD).toList();

    for (Element enclosedElement : recordComponents) {
      VariableElement re = (VariableElement) enclosedElement;
      // name of the variable
      Name varName = re.getSimpleName();
      // type of the variable
      TypeMirror type = re.asType();
      // TypeKind.DECLARED -> this is an object
      TypeKind kind = type.getKind();
      Min min = re.getAnnotation(Min.class);
      Max max = re.getAnnotation(Max.class);
      Size size = re.getAnnotation(Size.class);

      // if type is declared and java.lang.String it is ok
      if (TypeKind.DECLARED == kind) {
        Property property = fromPreparedType(type, re, varName, kind, min, max, size);
        properties.add(property);
      } else if (kind.isPrimitive()) {
        Property property =
            new Property(
                varName.toString(), false, false, null, kind, false, min, max, size, List.of());
        properties.add(property);
      } else {
        String msg = String.format("unsupported kind: %s", kind);
        throw new GenerationException(msg);
      }
    }

    return properties;
  }

  private static Property fromPreparedType(
      TypeMirror type,
      VariableElement re,
      Name varName,
      TypeKind kind,
      Min min,
      Max max,
      Size size) {
    boolean nullable = null != re.getAnnotation(Nullable.class);
    boolean notBlank = null != re.getAnnotation(NotBlank.class);

    List<GenericParameterAnnotation> genericParameterAnnotations =
        getGenericParameterAnnotations((DeclaredType) type);

    TypeName tn = TypeName.get(type);
    TypeName typeNameWithoutAnnotations = tn.withoutAnnotations();
    String typeString = typeNameWithoutAnnotations.toString();
    return new Property(
        varName.toString(),
        nullable,
        true,
        typeString,
        kind,
        notBlank,
        min,
        max,
        size,
        genericParameterAnnotations);
  }

  private static List<GenericParameterAnnotation> getGenericParameterAnnotations(
      DeclaredType declaredType) {
    List<? extends TypeMirror> ta = declaredType.getTypeArguments();
    if (ta.isEmpty()) {
      return List.of();
    }
    if (ta.size() > 1) {
      throw new GenerationException("only support generic types with single generic type argument");
    }

    TypeMirror genericParameter = ta.getFirst();
    List<GenericParameterAnnotation> arr = new ArrayList<>();

    for (AnnotationMirror am : genericParameter.getAnnotationMirrors()) {
      DeclaredType annotationType = am.getAnnotationType();
      Element element = annotationType.asElement();
      String annotationClassName = element.asType().toString();
      switch (annotationClassName) {
        case NOT_NULL -> {
          GenericParameterAnnotation a = GenericParameterAnnotation.NotNull.create();
          arr.add(a);
        }
        case NOT_BLANK -> {
          GenericParameterAnnotation a = GenericParameterAnnotation.NotBlank.create();
          arr.add(a);
        }
        case MIN -> {
          GenericParameterAnnotation a = GenericParameterAnnotation.Min.create(am);
          arr.add(a);
        }
        case MAX -> {
          GenericParameterAnnotation a = GenericParameterAnnotation.Max.create(am);
          arr.add(a);
        }
        case SIZE -> {
          GenericParameterAnnotation a = GenericParameterAnnotation.Size.create(am);
          arr.add(a);
        }
        case null, default ->
            throw new GenerationException("unsupported annotation: " + annotationClassName);
      }
    }

    return List.copyOf(arr);
  }
}
