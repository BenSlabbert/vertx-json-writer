/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

record DateTimeSchemaGenerator(String fieldName, boolean required) implements SchemaGenerator {

  static DateTimeSchemaGenerator create(Property property) {
    String name = property.name();
    boolean nullable = property.nullable();
    return new DateTimeSchemaGenerator(name, !nullable);
  }

  @Override
  public String print() {
    String schema = required ? ".requiredProperty(" : ".property(";
    schema += "\"%s\", stringSchema().with(format(StringFormat.DATETIME))".formatted(fieldName);
    return schema + ")";
  }
}
