/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import jakarta.annotation.Nullable;

record ArraySchemaGenerator(
    String fieldName,
    boolean required,
    boolean uniqueElements,
    SchemaGenerator schemaGenerator,
    @Nullable Integer minItems,
    @Nullable Integer maxItems)
    implements SchemaGenerator {

  @Override
  public String print() {
    String schema = required ? ".requiredProperty(" : ".property(";
    schema += "\"%s\", arraySchema()".formatted(fieldName);

    schema += "%n.items(%n%s%n)".formatted(schemaGenerator.print());

    if (uniqueElements) {
      schema += ".with(uniqueItems())";
    }

    if (null != minItems) {
      schema += ".with(minItems(%d))".formatted(minItems);
    }

    if (null != maxItems) {
      schema += ".with(maxItems(%d))".formatted(maxItems);
    }

    return schema + ")";
  }
}
