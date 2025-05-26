/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.example.schema;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.OutputUnit;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class RequestTest {

  static Stream<String> validJsonProvider() {
    return Stream.of(
        // All fields
        """
        {
          "data": "test123",
          "other": "optional text",
          "integer": 5,
          "number": 50.5,
          "bool": true,
          "localDate": "2024-01-15",
          "localDateTime": "2024-01-15T10:30:00",
          "offsetDateTime": "2024-01-15T10:30:00+02:00",
          "ages": [3, 5, 8, 12],
          "tags": ["tag1", "tag2", "tag3"],
          "description": "Sample description"
        }
        """,
        // Only mandatory fields
        """
        {
          "data": "test123",
          "integer": 5,
          "number": 50.5
        }
        """);
  }

  static Stream<TestCase> invalidJsonProvider() {
    return Stream.of(
        // Missing required fields
        new TestCase(
            "Missing data field",
            """
            {
              "integer": 5,
              "number": 50.5
            }
            """,
            "data"),
        new TestCase(
            "Missing integer field",
            """
            {
              "data": "test123",
              "number": 50.5
            }
            """,
            "integer"),
        new TestCase(
            "Missing number field",
            """
            {
              "data": "test123",
              "integer": 5
            }
            """,
            "number"),

        // Field validation errors - data field
        new TestCase(
            "Data too short",
            """
            {
              "data": "a",
              "integer": 5,
              "number": 50.5
            }
            """,
            "data"),
        new TestCase(
            "Data too long",
            """
            {
              "data": "thisistoolong",
              "integer": 5,
              "number": 50.5
            }
            """,
            "data"),
        new TestCase(
            "Data is blank",
            """
            {
              "data": "",
              "integer": 5,
              "number": 50.5
            }
            """,
            "data"),

        // Field validation errors - other field
        new TestCase(
            "Other too long",
            """
{
  "data": "test123",
  "other": "This is a very long string that exceeds the maximum length of 66 characters allowed for this field",
  "integer": 5,
  "number": 50.5
}
""",
            "other"),

        // Field validation errors - integer field
        new TestCase(
            "Integer too small",
            """
            {
              "data": "test123",
              "integer": 0,
              "number": 50.5
            }
            """,
            "integer"),
        new TestCase(
            "Integer too large",
            """
            {
              "data": "test123",
              "integer": 11,
              "number": 50.5
            }
            """,
            "integer"),

        // Field validation errors - number field
        new TestCase(
            "Number too small",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 19.9
            }
            """,
            "number"),
        new TestCase(
            "Number too large",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 100.1
            }
            """,
            "number"),

        // Field validation errors - date fields
        new TestCase(
            "Invalid localDate format",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 50.5,
              "localDate": "not-a-date"
            }
            """,
            "localDate"),
        new TestCase(
            "Invalid localDateTime format",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 50.5,
              "localDateTime": "not-a-datetime"
            }
            """,
            "localDateTime"),
        new TestCase(
            "Invalid offsetDateTime format",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 50.5,
              "offsetDateTime": "not-an-offset-datetime"
            }
            """,
            "offsetDateTime"),

        // Field validation errors - ages collection
        new TestCase(
            "Empty ages collection",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 50.5,
              "ages": []
            }
            """,
            "ages"),
        new TestCase(
            "Ages with value too small",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 50.5,
              "ages": [1, 5, 8]
            }
            """,
            "ages"),
        new TestCase(
            "Ages with value too large",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 50.5,
              "ages": [3, 14, 8]
            }
            """,
            "ages"),

        // Field validation errors - tags collection
        new TestCase(
            "Tags collection too small",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 50.5,
              "tags": ["tag1"]
            }
            """,
            "tags"),
        new TestCase(
            "Tags with value too short",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 50.5,
              "tags": ["tag1", "ab"]
            }
            """,
            "tags"),
        new TestCase(
            "Tags with blank value",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": 50.5,
              "tags": ["tag1", ""]
            }
            """,
            "tags"),

        // Field validation errors - description field
        new TestCase(
            "Description too long",
            """
{
  "data": "test123",
  "integer": 5,
  "number": 50.5,
  "description": "This description is too long and exceeds the maximum allowed length of 25 characters"
}
""",
            "description"),

        // Type validation errors
        new TestCase(
            "Wrong type for data",
            """
            {
              "data": 123,
              "integer": 5,
              "number": 50.5
            }
            """,
            "data"),
        new TestCase(
            "Wrong type for integer",
            """
            {
              "data": "test123",
              "integer": "not-an-integer",
              "number": 50.5
            }
            """,
            "integer"),
        new TestCase(
            "Wrong type for number",
            """
            {
              "data": "test123",
              "integer": 5,
              "number": "not-a-number"
            }
            """,
            "number"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validJsonProvider")
  void validateValidJsonSchema(String json) {
    JsonObject jsonObject = new JsonObject(json);
    OutputUnit validate = Request.getValidator().validate(jsonObject);
    assertThat(validate.getValid()).isTrue();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidJsonProvider")
  void validateInvalidJsonSchema(TestCase testCase) {
    JsonObject jsonObject = new JsonObject(testCase.json());
    OutputUnit validate = Request.getValidator().validate(jsonObject);

    // Use the TestCase's validate method to perform assertions
    testCase.validate(validate);
  }

  record TestCase(String description, String json, String expectedErrorField) {

    @Override
    public String toString() {
      return description;
    }

    void validate(OutputUnit validate) {
      // Assert that validation fails
      assertThat(validate.getValid()).isFalse();

      // Assert that the expected field has an error
      assertThat(validate.getErrors()).isNotEmpty();

      // Check for errors based on the test case type
      boolean errorFound = false;
      String expectedField = expectedErrorField();
      String description = description();

      for (var error : validate.getErrors()) {
        // Check if this error is related to our expected field
        String instanceLocation = error.getInstanceLocation();
        String keywordLocation = error.getKeywordLocation();

        // For missing required fields
        if (description.startsWith("Missing")) {
          if (keywordLocation.contains("required")) {
            errorFound = true;
            break;
          }
        }
        // For other validation errors
        else if (instanceLocation.contains(expectedField)) {
          errorFound = true;
          break;
        }
      }

      // Assert that we found an error related to the expected field
      assertThat(errorFound)
          .as(
              "Expected to find an error related to field '%s' in test case '%s'",
              expectedField, description)
          .isTrue();
    }
  }
}
