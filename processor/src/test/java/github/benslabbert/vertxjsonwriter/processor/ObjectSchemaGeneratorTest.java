/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ObjectSchemaGeneratorTest {

  static Stream<Arguments> testCases() {
    return Stream.of(
        // testBasicRequiredProperty
        Arguments.of(
            "Basic Required Property",
            new ObjectSchemaGenerator("testField", true, "TestObject"),
            ".requiredProperty(\"testField\", TestObject.schemaBuilder())"),

        // testBasicOptionalProperty
        Arguments.of(
            "Basic Optional Property",
            new ObjectSchemaGenerator("testField", false, "TestObject"),
            ".property(\"testField\", TestObject.schemaBuilder())"),

        // testNullFieldName
        Arguments.of(
            "Null Field Name",
            new ObjectSchemaGenerator(null, false, "TestObject"),
            "TestObject.schemaBuilder()"),

        // testDifferentObjectClassName
        Arguments.of(
            "Different Object Class Name",
            new ObjectSchemaGenerator("testField", true, "AnotherObject"),
            ".requiredProperty(\"testField\", AnotherObject.schemaBuilder())"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void testObjectSchemaGenerator(
      String testName, ObjectSchemaGenerator generator, String expected) {

    // Act
    String result = generator.print();

    // Assert
    assertThat(result).isEqualTo(expected);
  }
}
