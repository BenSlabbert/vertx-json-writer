/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import static github.benslabbert.vertxjsonwriter.processor.Util.getGenericType;

import java.io.PrintWriter;
import java.util.List;

class FromJsonGenerator {

  private FromJsonGenerator() {}

  static void fromJson(PrintWriter out, List<Property> properties, String simpleClassName) {
    simpleClassName = simpleClassName.replace('_', '.');
    out.printf("\tpublic static %s fromJson(JsonObject json) {%n", simpleClassName);
    out.printf("\t\treturn %s.builder()%n", simpleClassName);

    for (Property property : properties) {
      String jsonGetter = getJsonGetter(property);
      out.printf("\t\t\t.%s(%s)%n", property.name(), jsonGetter);
    }

    out.println("\t\t\t.build();");
    out.println("\t}");
    out.println();
  }

  private static String getJsonGetter(Property property) {
    if (!property.isComplex()) {
      return switch (property.kind()) {
        case BOOLEAN -> "json.getBoolean(\"%s\")".formatted(property.name());
        case INT -> "json.getInteger(\"%s\")".formatted(property.name());
        case LONG -> "json.getLong(\"%s\")".formatted(property.name());
        case FLOAT -> "json.getFloat(\"%s\")".formatted(property.name());
        case DOUBLE -> "json.getDouble(\"%s\")".formatted(property.name());
        case SHORT -> "json.getNumber(\"%s\").shortValue()".formatted(property.name());
        case CHAR -> "json.getString(\"%s\").charAt(0)".formatted(property.name());
        case BYTE -> "json.getBinary(\"%s\")[0]".formatted(property.name());
        default ->
            throw new IllegalArgumentException("Unsupported primitive type: " + property.kind());
      };
    }

    if (property.className().startsWith("java.util.Set")
        || property.className().startsWith("java.util.List")
        || property.className().startsWith("java.util.Collection")) {

      String collector = "toList()";
      if (property.className().startsWith("java.util.Set")) {
        collector = "collect(Collectors.toSet())";
      }

      return switch (getGenericType(property.className())) {
        // if the generic type is a java type we cast
        // if it is something else, we get a JsonObject type and call a from json class and
        // collect it
        case "java.lang.String" ->
            "json.getJsonArray(\"%s\").stream().map(s -> (String) s).%s"
                .formatted(property.name(), collector);
        case "java.lang.Boolean" ->
            "json.getJsonArray(\"%s\").stream().map(b -> (Boolean) b).%s"
                .formatted(property.name(), collector);
        case "java.lang.Integer" ->
            "json.getJsonArray(\"%s\").stream().map(i -> (Integer) i).%s"
                .formatted(property.name(), collector);
        case "java.lang.Long" ->
            "json.getJsonArray(\"%s\").stream().map(l -> (Long) l).%s"
                .formatted(property.name(), collector);
        case "java.lang.Float" ->
            "json.getJsonArray(\"%s\").stream().map(f -> (Float) f).%s"
                .formatted(property.name(), collector);
        case "java.lang.Double" ->
            "json.getJsonArray(\"%s\").stream().map(d -> (Double) d).%s"
                .formatted(property.name(), collector);
        default ->
            "json.getJsonArray(\"%s\").stream().map(obj -> %s.fromJson((JsonObject) obj)).%s"
                .formatted(
                    property.name(),
                    getSimpleName(getGenericType(property.className())),
                    collector);
      };
    }

    if (property.className().startsWith("java.lang.")) {
      return switch (property.className()) {
        case "java.lang.String" -> "json.getString(\"%s\")".formatted(property.name());
        case "java.lang.Boolean" -> "json.getBoolean(\"%s\")".formatted(property.name());
        case "java.lang.Integer" -> "json.getInteger(\"%s\")".formatted(property.name());
        case "java.lang.Long" -> "json.getLong(\"%s\")".formatted(property.name());
        case "java.lang.Float" -> "json.getFloat(\"%s\")".formatted(property.name());
        case "java.lang.Double" -> "json.getDouble(\"%s\")".formatted(property.name());
        default ->
            throw new IllegalArgumentException(
                "Unsupported java.lang.* type: " + property.className());
      };
    }

    if (property.className().startsWith("java.time.")) {
      return switch (property.className()) {
        case "java.time.LocalDate" ->
            "LocalDate.parse(json.getString(\"%s\"), DateTimeFormatter.ISO_DATE)"
                .formatted(property.name());
        case "java.time.LocalDateTime" ->
            "LocalDateTime.parse(json.getString(\"%s\"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)"
                .formatted(property.name());
        case "java.time.OffsetDateTime" ->
            "OffsetDateTime.parse(json.getString(\"%s\"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)"
                .formatted(property.name());
        default ->
            throw new IllegalArgumentException(
                "Unsupported java.time.* type: " + property.className());
      };
    }

    // if this is an inner class we need to fix the name
    return "%s.fromJson(json.getJsonObject(\"%s\"))"
        .formatted(simpleName(property.className()), property.name());
  }

  private static String simpleName(String classname) {
    // my.test.Nested.Inner
    // if this is an inner type, return Nested.Inner
    int firstClassIdx = 0;
    for (int i = 0; i < classname.length(); i++) {
      if (Character.isUpperCase(classname.charAt(i))) {
        firstClassIdx = i;
        break;
      }
    }
    return classname.substring(firstClassIdx);
  }

  private static String getSimpleName(String canonicalName) {
    return canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
  }
}
