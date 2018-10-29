package com.flame239.cassandra.modelgen;

import com.flame239.cassandra.modelgen.context.ElementContext;
import com.flame239.cassandra.modelgen.context.Field;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Table;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static javax.tools.Diagnostic.Kind;

/**
 * @author laci009
 * @author flame239
 */
@SupportedAnnotationTypes({
    "org.springframework.data.cassandra.core.mapping.Table"
})
@SupportedOptions({
    CassandraModelProcessor.DEBUG_OPTION
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CassandraModelProcessor extends AbstractProcessor {
  private static final String META_POSTFIX = "_";
  public static final String DEBUG_OPTION = "debug";

  private MessageLogger logger;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    boolean debug = Boolean.parseBoolean(env.getOptions().get(CassandraModelProcessor.DEBUG_OPTION));
    logger = new MessageLogger(env, debug);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
    for (TypeElement annotation : annotations) {
      processAnnotation(roundEnvironment, annotation);
    }
    return true;
  }

  private void processAnnotation(RoundEnvironment roundEnv, TypeElement annotation) {
    processingEnv.getMessager().printMessage(Kind.NOTE, "Spring Cassandra Metamodel Generator (by flame239)\n");

    final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
    for (Element element : annotatedElements) {
      processAnnotatedElement(element);
    }
  }

  private void processAnnotatedElement(Element annotatedElement) {
    logger.logMessage(Kind.NOTE, "Element found: " + annotatedElement.toString());

    try {
      processElement(annotatedElement);
    } catch (IOException e) {
      processingEnv.getMessager().printMessage(Kind.ERROR,
          "Error while generating metadata: " + e.getMessage(), annotatedElement);
    }
  }

  private void processElement(Element element) throws IOException {
    if (!isClass(element)) {
      return;
    }

    TypeElement classElement = (TypeElement) element;
    logger.logMessage(Kind.NOTE, "Annotated class: " + classElement.getQualifiedName(), classElement);

    final ElementContext elementContext = initElementContext(classElement);

    ClassGenerator classGenerator = new ClassGenerator(elementContext, processingEnv, logger);
    classGenerator.generateMetadataClass();
  }

  private boolean isClass(Element element) {
    return ElementKind.CLASS.equals(element.getKind());
  }

  private boolean isField(Element classMember) {
    return ElementKind.FIELD.equals(classMember.getKind());
  }

  private ElementContext initElementContext(TypeElement classElement) {
    PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
    final String packageName = packageElement.getQualifiedName().toString();
    final String className = classElement.getSimpleName().toString();
    final String qualifiedName = classElement.getQualifiedName().toString() + META_POSTFIX;
    final Optional<String> tableName = getTableName(classElement, className);
    final List<Field> fields = getFields(classElement);
    return ElementContext.of(packageName, className, tableName, qualifiedName, fields);
  }

  private List<Field> getFields(TypeElement classElement) {
    List<Field> fields = new ArrayList<>();
    for (Element classMember : classElement.getEnclosedElements()) {
      if (isField(classMember)) {
        addField(fields, (VariableElement) classMember);
      }
    }

    return fields;
  }

  private void addField(List<Field> fields, VariableElement fieldElement) {
    if (!fieldElement.getModifiers().contains(Modifier.STATIC)) {
      fields.add(addColumnField(fieldElement));
    }
  }

  private Field addColumnField(VariableElement fieldElement) {
    String fieldName = fieldElement.getSimpleName().toString();
    Column columnAnnotation = fieldElement.getAnnotation(Column.class);
    String fieldValue = fieldName.toLowerCase();
    if (columnAnnotation != null && StringUtils.isNotBlank(columnAnnotation.value())) {
      fieldValue = columnAnnotation.value();
    }

    return Field.of(fieldName, fieldValue);
  }

  private Optional<String> getTableName(TypeElement classElement, String className) {
    Optional<String> tableName = Optional.empty();
    if (classElement.getAnnotation(Table.class) != null) {
      String annotationValue = classElement.getAnnotation(Table.class).value();
      if (StringUtils.isNotBlank(annotationValue)) {
        tableName = Optional.of(annotationValue);
      } else {
        tableName = Optional.of(className.toLowerCase());
      }
    }
    return tableName;
  }

}
