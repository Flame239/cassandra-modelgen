package com.flame239.cassandra.modelgen;

import com.flame239.cassandra.modelgen.context.ElementContext;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import static javax.tools.Diagnostic.Kind;

/**
 * @author laci009
 */
public class ClassGenerator {
  private static final String TEMPLATE_FILE = "metaclass.vm";

  private final ElementContext elementContext;
  private final ProcessingEnvironment env;
  private final MessageLogger logger;

  public ClassGenerator(
      ElementContext elementContext,
      ProcessingEnvironment procEnv,
      MessageLogger logger
  ) {
    this.elementContext = elementContext;
    this.env = procEnv;
    this.logger = logger;
  }

  public void generateMetadataClass() {
    try {
      generateClass();
    } catch (ResourceNotFoundException | ParseErrorException | IOException e) {
      env.getMessager().printMessage(Kind.ERROR, e.getLocalizedMessage());
    }
  }

  private void generateClass() throws IOException {
    VelocityEngine velocityEngine = initVelocityEngine();
    VelocityContext context = initVelocityContext(elementContext);

    Template template = velocityEngine.getTemplate(TEMPLATE_FILE);
    JavaFileObject javaFileObject = env.getFiler().createSourceFile(elementContext.getQualifiedName());
    logger.logMessage(Kind.NOTE, "Creating source file: " + javaFileObject.toUri());

    Writer writer = javaFileObject.openWriter();
    logger.logMessage(Kind.NOTE, "Applying velocity template: " + template.getName());
    template.merge(context, writer);
    writer.close();
  }

  private VelocityEngine initVelocityEngine() throws IOException {
    Properties properties = new Properties();
    URL url = this.getClass().getClassLoader().getResource("velocity.properties");
    properties.load(url.openStream());

    VelocityEngine velocityEngine = new VelocityEngine(properties);
    velocityEngine.init();
    return velocityEngine;
  }

  private VelocityContext initVelocityContext(ElementContext elementContext) {
    VelocityContext context = new VelocityContext();
    context.put("packageName", elementContext.getPackageName());
    context.put("className", elementContext.getClassName());
    context.put("fields", elementContext.getFields());
    context.put("tableName", elementContext.getTableName().orElse(null));
    return context;
  }
}
