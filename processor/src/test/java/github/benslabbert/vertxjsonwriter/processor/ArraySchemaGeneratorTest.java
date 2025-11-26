/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ArraySchemaGeneratorTest {

  static Stream<Arguments> testCases() {
    return Stream.of(
        // testBasicRequiredProperty
        Arguments.of(
            "Basic Required Property",
            new ArraySchemaGenerator(
                "testField",
                true,
                false,
                new StringSchemaGenerator(null, false, false, null, null),
                null,
                null),
            ".requiredProperty(\"testField\", arraySchema()\n.items(\nstringSchema()\n))"),

        // testBasicOptionalProperty
        Arguments.of(
            "Basic Optional Property",
            new ArraySchemaGenerator(
                "testField",
                false,
                false,
                new StringSchemaGenerator(null, false, false, null, null),
                null,
                null),
            ".property(\"testField\", arraySchema()\n.items(\nstringSchema()\n))"),

        // testUniqueElements
        Arguments.of(
            "Unique Elements",
            new ArraySchemaGenerator(
                "testField",
                false,
                true,
                new StringSchemaGenerator(null, false, false, null, null),
                null,
                null),
            ".property(\"testField\", arraySchema()\n"
                + ".items(\n"
                + "stringSchema()\n"
                + ").with(uniqueItems()))"),

        // testMinItems
        Arguments.of(
            "Min Items",
            new ArraySchemaGenerator(
                "testField",
                false,
                false,
                new StringSchemaGenerator(null, false, false, null, null),
                5,
                null),
            ".property(\"testField\", arraySchema()\n"
                + ".items(\n"
                + "stringSchema()\n"
                + ").with(minItems(5)))"),

        // testMaxItems
        Arguments.of(
            "Max Items",
            new ArraySchemaGenerator(
                "testField",
                false,
                false,
                new StringSchemaGenerator(null, false, false, null, null),
                null,
                10),
            ".property(\"testField\", arraySchema()\n"
                + ".items(\n"
                + "stringSchema()\n"
                + ").with(maxItems(10)))"),

        // testAllOptions
        Arguments.of(
            "All Options",
            new ArraySchemaGenerator(
                "testField",
                true,
                true,
                new IntegerSchemaGenerator(null, false, null, null),
                1,
                100),
            ".requiredProperty(\"testField\", arraySchema()\n"
                + ".items(\n"
                + "intSchema()\n"
                + ").with(uniqueItems()).with(minItems(1)).with(maxItems(100)))"),

        // testDifferentSchemaGenerator
        Arguments.of(
            "Different Schema Generator",
            new ArraySchemaGenerator(
                "testField",
                true,
                false,
                new ObjectSchemaGenerator(null, false, "TestObject"),
                null,
                null),
            ".requiredProperty(\"testField\", arraySchema()\n"
                + ".items(\n"
                + "TestObjectJson.schemaBuilder()\n"
                + "))"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void testArraySchemaGenerator(String testName, ArraySchemaGenerator generator, String expected) {

    // Act
    String result = generator.print();

    // Assert
    assertThat(result).isEqualTo(expected);
  }
}
