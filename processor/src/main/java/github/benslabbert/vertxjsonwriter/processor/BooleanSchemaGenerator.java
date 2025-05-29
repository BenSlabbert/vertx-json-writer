/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import jakarta.annotation.Nullable;

record BooleanSchemaGenerator(@Nullable String fieldName, boolean required)
    implements SchemaGenerator {

  static BooleanSchemaGenerator create(Property property) {
    String name = property.name();
    boolean nullable = property.nullable();
    return new BooleanSchemaGenerator(name, !nullable);
  }

  @Override
  public String print() {
    if (null == fieldName) {
      return "booleanSchema()";
    }

    String schema = required ? ".requiredProperty(" : ".property(";
    schema += "\"%s\", booleanSchema()".formatted(fieldName);
    return schema + ")";
  }
}
