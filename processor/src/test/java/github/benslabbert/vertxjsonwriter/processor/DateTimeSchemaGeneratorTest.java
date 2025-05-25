/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateTimeSchemaGeneratorTest {

  static Stream<Arguments> testCases() {
    return Stream.of(
        // testBasicRequiredProperty
        Arguments.of(
            "Basic Required Property",
            new DateTimeSchemaGenerator("testField", true),
            ".requiredProperty(\"testField\", stringSchema().with(format(StringFormat.DATETIME)))"),

        // testBasicOptionalProperty
        Arguments.of(
            "Basic Optional Property",
            new DateTimeSchemaGenerator("testField", false),
            ".property(\"testField\", stringSchema().with(format(StringFormat.DATETIME)))"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void testDateTimeSchemaGenerator(
      String testName, DateTimeSchemaGenerator generator, String expected) {

    // Act
    String result = generator.print();

    // Assert
    assertThat(result).isEqualTo(expected);
  }
}
