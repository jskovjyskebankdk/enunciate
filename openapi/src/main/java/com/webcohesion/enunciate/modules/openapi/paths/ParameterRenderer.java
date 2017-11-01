package com.webcohesion.enunciate.modules.openapi.paths;

import java.util.ArrayList;
import java.util.Set;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.modules.freemarker.Typed1ArgTemplateMethod;
import com.webcohesion.enunciate.modules.openapi.DataTypeReferenceRenderer;
import com.webcohesion.enunciate.modules.openapi.ObjectTypeRenderer;
import com.webcohesion.enunciate.modules.openapi.yaml.IndententationPrinter;

public class ParameterRenderer extends Typed1ArgTemplateMethod<String, String> {
  @SuppressWarnings("unused") private final EnunciateLogger logger;
  private final Parameter parameter;

  public ParameterRenderer(EnunciateLogger logger, Parameter parameter) {
    super(String.class);
    this.logger = logger;
    this.parameter = parameter;
  }

  @Override
  protected String exec(String nextLineIndent) {
    IndententationPrinter ip = new IndententationPrinter(nextLineIndent);

    addOptionalEnum(ip);
    addType(ip);

    return ip.toString();
  }

  private void addType(IndententationPrinter ip) {
    if (parameter.isMultivalued()) {
      ip.add("type: array");
      ip.add("items:");
      ip.nextLevel();
      DataTypeReferenceRenderer.renderBaseTypeWithOptFormat(ip, parameter.getTypeName(), parameter.getTypeFormat());
      ip.prevLevel();
    } else {
      DataTypeReferenceRenderer.renderBaseTypeWithOptFormat(ip, parameter.getTypeName(), parameter.getTypeFormat());
    }
  }

  private void addOptionalEnum(IndententationPrinter ip) {
    Set<String> constraintValues = parameter.getConstraintValues();
    if (constraintValues != null && !constraintValues.isEmpty()) {
      ObjectTypeRenderer.renderEnum(ip, new ArrayList<>(constraintValues));
    }
  }
}
