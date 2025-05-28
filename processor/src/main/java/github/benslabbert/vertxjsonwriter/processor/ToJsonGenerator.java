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
    out.printf("\tpublic static JsonObject toJson(%s %s) {%n", simpleClassName, "o");
    out.println("\t\tJsonObject json = new JsonObject();");

    JsonObject entries = new JsonObject();
    entries.toBuffer();

    for (Property property : properties) {
      if (!property.isComplex()) {
        out.printf("\t\tjson.put(\"%s\", o.%s());%n", property.name(), property.name());
      } else {
        if (property.className().startsWith("java.lang.")) {
          out.printf("\t\tjson.put(\"%s\", o.%s());%n", property.name(), property.name());
        } else if (property.className().startsWith("java.time.")) {
          timeToJson(out, property);
        } else if (property.className().startsWith("java.util.Set")) {
          iterableToJson(out, property);
        } else if (property.className().startsWith("java.util.List")) {
          iterableToJson(out, property);
        } else if (property.className().startsWith("java.util.Collection")) {
          iterableToJson(out, property);
        } else {
          out.printf("\t\tjson.put(\"%s\", o.%s().toJson());%n", property.name(), property.name());
        }
      }
    }

    out.println("\t\treturn json;");
    out.println("\t}");
    out.println();
  }

  private static void timeToJson(PrintWriter out, Property property) {
    switch (property.className()) {
      case "java.time.LocalDate" ->
          out.printf(
              "\t\tjson.put(\"%s\", o.%s().format(DateTimeFormatter.ISO_DATE));%n",
              property.name(), property.name());
      case "java.time.LocalDateTime" ->
          out.printf(
              "\t\tjson.put(\"%s\", o.%s().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));%n",
              property.name(), property.name());
      case "java.time.OffsetDateTime" ->
          out.printf(
              "\t\tjson.put(\"%s\", o.%s().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));%n",
              property.name(), property.name());
      default -> throw new IllegalArgumentException("Unsupported class: " + property.className());
    }
  }

  private static void iterableToJson(PrintWriter out, Property property) {
    out.printf("\t\tJsonArray %s = new JsonArray();%n", property.name());
    out.printf("\t\tfor (var i : o.%s()) {%n", property.name());

    if (!property.isComplex()) {
      out.printf("\t\t\t%s.add(i);%n", property.name());
    } else {
      String genericType = getGenericType(property.className());
      if (genericType.startsWith("java.lang.")) {
        out.printf("\t\t\t%s.add(i);%n", property.name());
      } else {
        out.printf("\t\t\t%s.add(i.toJson());%n", property.name());
      }
    }

    out.println("\t\t}");
    out.printf("\t\tjson.put(\"%s\", %s);%n", property.name(), property.name());
    out.println();
  }
}
