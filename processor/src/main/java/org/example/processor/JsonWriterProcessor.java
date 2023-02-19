package org.example.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
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
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class JsonWriterProcessor extends AbstractProcessor {

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

    List<Property> jsonNodeForNameMap = getJsonNodeForNameMap(te);

    if (jsonNodeForNameMap.isEmpty()) {
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
      out.println();

      out.print("public class ");
      out.print(builderSimpleClassName);
      out.println(" {");
      out.println();

      toJson(simpleClassName, out);
      toJsonNode(jsonNodeForNameMap, simpleClassName, out);

      out.println("}");
    }
  }

  private void toJsonNode(
      List<Property> jsonNodeForNameMap, String simpleClassName, PrintWriter out) {
    out.println("\tstatic JsonNode toJsonNode(ObjectNode root, " + simpleClassName + " in) {");

    for (Property property : jsonNodeForNameMap) {
      if (Boolean.FALSE.equals(property.isComplex())) {
        out.println("\t\troot.put(\"" + property.name() + "\", in." + property.name() + "());");
      } else {
        out.println(
            "\t\troot.set(\""
                + property.name()
                + "\", "
                + property.qualifiedName()
                + "JsonWriter.toJsonNode(root.objectNode(), in.job()));");
      }
    }

    out.println("\t\treturn root;");
    out.println("\t}");
  }

  private void toJson(String simpleClassName, PrintWriter out) {
    out.println("\tpublic static byte[] toJson(" + simpleClassName + " in) {");
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

  private List<Property> getJsonNodeForNameMap(Element e) {
    List<Property> vars = new ArrayList<>();

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
          vars.add(new Property(varName.toString(), false, null));
        }

        String participatingPackage = processingEnv.getOptions().get(PACKAGE);

        if (participatingPackage != null && typeString.startsWith("org.example.")) {
          String name = typeString.substring(typeString.lastIndexOf('.') + 1);
          vars.add(new Property(varName.toString(), true, name));
        }

        // todo
        //  we can put lists
        //  get the type of the list
        //  if (typeString.equals("java.util.List")) {
        //    vars.put(varName, type);
        //    continue;
        //  }
      } else if (kind.isPrimitive()) {
        vars.add(new Property(varName.toString(), false, null));
      } else {
        LOGGER.info("unsupported kind: " + kind);
      }
    }

    return vars;
  }

  private record Property(String name, boolean isComplex, String qualifiedName) {}
}
