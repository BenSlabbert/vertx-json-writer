/* Licensed under Apache-2.0 2024. */
package org.example.processor;

import com.google.auto.service.AutoService;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("org.example.processor.annotation.JsonWriter")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class JsonWriterProcessor extends AbstractProcessor {

  // todo look into
  //  https://checkerframework.org/api/org/checkerframework/javacutil/package-summary.html
  //  and dep https://mvnrepository.com/artifact/org.checkerframework/javacutil/3.31.0
  //  can help get better type management for generated code

  private static final Logger LOGGER = Logger.getLogger(JsonWriterProcessor.class.getName());

  private static final String PACKAGE = "package";

  @Override
  public Set<String> getSupportedOptions() {
    return Set.of(PACKAGE);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement annotation : annotations) {
      Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(annotation);

      try {
        for (Element o : elementsAnnotatedWith) {
          generate(o);
        }
      } catch (Exception e) {
        throw new GenerationException(e);
      }
    }

    return true;
  }

  private void generate(Element e) throws IOException {
    if (!TypeElement.class.isAssignableFrom(e.getClass())) {
      return;
    }

    TypeElement te = (TypeElement) e;

    List<Property> properties = getProperties(te);

    if (properties.isEmpty()) {
      return;
    }

    String annotatedClassName = te.getQualifiedName().toString();

    String packageName = null;
    int lastDot = annotatedClassName.lastIndexOf('.');
    if (lastDot > 0) {
      packageName = annotatedClassName.substring(0, lastDot);
    }

    // simpleClassName this will be what we return from our method
    String simpleClassName = annotatedClassName.substring(lastDot + 1);
    String builderClassName = annotatedClassName + "_JsonWriter";
    String builderSimpleClassName = builderClassName.substring(lastDot + 1);

    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(builderClassName);
    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      if (packageName != null) {
        out.print("package ");
        out.print(packageName);
        out.println(";");
        out.println();
      }

      out.println("import " + annotatedClassName + ";");
      out.println("import io.vertx.core.json.JsonObject;");
      out.println("import javax.annotation.processing.Generated;");
      out.println();

      out.printf(
          "@Generated(value = \"%s\", date = \"%s\")%n",
          getClass().getCanonicalName(),
          LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
      out.print("public final class ");
      out.print(builderSimpleClassName);
      out.println(" {");
      out.println();

      out.println("\tprivate " + builderSimpleClassName + "() {}");
      out.println();

      // toJson
      toJson(out, properties, simpleClassName);

      // fromJson
      fromJson(out, properties, simpleClassName);

      out.println("}");
    }
  }

  private void toJson(PrintWriter out, List<Property> properties, String simpleClassName) {
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
        } else {
          out.printf("\t\tjson.put(\"%s\", o.%s().toJson());%n", property.name(), property.name());
        }

        System.err.println("property:");
        System.err.println("kind: " + property.kind());
        System.err.println("isComplex: " + property.isComplex());
        System.err.println("name: " + property.name());
        System.err.println("className: " + property.className());
      }
    }

    out.println("\t\treturn json;");
    out.println("\t}");
    out.println();
  }

  private void fromJson(PrintWriter out, List<Property> properties, String simpleClassName) {
    out.printf("\tpublic static %s fromJson(JsonObject json) {%n", simpleClassName);
    out.printf("\t\treturn %s.builder()%n", simpleClassName);

    for (Property property : properties) {
      String jsonGetter = getJsonGetter("json", property);
      out.printf("\t\t\t.%s(%s)%n", property.name(), jsonGetter);
    }

    out.println("\t\t\t.build();");
    out.println("\t}");
  }

  private static String getJsonGetter(String caller, Property property) {
    if (!property.isComplex()) {
      return switch (property.kind) {
        case BOOLEAN -> "%s.getBoolean(\"%s\")".formatted(caller, property.name());
        case INT -> "%s.getInteger(\"%s\")".formatted(caller, property.name());
        case LONG -> "%s.getLong(\"%s\")".formatted(caller, property.name());
        case FLOAT -> "%s.getFloat(\"%s\")".formatted(caller, property.name());
        case DOUBLE -> "%s.getDouble(\"%s\")".formatted(caller, property.name());
        case SHORT -> "%s.getNumber(\"%s\").shortValue()".formatted(caller, property.name());
        case CHAR -> "%s.getString(\"%s\").charAt(0)".formatted(caller, property.name());
        case BYTE -> "%s.getBinary(\"%s\")[0]".formatted(caller, property.name());
        default ->
            throw new IllegalArgumentException("Unsupported primitive type: " + property.kind);
      };
    }

    if (property.className().startsWith("java.lang.")) {
      return switch (property.className()) {
        case "java.lang.String" -> "%s.getString(\"%s\")".formatted(caller, property.name());
        case "java.lang.Boolean" -> "%s.getBoolean(\"%s\")".formatted(caller, property.name());
        case "java.lang.Integer" -> "%s.getInteger(\"%s\")".formatted(caller, property.name());
        case "java.lang.Long" -> "%s.getLong(\"%s\")".formatted(caller, property.name());
        case "java.lang.Float" -> "%s.getFloat(\"%s\")".formatted(caller, property.name());
        case "java.lang.Double" -> "%s.getDouble(\"%s\")".formatted(caller, property.name());
        default ->
            throw new IllegalArgumentException(
                "Unsupported java.lang.* type: " + property.className());
      };
    }

    return "%s.fromJson(%s.getJsonObject(\"%s\"))"
        .formatted(simpleName(property.className()), caller, property.name());
  }

  private static String simpleName(String classname) {
    return classname.substring(classname.lastIndexOf('.') + 1);
  }

  private List<Property> getProperties(Element e) {
    List<Property> properties = new ArrayList<>();

    List<? extends Element> recordComponents =
        e.getEnclosedElements().stream()
            .filter(f -> f.getKind() == ElementKind.RECORD_COMPONENT)
            .toList();

    for (Element enclosedElement : recordComponents) {

      RecordComponentElement re = (RecordComponentElement) enclosedElement;
      // name of the variable
      Name varName = re.getSimpleName();
      // type of the variable
      TypeMirror type = re.asType();
      // TypeKind.DECLARED -> this is an object
      TypeKind kind = type.getKind();

      // if type is declared and java.lang.String it is ok
      if (TypeKind.DECLARED == kind) {
        String typeString = type.toString();
        properties.add(new Property(varName.toString(), true, typeString, kind));
      } else if (kind.isPrimitive()) {
        properties.add(new Property(varName.toString(), false, null, kind));
      } else {
        String msg = String.format("unsupported kind: %s", kind);
        LOGGER.info(msg);
      }
    }

    return properties;
  }

  private record Property(String name, boolean isComplex, String className, TypeKind kind) {}
}
