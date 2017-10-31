package com.webcohesion.enunciate.modules.openapi.paths;

import static com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper.safeYamlString;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.modules.openapi.OpenApiParameterRenderer;
import com.webcohesion.enunciate.modules.openapi.OpenApiParameterTypes;
import com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper;

public class Param {
  private final Parameter parameter;
  private final OpenApiParameterTypes type;
  private final OpenApiParameterRenderer renderer;

  public Param(EnunciateLogger logger, Parameter parameter) {
    this.parameter = parameter;
    type = OpenApiParameterTypes.fromEnunciateTypeLabel(parameter.getTypeLabel());
    renderer = new OpenApiParameterRenderer(logger, parameter, type);
  }

  public String getName() {
    return safeYamlString(parameter.getName());
  }

  public String getTypeLabel() {
    return type.toYamlString();
  }
  
  public String getDescription() {
    return YamlHelper.safeYamlString(parameter.getDescription());
  }

  public String getRequired() {
    return Boolean.toString(type == OpenApiParameterTypes.PATH);
  }
  
  public OpenApiParameterRenderer getRenderDataType() {
    return renderer;
  }
}
