/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.palantir.javapoet.TypeName;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.Draft;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.OutputFormat;
import io.vertx.json.schema.Validator;
import io.vertx.json.schema.common.dsl.Keywords;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import io.vertx.json.schema.common.dsl.Schemas;
import io.vertx.json.schema.common.dsl.StringFormat;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Generated;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
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
      out.printf("import %s;%n", ImmutableSet.class.getCanonicalName());
      out.printf("import %s;%n", Set.class.getCanonicalName());
      out.printf("import %s;%n", JsonObject.class.getCanonicalName());
      out.printf("import %s;%n", JsonArray.class.getCanonicalName());
      out.printf("import %s;%n", Collectors.class.getCanonicalName());
      out.printf("import %s;%n", DateTimeFormatter.class.getCanonicalName());
      out.printf("import %s;%n", LocalDate.class.getCanonicalName());
      out.printf("import %s;%n", LocalDateTime.class.getCanonicalName());
      out.printf("import %s;%n", OffsetDateTime.class.getCanonicalName());
      out.printf("import %s;%n", Generated.class.getCanonicalName());
      out.printf("import %s;%n", Pattern.class.getCanonicalName());
      out.printf("import %s;%n", Draft.class.getCanonicalName());
      out.printf("import %s;%n", OutputFormat.class.getCanonicalName());
      out.printf("import %s;%n", JsonSchemaOptions.class.getCanonicalName());
      out.printf("import %s;%n", Validator.class.getCanonicalName());
      out.printf("import %s;%n", JsonSchema.class.getCanonicalName());
      out.printf("import %s;%n", ObjectSchemaBuilder.class.getCanonicalName());
      out.printf("import %s;%n", StringFormat.class.getCanonicalName());

      String keywordsCanonicalName = Keywords.class.getCanonicalName();
      out.printf("import static %s.maxLength;%n", keywordsCanonicalName);
      out.printf("import static %s.maximum;%n", keywordsCanonicalName);
      out.printf("import static %s.minItems;%n", keywordsCanonicalName);
      out.printf("import static %s.maxItems;%n", keywordsCanonicalName);
      out.printf("import static %s.minLength;%n", keywordsCanonicalName);
      out.printf("import static %s.minimum;%n", keywordsCanonicalName);
      out.printf("import static %s.pattern;%n", keywordsCanonicalName);
      out.printf("import static %s.uniqueItems;%n", keywordsCanonicalName);
      out.printf("import static %s.format;%n", keywordsCanonicalName);
      String schemasCanonicalName = Schemas.class.getCanonicalName();
      out.printf("import static %s.arraySchema;%n", schemasCanonicalName);
      out.printf("import static %s.booleanSchema;%n", schemasCanonicalName);
      out.printf("import static %s.intSchema;%n", schemasCanonicalName);
      out.printf("import static %s.numberSchema;%n", schemasCanonicalName);
      out.printf("import static %s.objectSchema;%n", schemasCanonicalName);
      out.printf("import static %s.stringSchema;%n", schemasCanonicalName);

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
      jsonSchema(out, properties);

      out.println("}");
    }

    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(builderClassName);
    formatFile(stringWriter, builderFile);
  }

  private void jsonSchema(PrintWriter out, List<Property> properties) {
    out.println(
        """
    public static Validator getValidator() {
        return Validator.create(
            schema(),
            new JsonSchemaOptions()
                .setBaseUri("https://example.com")
                .setDraft(Draft.DRAFT7)
                .setOutputFormat(OutputFormat.Basic));
    }
""");
    out.println();
    out.println(
        """
    static JsonSchema schema() {
        return JsonSchema.of(schemaBuilder().toJson());
    }
""");
    out.println();
    out.println(
        """
static ObjectSchemaBuilder schemaBuilder() {
    return objectSchema()
""");

    for (Property property : properties) {
      if (property.isComplex()) {
        if (property.className().startsWith("java.lang.String")) {
          SchemaGenerator schemaGenerator =
              new StringSchemaGenerator(
                  property.name,
                  !property.nullable,
                  property.notBlank,
                  null == property.size || property.size.min() == 0 ? null : property.size.min(),
                  null == property.size || property.size.max() == Integer.MAX_VALUE
                      ? null
                      : property.size.max());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.lang.Boolean")) {
          SchemaGenerator schemaGenerator =
              new BooleanSchemaGenerator(property.name, !property.nullable);
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.lang.Long")) {
          SchemaGenerator schemaGenerator =
              new IntegerSchemaGenerator(
                  property.name,
                  !property.nullable,
                  null == property.min ? null : property.min.value(),
                  null == property.max ? null : property.max.value());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.lang.Integer")) {
          SchemaGenerator schemaGenerator =
              new IntegerSchemaGenerator(
                  property.name,
                  !property.nullable,
                  null == property.min ? null : property.min.value(),
                  null == property.max ? null : property.max.value());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.lang.Double")) {
          SchemaGenerator schemaGenerator =
              new IntegerSchemaGenerator(
                  property.name,
                  !property.nullable,
                  null == property.min ? null : property.min.value(),
                  null == property.max ? null : property.max.value());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.time.LocalDate")) {
          SchemaGenerator schemaGenerator =
              new DateSchemaGenerator(property.name, !property.nullable);
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.time.LocalDateTime")) {
          SchemaGenerator schemaGenerator =
              new DateSchemaGenerator(property.name, !property.nullable);
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.time.OffsetDateTime")) {
          SchemaGenerator schemaGenerator =
              new DateTimeSchemaGenerator(property.name, !property.nullable);
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else if (property.className().startsWith("java.util.Set")
            || property.className().startsWith("java.util.List")
            || property.className().startsWith("java.util.Collection")) {
          List<GenericParameterAnnotation> gpa = property.genericParameterAnnotations();
          var notNull = gpa.stream().anyMatch(f -> f instanceof GenericParameterAnnotation.NotNull);
          var notBlank =
              gpa.stream().anyMatch(f -> f instanceof GenericParameterAnnotation.NotBlank);
          var maybeSize =
              gpa.stream()
                  .filter(f -> f instanceof GenericParameterAnnotation.Size)
                  .map(s -> (GenericParameterAnnotation.Size) s)
                  .findFirst();
          var maybeMin =
              gpa.stream()
                  .filter(f -> f instanceof GenericParameterAnnotation.Min)
                  .map(s -> (GenericParameterAnnotation.Min) s)
                  .findFirst();
          var maybeMax =
              gpa.stream()
                  .filter(f -> f instanceof GenericParameterAnnotation.Max)
                  .map(s -> (GenericParameterAnnotation.Max) s)
                  .findFirst();

          SchemaGenerator itemSchemaGenerator =
              switch (getGenericType(property.className())) {
                case "java.lang.String" ->
                    new StringSchemaGenerator(
                        null,
                        notNull,
                        notBlank,
                        maybeSize.map(GenericParameterAnnotation.Size::min).orElse(null),
                        maybeSize.map(GenericParameterAnnotation.Size::max).orElse(null));
                case "java.lang.Boolean" -> new BooleanSchemaGenerator(null, notNull);
                case "java.lang.Long", "java.lang.Integer" ->
                    new IntegerSchemaGenerator(
                        null,
                        notNull,
                        maybeMin.isPresent() ? maybeMin.get().value() : null,
                        maybeMax.isPresent() ? maybeMax.get().value() : null);
                case "java.lang.Float", "java.lang.Double" ->
                    new NumberSchemaGenerator(
                        null,
                        notNull,
                        maybeMin.isPresent() ? maybeMin.get().value() : null,
                        maybeMax.isPresent() ? maybeMax.get().value() : null);
                default ->
                    new ObjectSchemaGenerator(null, notNull, getGenericType(property.className));
              };
          SchemaGenerator schemaGenerator =
              new ArraySchemaGenerator(
                  property.name,
                  !property.nullable,
                  true,
                  itemSchemaGenerator,
                  null == property.size || property.size.min() == 0 ? null : property.size.min(),
                  null == property.size || property.size.max() == Integer.MAX_VALUE
                      ? null
                      : property.size.max());
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        } else {
          SchemaGenerator schemaGenerator =
              new ObjectSchemaGenerator(property.name, !property.nullable, property.className);
          String print = schemaGenerator.print();
          out.println(print);
          out.println();
        }
      } else {
        SchemaGenerator schemaGenerator =
            switch (property.kind) {
              case CHAR, BYTE -> new StringSchemaGenerator(property.name, true, true, 1, 1);
              case LONG, INT, SHORT ->
                  new IntegerSchemaGenerator(
                      property.name,
                      true,
                      null == property.min ? null : property.min.value(),
                      null == property.max ? null : property.max.value());
              case DOUBLE, FLOAT ->
                  new NumberSchemaGenerator(
                      property.name,
                      true,
                      null == property.min ? null : property.min.value(),
                      null == property.max ? null : property.max.value());
              case BOOLEAN -> new BooleanSchemaGenerator(property.name, true);
              default ->
                  throw new IllegalArgumentException(
                      "Unsupported primitive type: " + property.kind);
            };
        String print = schemaGenerator.print();
        out.println(print);
        out.println();
      }
    }

    out.println("\t;");
    out.println("\t}");
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

    // RecordComponentElement does not work here as the annotations do not have
    // @Target(ElementType.RECORD_COMPONENT) as their target
    List<? extends Element> recordComponents =
        e.getEnclosedElements().stream().filter(f -> f.getKind() == ElementKind.FIELD).toList();

    for (Element enclosedElement : recordComponents) {
      VariableElement re = (VariableElement) enclosedElement;
      // name of the variable
      Name varName = re.getSimpleName();
      // type of the variable
      TypeMirror type = re.asType();
      // TypeKind.DECLARED -> this is an object
      TypeKind kind = type.getKind();
      Min min = re.getAnnotation(Min.class);
      Max max = re.getAnnotation(Max.class);
      Size size = re.getAnnotation(Size.class);

      // if type is declared and java.lang.String it is ok
      if (TypeKind.DECLARED == kind) {
        TypeName tn = TypeName.get(type);
        boolean nullable = null != re.getAnnotation(Nullable.class);
        boolean notBlank = null != re.getAnnotation(NotBlank.class);

        List<GenericParameterAnnotation> genericParameterAnnotations =
            getGenericParameterAnnotations((DeclaredType) type);

        TypeName typeNameWithoutAnnotations = tn.withoutAnnotations();
        String typeString = typeNameWithoutAnnotations.toString();
        properties.add(
            new Property(
                varName.toString(),
                nullable,
                true,
                typeString,
                kind,
                notBlank,
                min,
                max,
                size,
                genericParameterAnnotations));
      } else if (kind.isPrimitive()) {
        properties.add(
            new Property(
                varName.toString(), false, false, null, kind, false, min, max, size, List.of()));
      } else {
        String msg = String.format("unsupported kind: %s", kind);
        LOGGER.info(msg);
      }
    }

    return properties;
  }

  private List<GenericParameterAnnotation> getGenericParameterAnnotations(
      DeclaredType declaredType) {
    List<? extends TypeMirror> ta = declaredType.getTypeArguments();
    if (ta.isEmpty()) {
      return List.of();
    }
    if (ta.size() > 1) {
      throw new GenerationException("only support generic types with single generic type argument");
    }

    TypeMirror genericParameter = ta.getFirst();
    List<GenericParameterAnnotation> arr = new ArrayList<>();

    for (AnnotationMirror am : genericParameter.getAnnotationMirrors()) {
      DeclaredType annotationType = am.getAnnotationType();
      Element element = annotationType.asElement();
      String annotationClassName = element.asType().toString();
      switch (annotationClassName) {
        case "jakarta.validation.constraints.NotNull" -> {
          GenericParameterAnnotation a = GenericParameterAnnotation.NotNull.create(am);
          arr.add(a);
        }
        case "jakarta.validation.constraints.NotBlank" -> {
          GenericParameterAnnotation a = GenericParameterAnnotation.NotBlank.create(am);
          arr.add(a);
        }
        case "jakarta.validation.constraints.Min" -> {
          GenericParameterAnnotation a = GenericParameterAnnotation.Min.create(am);
          arr.add(a);
        }
        case "jakarta.validation.constraints.Max" -> {
          GenericParameterAnnotation a = GenericParameterAnnotation.Max.create(am);
          arr.add(a);
        }
        case "jakarta.validation.constraints.Size" -> {
          GenericParameterAnnotation a = GenericParameterAnnotation.Size.create(am);
          arr.add(a);
        }
        case null, default ->
            throw new GenerationException("unsupported annotation: " + annotationClassName);
      }
    }

    return List.copyOf(arr);
  }

  private record Property(
      String name,
      boolean nullable,
      boolean isComplex,
      String className,
      TypeKind kind,
      boolean notBlank,
      Min min,
      Max max,
      Size size,
      List<GenericParameterAnnotation> genericParameterAnnotations) {}

  private sealed interface GenericParameterAnnotation
      permits GenericParameterAnnotation.NotNull,
          GenericParameterAnnotation.NotBlank,
          GenericParameterAnnotation.Min,
          GenericParameterAnnotation.Max,
          GenericParameterAnnotation.Size {

    record NotNull() implements GenericParameterAnnotation {
      static NotNull create(AnnotationMirror am) {
        return new NotNull();
      }
    }

    record NotBlank() implements GenericParameterAnnotation {
      static NotBlank create(AnnotationMirror am) {
        return new NotBlank();
      }
    }

    record Min(long value) implements GenericParameterAnnotation {
      static Min create(AnnotationMirror am) {
        long min = 0L;
        for (var entry : am.getElementValues().entrySet()) {
          if (entry.getKey().getSimpleName().toString().equals("value")) {
            min = (long) entry.getValue().getValue();
          }
        }
        return new Min(min);
      }
    }

    record Max(long value) implements GenericParameterAnnotation {
      static Max create(AnnotationMirror am) {
        long max = 0L;
        for (var entry : am.getElementValues().entrySet()) {
          if (entry.getKey().getSimpleName().toString().equals("value")) {
            max = (long) entry.getValue().getValue();
          }
        }
        return new Max(max);
      }
    }

    record Size(@Null Integer min, @Null Integer max) implements GenericParameterAnnotation {
      static Size create(AnnotationMirror am) {
        Integer min = null;
        Integer max = null;
        for (var entry : am.getElementValues().entrySet()) {
          int defaultValue = (int) entry.getKey().getDefaultValue().getValue();

          if (entry.getKey().getSimpleName().toString().equals("min")) {
            min = (int) entry.getValue().getValue();
            if (min == defaultValue) {
              min = null;
            }
          }
          if (entry.getKey().getSimpleName().toString().equals("max")) {
            max = (int) entry.getValue().getValue();
            if (max == defaultValue) {
              max = null;
            }
          }
        }
        return new Size(min, max);
      }
    }
  }
}
