package com.webcohesion.enunciate.modules.openapi.paths;

import static com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper.safeYamlString;

import java.util.HashMap;
import java.util.Map;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.modules.openapi.OpenApiParameterRenderer;
import com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper;

public class Param {
  private static final String SIMPLE = "simple";
  private static final String FORM = "form";
  private static final String QUERY = "query";
  private static final String PATH = "path";
  private static final String MATRIX = "matrix";
  private static final String HEADER = "header";
  private static final String COOKIE = "cookie";
  private final Parameter parameter;
  private final OpenApiParameterRenderer renderer;
  private final boolean required;
  private String paramPassedIn;
  private String paramStyle;

  private static final Map<String, String> typeLabel2in = new HashMap<>();
  static {
    typeLabel2in.put(COOKIE, COOKIE);
    typeLabel2in.put(HEADER, HEADER);
    typeLabel2in.put(MATRIX, PATH);
    typeLabel2in.put(PATH, PATH);
    typeLabel2in.put(QUERY, QUERY);
  }

  private static final Map<String, String> typeLabel2style = new HashMap<>();
  static {
    typeLabel2style.put(COOKIE, FORM);
    typeLabel2style.put(HEADER, SIMPLE);
    typeLabel2style.put(MATRIX, MATRIX);
    typeLabel2style.put(PATH, SIMPLE);
    typeLabel2style.put(QUERY, FORM);
  }

  public Param(EnunciateLogger logger, Parameter parameter) {
    this.parameter = parameter;
    
    renderer = new OpenApiParameterRenderer(logger, parameter);
    String typeLabel = parameter.getTypeLabel();
    paramPassedIn = typeLabel2in.get(typeLabel);
    paramStyle = typeLabel2style.get(typeLabel);
    
    if (paramPassedIn == null) {
      throw new IllegalStateException("Do not know how to pass type " + typeLabel);
    }
    if (paramStyle == null) {
      throw new IllegalStateException("Do not know style to use with type " + typeLabel);
    }

    required = PATH.equals(paramPassedIn);
  }

  public String getName() {
    return safeYamlString(parameter.getName());
  }

  public String getInFormat() {
    return paramPassedIn;
  }
  
  public String getDescription() {
    return YamlHelper.safeYamlString(parameter.getDescription());
  }

  public String getRequired() {
    return Boolean.toString(required);
  }
  
  public OpenApiParameterRenderer getRenderDataType() {
    return renderer;
  }
  
  public String getStyle() {
    return paramStyle;
  }
}
