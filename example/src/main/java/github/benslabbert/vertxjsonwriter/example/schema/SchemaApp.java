/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.example.schema;

import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.OutputErrorType;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.Validator;
import java.util.List;

public class SchemaApp {

  public static void main(String[] args) {
    Validator validator = Request.getValidator();
    OutputUnit outputUnit =
        validator.validate(
            new JsonObject().put("data", "data").put("count", 1).put("tags", List.of("tag1")));
    Boolean valid = outputUnit.getValid();
    List<OutputUnit> errors = outputUnit.getErrors();
    OutputErrorType errorType = outputUnit.getErrorType();
    System.err.println(valid);
    System.err.println(errorType);
    System.err.println(errors);
  }
}
