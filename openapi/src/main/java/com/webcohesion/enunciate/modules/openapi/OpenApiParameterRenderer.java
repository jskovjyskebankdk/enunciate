package com.webcohesion.enunciate.modules.openapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.modules.freemarker.Typed1ArgTemplateMethod;

public class OpenApiParameterRenderer extends Typed1ArgTemplateMethod<String, String> {
  private final EnunciateLogger logger;
  private final Parameter parameter;
  private final OpenApiParameterTypes type;

  public OpenApiParameterRenderer(EnunciateLogger logger, Parameter parameter, OpenApiParameterTypes type) {
    super(String.class);
    this.logger = logger;
    this.parameter = parameter;
    this.type = type;
  }

  @Override
  protected String exec(String newlineIndents) {
    String nextLineIndent = "FIXME>"+newlineIndents+"<";
    
    List<String> lines = renderLines();
    
    Iterator<String> ix = lines.iterator();
    StringBuilder sb = new StringBuilder(ix.next());
    while (ix.hasNext()) {
      sb.append(System.lineSeparator()).append(nextLineIndent);
      sb.append(ix.next());
    }
    
    return sb.toString();
  }
  
  private List<String> renderLines() {
    if (isEnum()) {
      return enums();
    }
    
    if (parameter.isMultivalued()) {
      return arrays();
    }
    
    List<String> lines = new ArrayList<>();
    lines.add("type: " + parameter.getTypeName());
    if (getTypeFormatName() != null) {
      lines.add("format: " + getTypeFormatName());
    }
    return lines;
  }

  private String getTypeFormatName() {
    return BaseTypeToOpenApiType.toSwaggerFormat(parameter.getTypeFormat());
  }

  private boolean isEnum() {
    return parameter.getConstraintValues() != null;
  }

  private List<String> enums() {
    List<String> lines = new ArrayList<>();
    lines.add("type: string");
    lines.add("enum:");
    for (String i : parameter.getConstraintValues()) {
      lines.add("- " + i);
    }
    return lines;
  }
  
  private List<String> arrays() {
    List<String> lines = new ArrayList<>();
    lines.add("type: array");
    lines.add("items:");
    lines.add("- FIXME");
    return lines;
  }
}
