/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import jakarta.annotation.Nullable;

record ObjectSchemaGenerator(@Nullable String fieldName, boolean required, String objectClassName)
    implements SchemaGenerator {

  @Override
  public String print() {
    if (null == fieldName) {
      return "%s.schemaBuilder()".formatted(objectClassName);
    }

    String schema = required ? ".requiredProperty(" : ".property(";
    return schema + "\"%s\", %s.schemaBuilder())".formatted(fieldName, objectClassName);
  }
}
