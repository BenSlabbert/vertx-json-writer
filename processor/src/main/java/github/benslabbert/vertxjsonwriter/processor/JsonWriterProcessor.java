/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

public class JsonWriterProcessor extends AbstractProcessor {

  private static final Logger LOGGER = Logger.getLogger(JsonWriterProcessor.class.getName());

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(JsonWriter.class.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
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

  private static class StringSource extends CharSource {

    private final StringWriter writer;

    private StringSource(StringWriter writer) {
      this.writer = writer;
    }

    @Nonnull
    @Override
    public Reader openStream() {
      return new StringReader(writer.toString());
    }
  }

  private static class FileSink extends CharSink {

    private final JavaFileObject builderFile;

    private FileSink(JavaFileObject builderFile) {
      this.builderFile = builderFile;
    }

    @Nonnull
    @Override
    public Writer openStream() throws IOException {
      return new PrintWriter(builderFile.openWriter());
    }
  }

  private void generate(Element e) throws IOException {
    if (ElementKind.RECORD != e.getKind()) {
      processingEnv.getMessager().printError("can only generate JsonWriters on record types", e);
      return;
    }

    TypeElement te = (TypeElement) e;

    if (NestingKind.TOP_LEVEL != te.getNestingKind() && NestingKind.MEMBER != te.getNestingKind()) {
      processingEnv
          .getMessager()
          .printError("can only generate JsonWriters on top level or member types", e);
      return;
    }

    boolean isMember = NestingKind.MEMBER == te.getNestingKind();

    List<Property> properties = getProperties(te);

    if (properties.isEmpty()) {
      return;
    }

    String annotatedClassName = te.getQualifiedName().toString();

    String packageName = null;
    int lastDot = annotatedClassName.lastIndexOf('.');
    if (lastDot > 0) {
      if (isMember) {
        packageName = getMemberClassPackage(annotatedClassName);
        annotatedClassName = getMemberClassName(packageName, annotatedClassName);
        lastDot = annotatedClassName.lastIndexOf('.');
      } else {
        packageName = annotatedClassName.substring(0, lastDot);
      }
    }

    // simpleClassName this will be what we return from our method
    String simpleClassName = annotatedClassName.substring(lastDot + 1);
    String builderClassName = annotatedClassName + "_JsonWriter";
    String builderSimpleClassName = builderClassName.substring(lastDot + 1);

    // 8k should be big enough for most files without needing a resize
    StringWriter stringWriter = new StringWriter(8 * 1024);

    try (PrintWriter out = new PrintWriter(stringWriter)) {
      if (packageName != null) {
        out.print("package ");
        out.print(packageName);
        out.println(";");
        out.println();
      }

      // if this is a nested class name we need to undo our renaming here
      if (isMember) {
        String tmp = annotatedClassName.replace('_', '.');
        out.println("import " + tmp + ";");
      } else {
        out.println("import " + annotatedClassName + ";");
      }
      out.println("import com.google.common.collect.ImmutableSet;");
      out.println("import java.util.Set;");
      out.println("import io.vertx.core.json.JsonObject;");
      out.println("import io.vertx.core.json.JsonArray;");
      out.println("import java.util.stream.Collectors;");
      out.println("import java.time.format.DateTimeFormatter;");
      out.println("import java.time.LocalDate;");
      out.println("import java.time.LocalDateTime;");
      out.println("import java.time.OffsetDateTime;");
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

      toJson(out, properties, simpleClassName);
      fromJson(out, properties, simpleClassName);

      out.println("}");
    }

    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(builderClassName);
    formatFile(stringWriter, builderFile);
  }

  private void formatFile(StringWriter writer, JavaFileObject builderFile) {
    try {
      CharSource source = new StringSource(writer);
      CharSink output = new FileSink(builderFile);

      JavaFormatterOptions options =
          JavaFormatterOptions.builder()
              .formatJavadoc(true)
              .reorderModifiers(true)
              .style(JavaFormatterOptions.Style.GOOGLE)
              .build();

      new Formatter(options).formatSource(source, output);
    } catch (FormatterException | IOException e) {
      throw new GenerationException(e);
    }
  }

  private static String getMemberClassPackage(String canonicalName) {
    int firstClassIdx = 0;
    for (int i = 0; i < canonicalName.length(); i++) {
      if (Character.isUpperCase(canonicalName.charAt(i))) {
        firstClassIdx = i;
        break;
      }
    }
    return canonicalName.substring(0, firstClassIdx - 1);
  }

  private static String getMemberClassName(String packageName, String canonicalName) {
    int firstClassIdx = 0;
    for (int i = 0; i < canonicalName.length(); i++) {
      if (Character.isUpperCase(canonicalName.charAt(i))) {
        firstClassIdx = i;
        break;
      }
    }
    String s = canonicalName.substring(firstClassIdx).replace('.', '_');
    return packageName + "." + s;
  }

  private void toJson(PrintWriter out, List<Property> properties, String simpleClassName) {
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

  private void timeToJson(PrintWriter out, Property property) {
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

  private void iterableToJson(PrintWriter out, Property property) {
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

  private static String getGenericType(String className) {
    return className.substring(className.indexOf('<') + 1, className.indexOf('>'));
  }

  private static String getSimpleName(String canonicalName) {
    return canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
  }

  private void fromJson(PrintWriter out, List<Property> properties, String simpleClassName) {
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
            throw new IllegalArgumentException("Unsupported primitive type: " + property.kind);
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
        TypeName tn = TypeName.get(type);
        boolean nullable = isNullable(tn);
        TypeName typeNameWithoutAnnotations = tn.withoutAnnotations();
        String typeString = typeNameWithoutAnnotations.toString();
        properties.add(new Property(varName.toString(), nullable, true, typeString, kind));
      } else if (kind.isPrimitive()) {
        properties.add(new Property(varName.toString(), false, false, null, kind));
      } else {
        String msg = String.format("unsupported kind: %s", kind);
        LOGGER.info(msg);
      }
    }

    return properties;
  }

  private boolean isNullable(TypeName tn) {
    for (AnnotationSpec annotation : tn.annotations) {
      if (annotation.type.toString().equals("javax.annotation.Nullable")
          || annotation.type.toString().equals("jakarta.annotation.Nullable")) {
        return true;
      }
    }
    return false;
  }

  private record Property(
      String name, boolean nullable, boolean isComplex, String className, TypeKind kind) {}
}
