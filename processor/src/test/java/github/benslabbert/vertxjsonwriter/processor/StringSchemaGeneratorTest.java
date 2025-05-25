/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringSchemaGeneratorTest {

  static Stream<Arguments> testCases() {
    return Stream.of(
        // testBasicRequiredProperty
        Arguments.of(
            "Basic Required Property",
            new StringSchemaGenerator("testField", true, false, null, null),
            ".requiredProperty(\"testField\", stringSchema())"),

        // testBasicOptionalProperty
        Arguments.of(
            "Basic Optional Property",
            new StringSchemaGenerator("testField", false, false, null, null),
            ".property(\"testField\", stringSchema())"),

        // testNotBlank
        Arguments.of(
            "Not Blank",
            new StringSchemaGenerator("testField", true, true, null, null),
            ".requiredProperty(\"testField\","
                + " stringSchema().with(pattern(Pattern.compile(\".*\\\\S.*\"))))"),

        // testMinLength
        Arguments.of(
            "Min Length",
            new StringSchemaGenerator("testField", false, false, 5, null),
            ".property(\"testField\", stringSchema().with(minLength(5)))"),

        // testMaxLength
        Arguments.of(
            "Max Length",
            new StringSchemaGenerator("testField", false, false, null, 10),
            ".property(\"testField\", stringSchema().with(maxLength(10)))"),

        // testMinAndMaxLength
        Arguments.of(
            "Min and Max Length",
            new StringSchemaGenerator("testField", true, false, 5, 10),
            ".requiredProperty(\"testField\","
                + " stringSchema().with(minLength(5)).with(maxLength(10)))"),

        // testAllOptions
        Arguments.of(
            "All Options",
            new StringSchemaGenerator("testField", true, true, 5, 10),
            ".requiredProperty(\"testField\","
                + " stringSchema().with(pattern(Pattern.compile(\".*\\\\S.*\"))).with(minLength(5)).with(maxLength(10)))"),

        // testNullFieldName
        Arguments.of(
            "Null Field Name",
            new StringSchemaGenerator(null, false, false, null, null),
            "stringSchema()"),

        // testNullFieldNameWithConstraints
        Arguments.of(
            "Null Field Name With Constraints",
            new StringSchemaGenerator(null, false, false, 5, 10),
            "stringSchema().with(minLength(5)).with(maxLength(10))"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void testStringSchemaGenerator(
      String testName, StringSchemaGenerator generator, String expected) {

    // Act
    String result = generator.print();

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testNotBlankWithoutRequired() {
    // Arrange
    StringSchemaGenerator generator =
        new StringSchemaGenerator("testField", false, true, null, null);

    // Act & Assert
    GenerationException exception =
        assertThrows(
            GenerationException.class,
            generator::print,
            "Expected print() to throw GenerationException, but it didn't");

    assertThat(exception.getMessage())
        .isEqualTo("field 'testField' cannot be NotBlank if not required");
  }
}
