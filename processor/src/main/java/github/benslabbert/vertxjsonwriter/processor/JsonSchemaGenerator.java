/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import static github.benslabbert.vertxjsonwriter.processor.Util.getGenericType;

import java.io.PrintWriter;
import java.util.List;

class JsonSchemaGenerator {

  private JsonSchemaGenerator() {}

  static void jsonSchema(PrintWriter out, List<Property> properties) {
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
    out.println(
        """
    static JsonSchema schema() {
        return JsonSchema.of(schemaBuilder().toJson());
    }
""");
    out.println();
    out.println(
        """
static ObjectSchemaBuilder schemaBuilder() {
    return objectSchema()
""");

    for (Property property : properties) {
      if (property.isComplex()) {
        if (property.className().startsWith("java.lang.String")) {
          SchemaGenerator schemaGenerator =
              new StringSchemaGenerator(
                  property.name(),
                  !property.nullable(),
                  property.notBlank(),
                  null == property.size() || property.size().min() == 0
                      ? null
                      : property.size().min(),
                  null == property.size() || property.size().max() == Integer.MAX_VALUE
                      ? null
                      : property.size().max());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.lang.Boolean")) {
          SchemaGenerator schemaGenerator =
              new BooleanSchemaGenerator(property.name(), !property.nullable());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.lang.Long")) {
          SchemaGenerator schemaGenerator =
              new IntegerSchemaGenerator(
                  property.name(),
                  !property.nullable(),
                  null == property.min() ? null : property.min().value(),
                  null == property.max() ? null : property.max().value());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.lang.Integer")) {
          SchemaGenerator schemaGenerator =
              new IntegerSchemaGenerator(
                  property.name(),
                  !property.nullable(),
                  null == property.min() ? null : property.min().value(),
                  null == property.max() ? null : property.max().value());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.lang.Double")) {
          SchemaGenerator schemaGenerator =
              new NumberSchemaGenerator(
                  property.name(),
                  !property.nullable(),
                  null == property.min() ? null : property.min().value(),
                  null == property.max() ? null : property.max().value());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().equals("java.time.LocalDate")) {
          SchemaGenerator schemaGenerator =
              new DateSchemaGenerator(property.name(), !property.nullable());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().equals("java.time.LocalDateTime")) {
          SchemaGenerator schemaGenerator =
              new DateTimeSchemaGenerator(property.name(), !property.nullable());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().equals("java.time.OffsetDateTime")) {
          SchemaGenerator schemaGenerator =
              new DateTimeSchemaGenerator(property.name(), !property.nullable());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.util.Set")
            || property.className().startsWith("java.util.List")
            || property.className().startsWith("java.util.Collection")) {
          List<GenericParameterAnnotation> gpa = property.genericParameterAnnotations();
          var notNull = gpa.stream().anyMatch(f -> f instanceof GenericParameterAnnotation.NotNull);
          var notBlank =
              gpa.stream().anyMatch(f -> f instanceof GenericParameterAnnotation.NotBlank);
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
              switch (getGenericType(property.className())) {
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
                default ->
                    new ObjectSchemaGenerator(null, notNull, getGenericType(property.className()));
              };
          SchemaGenerator schemaGenerator =
              new ArraySchemaGenerator(
                  property.name(),
                  !property.nullable(),
                  true,
                  itemSchemaGenerator,
                  null == property.size() || property.size().min() == 0
                      ? null
                      : property.size().min(),
                  null == property.size() || property.size().max() == Integer.MAX_VALUE
                      ? null
                      : property.size().max());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else {
          SchemaGenerator schemaGenerator =
              new ObjectSchemaGenerator(
                  property.name(), !property.nullable(), property.className());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        }
      } else {
        SchemaGenerator schemaGenerator =
            switch (property.kind()) {
              case CHAR, BYTE -> new StringSchemaGenerator(property.name(), true, true, 1, 1);
              case LONG, INT, SHORT ->
                  new IntegerSchemaGenerator(
                      property.name(),
                      true,
                      null == property.min() ? null : property.min().value(),
                      null == property.max() ? null : property.max().value());
              case DOUBLE, FLOAT ->
                  new NumberSchemaGenerator(
                      property.name(),
                      true,
                      null == property.min() ? null : property.min().value(),
                      null == property.max() ? null : property.max().value());
              case BOOLEAN -> new BooleanSchemaGenerator(property.name(), true);
              default ->
                  throw new GenerationException("Unsupported primitive type: " + property.kind());
            };
        String print = schemaGenerator.print();
        out.println(print);
        out.println();
      }
    }

    out.println("\t;");
    out.println("\t}");
  }
}
