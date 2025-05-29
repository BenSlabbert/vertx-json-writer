/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

import static github.benslabbert.vertxjsonwriter.processor.Util.getGenericType;

import java.io.PrintWriter;
import java.util.List;
import javax.lang.model.type.TypeKind;

class FromJsonGenerator {

  private FromJsonGenerator() {}

  static void fromJson(PrintWriter out, List<Property> properties, String simpleClassName) {
    simpleClassName = simpleClassName.replace('_', '.');
    out.printf("public static %s fromJson(JsonObject json) {%n", simpleClassName);
    out.println("if (null == json) {");
    out.println("return null;");
    out.println("}");
    out.printf("return %s.builder()%n", simpleClassName);

    for (Property property : properties) {
      String jsonGetter =
          getJsonGetter(
              property.name(),
              property.kind(),
              property.className(),
              property.isComplex(),
              property.nullable());
      out.printf(".%s(%s)%n", property.name(), jsonGetter);
    }

    out.println(".build();");
    out.println("}");
    out.println();
  }

  private static String getJsonGetter(
      String name, TypeKind kind, String className, boolean isComplex, boolean nullable) {

    if (!isComplex) {
      return primitiveGetter(kind, name);
    }

    if (className.startsWith("java.util.Set")
        || className.startsWith("java.util.List")
        || className.startsWith("java.util.Collection")) {

      return collectionGetter(name, className);
    }

    if (className.startsWith("java.lang.")) {
      return javaLangGetter(name, className);
    }

    if (className.startsWith("java.time.")) {
      return timeGetter(name, className, nullable);
    }

    // if this is an inner class we need to fix the name
    return "%s.fromJson(json.getJsonObject(\"%s\"))".formatted(simpleName(className), name);
  }

  private static String collectionGetter(String name, String className) {
    String collector = "toList()";
    if (className.startsWith("java.util.Set")) {
      collector = "collect(Collectors.toSet())";
    }

    String genericType = getGenericType(className);
    return switch (genericType) {
      // if the generic type is a java type we cast
      // if it is something else, we get a JsonObject type and call a from json class and
      // collect it
      case "java.lang.String" ->
          "json.getJsonArray(\"%s\").stream().filter(Objects::nonNull).map(s -> (String) s).%s"
              .formatted(name, collector);
      case "java.lang.Boolean" ->
          "json.getJsonArray(\"%s\").stream().filter(Objects::nonNull).map(b -> (Boolean) b).%s"
              .formatted(name, collector);
      case "java.lang.Integer" ->
          "json.getJsonArray(\"%s\").stream().filter(Objects::nonNull).map(i -> (Integer) i).%s"
              .formatted(name, collector);
      case "java.lang.Long" ->
          "json.getJsonArray(\"%s\").stream().filter(Objects::nonNull).map(l -> (Long) l).%s"
              .formatted(name, collector);
      case "java.lang.Float" ->
          "json.getJsonArray(\"%s\").stream().filter(Objects::nonNull).map(f -> (Float) f).%s"
              .formatted(name, collector);
      case "java.lang.Double" ->
          "json.getJsonArray(\"%s\").stream().map(d -> (Double) d).%s".formatted(name, collector);
      default ->
          "json.getJsonArray(\"%s\").stream().map(obj -> %s.fromJson((JsonObject) obj)).%s"
              .formatted(name, getSimpleName(genericType), collector);
    };
  }

  private static String javaLangGetter(String name, String className) {
    return switch (className) {
      case "java.lang.String" -> "json.getString(\"%s\")".formatted(name);
      case "java.lang.Boolean" -> "json.getBoolean(\"%s\")".formatted(name);
      case "java.lang.Integer" -> "json.getInteger(\"%s\")".formatted(name);
      case "java.lang.Short" -> "json.getInteger(\"%s\").shortValue()".formatted(name);
      case "java.lang.Long" -> "json.getLong(\"%s\")".formatted(name);
      case "java.lang.Float" -> "json.getFloat(\"%s\")".formatted(name);
      case "java.lang.Double" -> "json.getDouble(\"%s\")".formatted(name);
      case null, default ->
          throw new GenerationException("Unsupported java.lang.* type: " + className);
    };
  }

  private static String timeGetter(String name, String className, boolean nullable) {
    return switch (className) {
      case "java.time.LocalDate" -> {
        if (nullable) {
          yield "null == json.getString(\"%s\") ? null :"
              + " LocalDate.parse(json.getString(\"%s\"), DateTimeFormatter.ISO_DATE)";
        }
        yield "LocalDate.parse(json.getString(\"%s\"), DateTimeFormatter.ISO_DATE)".formatted(name);
      }
      case "java.time.LocalDateTime" -> {
        if (nullable) {
          yield "null == json.getString(\"%s\") ? null :"
              + " LocalDateTime.parse(json.getString(\"%s\"),"
              + " DateTimeFormatter.ISO_LOCAL_DATE_TIME)";
        }
        yield "LocalDateTime.parse(json.getString(\"%s\"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)"
            .formatted(name);
      }
      case "java.time.OffsetDateTime" -> {
        if (nullable) {
          yield "null == json.getString(\"%s\") ? null :"
              + " OffsetDateTime.parse(json.getString(\"%s\"),"
              + " DateTimeFormatter.ISO_OFFSET_DATE_TIME)";
        }
        yield "OffsetDateTime.parse(json.getString(\"%s\"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)"
            .formatted(name);
      }
      case null, default ->
          throw new GenerationException("Unsupported java.time.* type: " + className);
    };
  }

  private static String primitiveGetter(TypeKind kind, String name) {
    return switch (kind) {
      case BOOLEAN -> "json.getBoolean(\"%s\")".formatted(name);
      case INT -> "json.getInteger(\"%s\")".formatted(name);
      case LONG -> "json.getLong(\"%s\")".formatted(name);
      case FLOAT -> "json.getFloat(\"%s\")".formatted(name);
      case DOUBLE -> "json.getDouble(\"%s\")".formatted(name);
      case SHORT -> "json.getInteger(\"%s\").shortValue()".formatted(name);
      case CHAR -> "json.getString(\"%s\").charAt(0)".formatted(name);
      case BYTE -> "json.getBinary(\"%s\")[0]".formatted(name);
      default -> throw new GenerationException("Unsupported primitive type: " + kind);
    };
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
