/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import static github.benslabbert.vertxjsonwriter.processor.Util.getGenericType;

import io.vertx.core.json.JsonObject;
import java.io.PrintWriter;
import java.util.List;

final class ToJsonGenerator {

  private ToJsonGenerator() {}

  static void toJson(PrintWriter out, List<Property> properties, String simpleClassName) {
    simpleClassName = simpleClassName.replace('_', '.');
    out.printf("public static JsonObject toJson(%s %s) {%n", simpleClassName, "o");
    out.println("JsonObject json = new JsonObject();");

    JsonObject entries = new JsonObject();
    entries.toBuffer();

    for (Property property : properties) {
      String className = property.className();
      String name = property.name();

      if (!property.isComplex()) {
        out.printf("json.put(\"%s\", o.%s());%n", name, name);
        continue;
      }
      if (className.startsWith("java.lang.")) {
        out.printf("json.put(\"%s\", o.%s());%n", name, name);
      } else if (className.startsWith("java.time.")) {
        timeToJson(out, property.name(), property.className());
      } else if (className.startsWith("java.util.Set")) {
        iterableToJson(out, property.name(), property.className());
      } else if (className.startsWith("java.util.List")) {
        iterableToJson(out, property.name(), property.className());
      } else if (className.startsWith("java.util.Collection")) {
        iterableToJson(out, property.name(), property.className());
      } else {
        out.printf("json.put(\"%s\", o.%s().toJson());%n", name, name);
      }
    }

    out.println("return json;");
    out.println("}");
    out.println();
  }

  private static void timeToJson(PrintWriter out, String name, String className) {
    switch (className) {
      case "java.time.LocalDate" ->
          out.printf("json.put(\"%s\", o.%s().format(DateTimeFormatter.ISO_DATE));%n", name, name);
      case "java.time.LocalDateTime" ->
          out.printf(
              "json.put(\"%s\", o.%s().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));%n",
              name, name);
      case "java.time.OffsetDateTime" ->
          out.printf(
              "json.put(\"%s\", o.%s().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));%n",
              name, name);
      default -> throw new GenerationException("Unsupported class: " + className);
    }
  }

  private static void iterableToJson(PrintWriter out, String name, String className) {
    out.printf("JsonArray %s = new JsonArray();%n", name);
    out.printf("for (var i : o.%s()) {%n", name);

    String genericType = getGenericType(className);
    if (genericType.startsWith("java.lang.")) {
      out.printf("%s.add(i);%n", name);
    } else {
      out.printf("%s.add(i.toJson());%n", name);
    }

    out.println("}");
    out.printf("json.put(\"%s\", %s);%n", name, name);
    out.println();
  }
}
