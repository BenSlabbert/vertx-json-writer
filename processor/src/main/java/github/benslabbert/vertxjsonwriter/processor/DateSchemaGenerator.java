/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

record DateSchemaGenerator(String fieldName, boolean required) implements SchemaGenerator {

  static DateSchemaGenerator create(Property property) {
    String name = property.name();
    boolean nullable = property.nullable();
    return new DateSchemaGenerator(name, !nullable);
  }

  @Override
  public String print() {
    String schema = required ? ".requiredProperty(" : ".property(";
    schema += "\"%s\", stringSchema().with(format(StringFormat.DATE))".formatted(fieldName);
    return schema + ")";
  }
}
