/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

record DateTimeSchemaGenerator(String fieldName, boolean required) implements SchemaGenerator {

  @Override
  public String print() {
    String schema = required ? ".requiredProperty(" : ".property(";
    schema += "\"%s\", stringSchema().with(format(StringFormat.DATETIME))".formatted(fieldName);
    return schema + ")";
  }
}
