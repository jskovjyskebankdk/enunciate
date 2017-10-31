package com.webcohesion.enunciate.modules.openapi;

/**
 * Parameter types according to
 * https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#parameterObject
 * 
 * @author jb1811
 */
public enum OpenApiParameterTypes {
  QUERY,
  HEADER,
  PATH,
  COOKIE,
  UNKNOWN_FIXME;
  
  public static OpenApiParameterTypes fromEnunciateTypeLabel(String typeLabel) {
    String type = typeLabel.toLowerCase();
    if (type.contains("path")) {
      return OpenApiParameterTypes.PATH;
    }
    if (type.contains("query")) {
      return OpenApiParameterTypes.QUERY;
    }
    if (type.contains("header")) {
      return OpenApiParameterTypes.HEADER;
    }
    if (type.contains("matrix")) {
      return OpenApiParameterTypes.UNKNOWN_FIXME;
    }
    throw new IllegalStateException("Unmapped typeLabel " + typeLabel);
  }
  
  public String toYamlString() {
    return name().toLowerCase();
  }
}
