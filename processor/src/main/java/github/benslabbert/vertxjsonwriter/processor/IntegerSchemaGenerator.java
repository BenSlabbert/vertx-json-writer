/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import jakarta.annotation.Nullable;

record IntegerSchemaGenerator(
    @Nullable String fieldName, boolean required, @Nullable Long min, @Nullable Long max)
    implements SchemaGenerator {

  static IntegerSchemaGenerator create(Property property) {
    String name = property.name();
    boolean nullable = property.nullable();
    return new IntegerSchemaGenerator(
        name, !nullable, property.getMinValue(), property.getMaxValue());
  }

  @Override
  public String print() {
    String schema;

    if (null == fieldName) {
      schema = "intSchema()";
    } else {
      schema = required ? ".requiredProperty(" : ".property(";
      schema += "\"%s\", intSchema()".formatted(fieldName);
    }

    schema = applyMin(schema);
    schema = applyMax(schema);

    return schema + (null == fieldName ? "" : ")");
  }

  private String applyMax(String schema) {
    if (null != max) {
      schema += ".with(maximum(%d))".formatted(max);
    }
    return schema;
  }

  private String applyMin(String schema) {
    if (null != min) {
      schema += ".with(minimum(%d))".formatted(min);
    }
    return schema;
  }
}
