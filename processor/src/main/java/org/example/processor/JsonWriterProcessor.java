package org.example.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class JsonWriterProcessor extends AbstractProcessor {

  // todo try using this rather for generating code: https://github.com/square/javapoet

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
    String builderClassName = annotatedClassName + "JsonWriter";
    String builderSimpleClassName = builderClassName.substring(lastDot + 1);

    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(builderClassName);
    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode objectNode = objectMapper.createObjectNode();
      ObjectNode jsonNodes = objectNode.objectNode();
      objectNode.set("newObject", jsonNodes);

      if (packageName != null) {
        out.print("package ");
        out.print(packageName);
        out.println(";");
        out.println();
      }

      out.println("import " + annotatedClassName + ";");
      out.println("import com.fasterxml.jackson.databind.ObjectMapper;");
      out.println("import com.fasterxml.jackson.databind.node.ObjectNode;");
      out.println("import com.fasterxml.jackson.databind.JsonNode;");
      out.println("import com.fasterxml.jackson.core.JsonProcessingException;");
      out.println("import java.util.Map;");
      out.println("import java.util.Base64;");
      out.println("import java.nio.charset.StandardCharsets;");
      out.println();

      out.print("public class ");
      out.print(builderSimpleClassName);
      out.println(" {");
      out.println();

      properties.stream()
          .map(
              property ->
                  "\tpublic static final String "
                      + property.name().toUpperCase(Locale.ROOT)
                      + " = \""
                      + property.name()
                      + "\";")
          .forEach(out::println);
      out.println();

      if (hasOnlyPrimitiveAttributes(properties)) {
        toMap(properties, simpleClassName, out);
        out.println();

        fromMap(properties, simpleClassName, out);
        out.println();
      }

      toJsonBytes(simpleClassName, out);
      out.println();

      toJsonString(simpleClassName, out);
      out.println();

      toJsonNode(properties, simpleClassName, out);

      out.println("}");
    }
  }

  private boolean hasOnlyPrimitiveAttributes(List<Property> properties) {
    for (Property property : properties) {
      if (property.isComplex()) {
        return false;
      }
    }

    return true;
  }

  private void fromMap(List<Property> properties, String simpleClassName, PrintWriter out) {
    out.println("\tpublic static " + simpleClassName + " fromMap(Map<String, String> in) {");

    List<Property> simpleProperties = properties.stream().filter(p -> !p.isComplex()).toList();

    simpleProperties.forEach(prim -> out.println("\t\t" + primitiveFromString(prim)));

    String varNames = String.join(",", simpleProperties.stream().map(Property::name).toList());
    out.println("\t\treturn new " + simpleClassName + "(" + varNames + ");");

    out.println("\t}");
  }

  private void toMap(List<Property> properties, String simpleClassName, PrintWriter out) {
    List<Property> simpleProperties = properties.stream().filter(p -> !p.isComplex()).toList();

    out.println("\tpublic static Map<String, String> toMap(" + simpleClassName + " in) {");
    out.println("\t\treturn Map.of(");

    for (int i = 0; i < simpleProperties.size(); i++) {
      Property simpleProperty = simpleProperties.get(i);
      out.print(
          "\t\t\t"
              + simpleProperty.name().toUpperCase(Locale.ROOT)
              + ", "
              + primitiveToString(simpleProperty)
              + "");

      // add trailing comma if needed
      if (i != simpleProperties.size() - 1) {
        out.println(",");
      } else {
        out.println();
      }
    }
    out.println("\t\t);");
    out.println("\t}");
  }

  private String primitiveFromString(Property property) {
    return switch (property.kind()) {
      case BOOLEAN -> "boolean "
          + property.name()
          + " = Boolean.getBoolean(in.get("
          + property.name().toUpperCase(Locale.ROOT)
          + "));";
      case BYTE -> "byte "
          + property.name()
          + " = Base64.getDecoder().decode(in.get("
          + property.name().toUpperCase(Locale.ROOT)
          + "))[0];";
      case SHORT -> "short "
          + property.name()
          + " = Short.parseShort(in.get("
          + property.name().toUpperCase(Locale.ROOT)
          + "));";
      case INT -> "int "
          + property.name()
          + " = Integer.parseInt(in.get("
          + property.name().toUpperCase(Locale.ROOT)
          + "));";
      case LONG -> "long "
          + property.name()
          + " = Long.parseLong(in.get("
          + property.name().toUpperCase(Locale.ROOT)
          + "));";
      case CHAR -> "char "
          + property.name()
          + " = in.get("
          + property.name().toUpperCase(Locale.ROOT)
          + ").charAt(0);";
      case FLOAT -> "float "
          + property.name()
          + " = Float.parseFloat(in.get("
          + property.name().toUpperCase(Locale.ROOT)
          + "));";
      case DOUBLE -> "double "
          + property.name()
          + " = Double.parseDouble(in.get("
          + property.name().toUpperCase(Locale.ROOT)
          + "));";
      default -> "String "
          + property.name()
          + " = in.get("
          + property.name().toUpperCase(Locale.ROOT)
          + ");";
    };
  }

  private String primitiveToString(Property property) {
    return switch (property.kind()) {
      case BOOLEAN -> "Boolean.toString(in." + property.name() + "())";
      case BYTE -> "Base64.getEncoder().encodeToString(new byte[]{in." + property.name() + "()})";
      case SHORT -> "Short.toString(in." + property.name() + "())";
      case INT -> "Integer.toString(in." + property.name() + "())";
      case LONG -> "Long.toString(in." + property.name() + "())";
      case CHAR -> "String.valueOf(in." + property.name() + "())";
      case FLOAT -> "Float.toString(in." + property.name() + "())";
      case DOUBLE -> "Double.toString(in." + property.name() + "())";
      default -> "in." + property.name() + "()";
    };
  }

  private void toJsonNode(List<Property> properties, String simpleClassName, PrintWriter out) {
    out.println("\tstatic JsonNode toJsonNode(ObjectNode root, " + simpleClassName + " in) {");

    for (Property property : properties) {
      if (Boolean.FALSE.equals(property.isComplex())) {
        if (property.kind == TypeKind.CHAR) {
          out.println(
              "\t\troot.put("
                  + property.name().toUpperCase(Locale.ROOT)
                  + ", String.valueOf(in."
                  + property.name()
                  + "()));");
        } else if (property.kind == TypeKind.BYTE) {
          // base64 encode
          out.println(
              "\t\troot.put("
                  + property.name().toUpperCase(Locale.ROOT)
                  + ", Base64.getEncoder().encodeToString(new byte[]{in."
                  + property.name()
                  + "()}));");
        } else {
          out.println(
              "\t\troot.put("
                  + property.name().toUpperCase(Locale.ROOT)
                  + ", in."
                  + property.name()
                  + "());");
        }
      } else {
        out.println(
            "\t\troot.set("
                + property.name().toUpperCase(Locale.ROOT)
                + ", "
                + property.className()
                + "JsonWriter.toJsonNode(root.objectNode(), in."
                + property.name()
                + "()));");
      }
    }

    out.println("\t\treturn root;");
    out.println("\t}");
  }

  private void toJsonBytes(String simpleClassName, PrintWriter out) {
    out.println("\tpublic static byte[] toJsonBytes(" + simpleClassName + " in) {");
    out.println("\t\tObjectMapper objectMapper = new ObjectMapper();");
    out.println();
    out.println("\t\ttry {");
    out.println("\t\t\tJsonNode node = toJsonNode(objectMapper.createObjectNode(),in);");
    out.println("\t\t\treturn objectMapper.writeValueAsBytes(node);");
    out.println("\t\t} catch (JsonProcessingException e) {");
    out.println("\t\t\tthrow new RuntimeException(e);");
    out.println("\t\t}");
    out.println("\t}");
  }

  private void toJsonString(String simpleClassName, PrintWriter out) {
    out.println("\tpublic static String toJsonString(" + simpleClassName + " in) {");
    out.println("\t\tObjectMapper objectMapper = new ObjectMapper();");
    out.println();
    out.println("\t\ttry {");
    out.println("\t\t\tJsonNode node = toJsonNode(objectMapper.createObjectNode(),in);");
    out.println("\t\t\treturn objectMapper.writeValueAsString(node);");
    out.println("\t\t} catch (JsonProcessingException e) {");
    out.println("\t\t\tthrow new RuntimeException(e);");
    out.println("\t\t}");
    out.println("\t}");
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
      if (kind == TypeKind.DECLARED) {
        String typeString = type.toString();

        // we can put strings
        if (typeString.equals("java.lang.String")) {
          properties.add(new Property(varName.toString(), false, null, kind));
        }

        // todo support a list of these packages
        String participatingPackage = processingEnv.getOptions().get(PACKAGE);

        if (participatingPackage != null && typeString.startsWith(participatingPackage)) {
          String name = typeString.substring(typeString.lastIndexOf('.') + 1);
          properties.add(new Property(varName.toString(), true, name, kind));
        }

        // todo
        //  we can put lists
        //  get the type of the list
        //  if (typeString.equals("java.util.List")) {
        //    vars.put(varName, type);
        //    continue;
        //  }
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
