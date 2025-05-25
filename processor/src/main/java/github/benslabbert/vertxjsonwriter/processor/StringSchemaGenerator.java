/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import jakarta.annotation.Nullable;

record StringSchemaGenerator(
    @Nullable String fieldName,
    boolean required,
    boolean notBlank,
    @Nullable Integer minLength,
    @Nullable Integer maxLength)
    implements SchemaGenerator {

  @Override
  public String print() {
    if (!required && notBlank) {
      throw new GenerationException(
          "field '%s' cannot be NotBlank if not required".formatted(fieldName));
    }

    String schema;

    if (null == fieldName) {
      schema = "stringSchema()";
    } else {
      schema = required ? ".requiredProperty(" : ".property(";
      schema += "\"%s\", stringSchema()".formatted(fieldName);
    }

    schema = applyNotBlank(schema);
    schema = applyMinLength(schema);
    schema = applyMaxLength(schema);

    return schema + (null == fieldName ? "" : ")");
  }

  private String applyMaxLength(String schema) {
    if (null != maxLength) {
      schema += ".with(maxLength(%d))".formatted(maxLength);
    }
    return schema;
  }

  private String applyMinLength(String schema) {
    if (null != minLength) {
      schema += ".with(minLength(%d))".formatted(minLength);
    }
    return schema;
  }

  private String applyNotBlank(String schema) {
    if (notBlank) {
      schema += ".with(pattern(Pattern.compile(\".*\\\\S.*\")))";
    }
    return schema;
  }
}
