/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BooleanSchemaGeneratorTest {

  static Stream<Arguments> testCases() {
    return Stream.of(
        // testBasicRequiredProperty
        Arguments.of(
            "Basic Required Property",
            new BooleanSchemaGenerator("testField", true),
            ".requiredProperty(\"testField\", booleanSchema())"),

        // testBasicOptionalProperty
        Arguments.of(
            "Basic Optional Property",
            new BooleanSchemaGenerator("testField", false),
            ".property(\"testField\", booleanSchema())"),

        // testNullFieldName
        Arguments.of(
            "Null Field Name", new BooleanSchemaGenerator(null, false), "booleanSchema()"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void testBooleanSchemaGenerator(
      String testName, BooleanSchemaGenerator generator, String expected) {

    // Act
    String result = generator.print();

    // Assert
    assertThat(result).isEqualTo(expected);
  }
}
