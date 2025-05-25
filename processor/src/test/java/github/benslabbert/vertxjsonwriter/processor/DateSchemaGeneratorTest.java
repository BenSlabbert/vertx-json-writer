/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateSchemaGeneratorTest {

  static Stream<Arguments> testCases() {
    return Stream.of(
        // testBasicRequiredProperty
        Arguments.of(
            "Basic Required Property",
            new DateSchemaGenerator("testField", true),
            ".requiredProperty(\"testField\", stringSchema().with(format(StringFormat.DATE)))"),

        // testBasicOptionalProperty
        Arguments.of(
            "Basic Optional Property",
            new DateSchemaGenerator("testField", false),
            ".property(\"testField\", stringSchema().with(format(StringFormat.DATE)))"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void testDateSchemaGenerator(String testName, DateSchemaGenerator generator, String expected) {

    // Act
    String result = generator.print();

    // Assert
    assertThat(result).isEqualTo(expected);
  }
}
