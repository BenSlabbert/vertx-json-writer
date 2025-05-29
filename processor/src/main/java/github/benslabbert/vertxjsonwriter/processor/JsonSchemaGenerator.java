/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import static github.benslabbert.vertxjsonwriter.processor.Util.getGenericType;

import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

class JsonSchemaGenerator {

  private JsonSchemaGenerator() {}

  static void jsonSchema(PrintWriter out, List<Property> properties) {
    writeValidatorMethod(out);
    writeSchemaMethod(out);
    writeSchemaBuilderMethodStart(out);

    for (Property property : properties) {
      SchemaGenerator schemaGenerator;
      if (property.isComplex()) {
        schemaGenerator = handleComplexProperty(property);
      } else {
        schemaGenerator = handlePrimitiveProperty(property);
      }
      out.println(schemaGenerator.print());
      out.println();
    }

    out.println("\t;");
    out.println("\t}");
  }

  private static void writeValidatorMethod(PrintWriter out) {
    out.println(
        """
    public static Validator getValidator() {
        return Validator.create(
            schema(),
            new JsonSchemaOptions()
                .setBaseUri("https://example.com")
                .setDraft(Draft.DRAFT7)
                .setOutputFormat(OutputFormat.Basic));
    }
""");
    out.println();
  }

  private static void writeSchemaMethod(PrintWriter out) {
    out.println(
        """
    static JsonSchema schema() {
        return JsonSchema.of(schemaBuilder().toJson());
    }
""");
    out.println();
  }

  private static void writeSchemaBuilderMethodStart(PrintWriter out) {
    out.println(
        """
static ObjectSchemaBuilder schemaBuilder() {
    return objectSchema()
""");
    out.println();
  }

  private static SchemaGenerator handleComplexProperty(Property property) {
    String className = property.className();
    String name = property.name();
    boolean nullable = property.nullable();
    boolean notBlank = property.notBlank();

    if (className.startsWith("java.lang.String")) {
      return new StringSchemaGenerator(
          name, !nullable, notBlank, getSizeMin(property), getSizeMax(property));
    } else if (className.startsWith("java.lang.Boolean")) {
      return new BooleanSchemaGenerator(name, !nullable);
    } else if (className.startsWith("java.lang.Long")
        || className.startsWith("java.lang.Integer")) {
      return new IntegerSchemaGenerator(
          name, !nullable, getMinValue(property), getMaxValue(property));
    } else if (className.startsWith("java.lang.Double")) {
      return new NumberSchemaGenerator(
          name, !nullable, getMinValue(property), getMaxValue(property));
    } else if (className.equals("java.time.LocalDate")) {
      return new DateSchemaGenerator(name, !nullable);
    } else if (className.equals("java.time.LocalDateTime")
        || className.equals("java.time.OffsetDateTime")) {
      return new DateTimeSchemaGenerator(name, !nullable);
    } else if (isCollectionType(className)) {
      return handleCollectionProperty(property);
    }

    return new ObjectSchemaGenerator(name, !nullable, className);
  }

  private static boolean isCollectionType(String className) {
    return className.startsWith("java.util.Set")
        || className.startsWith("java.util.List")
        || className.startsWith("java.util.Collection");
  }

  private static SchemaGenerator handleCollectionProperty(Property property) {
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

    SchemaGenerator itemSchemaGenerator =
        createItemSchemaGenerator(property, notNull, notBlank, maybeSize, maybeMin, maybeMax);

    return new ArraySchemaGenerator(
        property.name(),
        !property.nullable(),
        true,
        itemSchemaGenerator,
        getSizeMin(property),
        getSizeMax(property));
  }

  private static SchemaGenerator createItemSchemaGenerator(
      Property property,
      boolean notNull,
      boolean notBlank,
      Optional<GenericParameterAnnotation.Size> maybeSize,
      Optional<GenericParameterAnnotation.Min> maybeMin,
      Optional<GenericParameterAnnotation.Max> maybeMax) {

    return switch (getGenericType(property.className())) {
      case "java.lang.String" ->
          new StringSchemaGenerator(
              null,
              notNull,
              notBlank,
              maybeSize.map(GenericParameterAnnotation.Size::min).orElse(null),
              maybeSize.map(GenericParameterAnnotation.Size::max).orElse(null));
      case "java.lang.Boolean" -> new BooleanSchemaGenerator(null, notNull);
      case "java.lang.Long", "java.lang.Integer", "java.lang.Short" ->
          new IntegerSchemaGenerator(
              null,
              notNull,
              maybeMin.isPresent() ? maybeMin.get().value() : null,
              maybeMax.isPresent() ? maybeMax.get().value() : null);
      case "java.lang.Float", "java.lang.Double" ->
          new NumberSchemaGenerator(
              null,
              notNull,
              maybeMin.isPresent() ? maybeMin.get().value() : null,
              maybeMax.isPresent() ? maybeMax.get().value() : null);
      default -> new ObjectSchemaGenerator(null, notNull, getGenericType(property.className()));
    };
  }

  private static SchemaGenerator handlePrimitiveProperty(Property property) {
    return switch (property.kind()) {
      case CHAR, BYTE -> new StringSchemaGenerator(property.name(), true, true, 1, 1);
      case LONG, INT, SHORT ->
          new IntegerSchemaGenerator(
              property.name(), true, getMinValue(property), getMaxValue(property));
      case DOUBLE, FLOAT ->
          new NumberSchemaGenerator(
              property.name(), true, getMinValue(property), getMaxValue(property));
      case BOOLEAN -> new BooleanSchemaGenerator(property.name(), true);
      default -> throw new GenerationException("Unsupported primitive type: " + property.kind());
    };
  }

  private static Integer getSizeMin(Property property) {
    return null == property.size() || property.size().min() == 0 ? null : property.size().min();
  }

  private static Integer getSizeMax(Property property) {
    return null == property.size() || property.size().max() == Integer.MAX_VALUE
        ? null
        : property.size().max();
  }

  private static Long getMinValue(Property property) {
    return null == property.min() ? null : property.min().value();
  }

  private static Long getMaxValue(Property property) {
    return null == property.max() ? null : property.max().value();
  }
}
