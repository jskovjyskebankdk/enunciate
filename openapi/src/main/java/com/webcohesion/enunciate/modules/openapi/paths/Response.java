package com.webcohesion.enunciate.modules.openapi.paths;

import static com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper.safeYamlString;

import java.util.List;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.modules.openapi.OpenApiDataTypeRenderer;
import com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper;

public class Response {
  @SuppressWarnings("unused") private final EnunciateLogger logger;
  private final int code;
  private final String mediaType;
  private final DataTypeReference dataType;
  private final List<Parameter> headers;
  private final String description;
  private final OpenApiDataTypeRenderer renderer;

  public Response(EnunciateLogger logger, int code, String mediaType, DataTypeReference dataType, List<Parameter> headers, String description) {
    this.logger = logger;
    this.code = code;
    this.mediaType = mediaType;
    this.dataType = dataType;
    this.headers = headers;
    this.description = YamlHelper.safeYamlString(description);
    renderer = new OpenApiDataTypeRenderer(logger, dataType, description);
    
    // TODO: Render headers
  }

  public String getCode() {
    return safeYamlString(Integer.toString(code));
  }

  public String getDescription() {
    return safeYamlString(description);
  }

  public String getMediaType() {
    return safeYamlString(mediaType);
  }
  
  public boolean getHasData() {
    return dataType != null;
  }
  
  public OpenApiDataTypeRenderer getRenderDataType() {
    return renderer;
  }
}
