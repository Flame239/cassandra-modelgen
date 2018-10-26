package com.flame239.cassandra.modelgen;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class MessageLogger {
  private final ProcessingEnvironment env;
  private final boolean debug;

  public MessageLogger(ProcessingEnvironment env, boolean debug) {
    this.env = env;
    this.debug = debug;
  }

  public void logMessage(Diagnostic.Kind type, String message) {
    if (debug) {
      env.getMessager().printMessage(type, message);
    }
  }

  public void logMessage(Diagnostic.Kind type, String message, Element e) {
    if (debug) {
      env.getMessager().printMessage(type, message, e);
    }
  }
}
