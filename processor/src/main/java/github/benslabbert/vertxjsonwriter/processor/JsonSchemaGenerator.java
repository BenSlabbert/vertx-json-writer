/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import java.io.PrintWriter;
import java.util.List;

class JsonSchemaGenerator {

  private JsonSchemaGenerator() {}

  static void jsonSchema(PrintWriter out, List<Property> properties) {
    writeValidatorMethod(out);
    writeSchemaMethod(out);
    writeSchemaBuilderMethodStart(out);

    for (Property property : properties) {
      SchemaGenerator schemaGenerator;
      if (property.isComplex()) {
        schemaGenerator = ComplexJsonSchemaGenerator.handleComplexProperty(property);
      } else {
        schemaGenerator = handlePrimitiveProperty(property);
      }
      out.println(schemaGenerator.print());
      out.println();
    }

    out.println(";");
    out.println("}");
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

  private static SchemaGenerator handlePrimitiveProperty(Property property) {
    return switch (property.kind()) {
      case CHAR, BYTE -> new StringSchemaGenerator(property.name(), true, true, 1, 1);
      case LONG, INT, SHORT ->
          new IntegerSchemaGenerator(
              property.name(), true, property.getMinValue(), property.getMaxValue());
      case DOUBLE, FLOAT ->
          new NumberSchemaGenerator(
              property.name(), true, property.getMinValue(), property.getMaxValue());
      case BOOLEAN -> new BooleanSchemaGenerator(property.name(), true);
      default -> throw new GenerationException("Unsupported primitive type: " + property.kind());
    };
  }
}
