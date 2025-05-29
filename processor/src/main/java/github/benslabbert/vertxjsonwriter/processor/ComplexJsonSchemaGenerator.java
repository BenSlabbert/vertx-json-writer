/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import java.util.List;

class ComplexJsonSchemaGenerator {

  private ComplexJsonSchemaGenerator() {}

  static SchemaGenerator handleComplexProperty(Property property) {
    String className = property.className();
    String name = property.name();
    boolean nullable = property.nullable();

    if (property.isString()) {
      return StringSchemaGenerator.create(property);
    }

    if (property.isBoolean()) {
      return BooleanSchemaGenerator.create(property);
    }

    if (property.isLong() || property.isInteger()) {
      return IntegerSchemaGenerator.create(property);
    }

    if (property.isDouble() || property.isFloat()) {
      return NumberSchemaGenerator.create(property);
    }

    if (property.isLocalDate()) {
      return DateSchemaGenerator.create(property);
    }

    if (property.isLocalDateTime() || property.isOffsetDateTime()) {
      return DateTimeSchemaGenerator.create(property);
    }

    if (isCollectionType(className)) {
      boolean unique = uniqueCollection(className);
      return handleCollectionProperty(property, unique);
    }

    return new ObjectSchemaGenerator(name, !nullable, className);
  }

  /**
   * @param className has generic params
   */
  private static boolean isCollectionType(String className) {
    return className.startsWith("java.util.Set")
        || className.startsWith("java.util.List")
        || className.startsWith("java.util.Collection");
  }

  /**
   * @param className has generic params
   */
  private static boolean uniqueCollection(String className) {
    return className.startsWith("java.util.Set");
  }

  private static SchemaGenerator handleCollectionProperty(Property property, boolean unique) {
    List<GenericParameterAnnotation> gpa = property.genericParameterAnnotations();
    var notNull = gpa.stream().anyMatch(f -> f instanceof GenericParameterAnnotation.NotNull);
    var notBlank = gpa.stream().anyMatch(f -> f instanceof GenericParameterAnnotation.NotBlank);
    var maybeSize =
        gpa.stream()
            .filter(f -> f instanceof GenericParameterAnnotation.Size)
            .map(s -> (GenericParameterAnnotation.Size) s)
            .findFirst();
    var maybeMin =
        gpa.stream()
            .filter(f -> f instanceof GenericParameterAnnotation.Min)
            .map(s -> (GenericParameterAnnotation.Min) s)
            .findFirst();
    var maybeMax =
        gpa.stream()
            .filter(f -> f instanceof GenericParameterAnnotation.Max)
            .map(s -> (GenericParameterAnnotation.Max) s)
            .findFirst();

    Integer sizeMin = maybeSize.map(GenericParameterAnnotation.Size::min).orElse(null);
    Integer sizeMax = maybeSize.map(GenericParameterAnnotation.Size::max).orElse(null);
    Long minValue = maybeMin.map(GenericParameterAnnotation.Min::value).orElse(null);
    Long maxValue = maybeMax.map(GenericParameterAnnotation.Max::value).orElse(null);

    SchemaGenerator itemSchemaGenerator =
        createItemSchemaGenerator(
            property.getGenericType(), notNull, notBlank, sizeMin, sizeMax, minValue, maxValue);

    return new ArraySchemaGenerator(
        property.name(),
        !property.nullable(),
        unique,
        itemSchemaGenerator,
        property.getSizeMin(),
        property.getSizeMax());
  }

  private static SchemaGenerator createItemSchemaGenerator(
      String genericType,
      boolean notNull,
      boolean notBlank,
      Integer sizeMin,
      Integer sizeMax,
      Long minValue,
      Long maxValue) {

    return switch (genericType) {
      case "java.lang.String" ->
          new StringSchemaGenerator(null, notNull, notBlank, sizeMin, sizeMax);
      case "java.lang.Boolean" -> new BooleanSchemaGenerator(null, notNull);
      case "java.lang.Long", "java.lang.Integer", "java.lang.Short" ->
          new IntegerSchemaGenerator(null, notNull, minValue, maxValue);
      case "java.lang.Float", "java.lang.Double" ->
          new NumberSchemaGenerator(null, notNull, minValue, maxValue);
      default -> new ObjectSchemaGenerator(null, notNull, genericType);
    };
  }
}
