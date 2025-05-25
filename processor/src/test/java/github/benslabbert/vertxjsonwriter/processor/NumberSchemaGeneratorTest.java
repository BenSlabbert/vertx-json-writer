/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NumberSchemaGeneratorTest {

  static Stream<Arguments> testCases() {
    return Stream.of(
        // testBasicRequiredProperty
        Arguments.of(
            "Basic Required Property",
            new NumberSchemaGenerator("testField", true, null, null),
            ".requiredProperty(\"testField\", numberSchema())"),

        // testBasicOptionalProperty
        Arguments.of(
            "Basic Optional Property",
            new NumberSchemaGenerator("testField", false, null, null),
            ".property(\"testField\", numberSchema())"),

        // testWithMinimum
        Arguments.of(
            "With Minimum",
            new NumberSchemaGenerator("testField", false, 5L, null),
            ".property(\"testField\", numberSchema().with(minimum(5)))"),

        // testWithMaximum
        Arguments.of(
            "With Maximum",
            new NumberSchemaGenerator("testField", false, null, 10L),
            ".property(\"testField\", numberSchema().with(maximum(10)))"),

        // testWithMinimumAndMaximum
        Arguments.of(
            "With Minimum and Maximum",
            new NumberSchemaGenerator("testField", true, 5L, 10L),
            ".requiredProperty(\"testField\", numberSchema().with(minimum(5)).with(maximum(10)))"),

        // testNullFieldName
        Arguments.of(
            "Null Field Name",
            new NumberSchemaGenerator(null, false, null, null),
            "numberSchema()"),

        // testNullFieldNameWithConstraints
        Arguments.of(
            "Null Field Name With Constraints",
            new NumberSchemaGenerator(null, false, 5L, 10L),
            "numberSchema().with(minimum(5)).with(maximum(10))"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void testNumberSchemaGenerator(
      String testName, NumberSchemaGenerator generator, String expected) {

    // Act
    String result = generator.print();

    // Assert
    assertThat(result).isEqualTo(expected);
  }
}
