/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Generated;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class JsonWriterProcessor extends AbstractProcessor {

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
      try {
        for (Element o : roundEnv.getElementsAnnotatedWith(annotation)) {
          Instant start = Instant.now();
          generate(o);
          Duration duration = Duration.between(start, Instant.now());
          processingEnv
              .getMessager()
              .printMessage(Diagnostic.Kind.NOTE, "%s: processing time %s".formatted(o, duration));
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

    List<Property> properties = PropertyBuilder.getProperties(te);
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
      out.printf("import %s;%n", Objects.class.getCanonicalName());

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

      out.println("private " + builderSimpleClassName + "() {}");
      out.println();

      ToJsonGenerator.toJson(out, properties, simpleClassName);
      FromJsonGenerator.fromJson(out, properties, simpleClassName);
      JsonSchemaGenerator.jsonSchema(out, properties);

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

      Instant start = Instant.now();
      new Formatter(options).formatSource(source, output);
      processingEnv
          .getMessager()
          .printMessage(
              Diagnostic.Kind.NOTE,
              "formatting time %s".formatted(Duration.between(start, Instant.now())));
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
}
