/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IntegerSchemaGeneratorTest {

  static Stream<Arguments> testCases() {
    return Stream.of(
        // testBasicRequiredProperty
        Arguments.of(
            "Basic Required Property",
            new IntegerSchemaGenerator("testField", true, null, null),
            ".requiredProperty(\"testField\", intSchema())"),

        // testBasicOptionalProperty
        Arguments.of(
            "Basic Optional Property",
            new IntegerSchemaGenerator("testField", false, null, null),
            ".property(\"testField\", intSchema())"),

        // testWithMinimum
        Arguments.of(
            "With Minimum",
            new IntegerSchemaGenerator("testField", false, 5L, null),
            ".property(\"testField\", intSchema().with(minimum(5)))"),

        // testWithMaximum
        Arguments.of(
            "With Maximum",
            new IntegerSchemaGenerator("testField", false, null, 10L),
            ".property(\"testField\", intSchema().with(maximum(10)))"),

        // testWithMinimumAndMaximum
        Arguments.of(
            "With Minimum and Maximum",
            new IntegerSchemaGenerator("testField", true, 5L, 10L),
            ".requiredProperty(\"testField\", intSchema().with(minimum(5)).with(maximum(10)))"),

        // testNullFieldName
        Arguments.of(
            "Null Field Name", new IntegerSchemaGenerator(null, false, null, null), "intSchema()"),

        // testNullFieldNameWithConstraints
        Arguments.of(
            "Null Field Name With Constraints",
            new IntegerSchemaGenerator(null, false, 5L, 10L),
            "intSchema().with(minimum(5)).with(maximum(10))"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void testIntegerSchemaGenerator(
      String testName, IntegerSchemaGenerator generator, String expected) {

    // Act
    String result = generator.print();

    // Assert
    assertThat(result).isEqualTo(expected);
  }
}
