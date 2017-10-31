package com.webcohesion.enunciate.modules.openapi.paths;

import static com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper.safeYamlString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.api.resources.StatusCode;
import com.webcohesion.enunciate.modules.openapi.FindBestDataTypeMethod;
import com.webcohesion.enunciate.modules.openapi.FindBestDataTypeMethod.MediaAndType;

public class Operation {
  private static final String DUMMY_SUCCESS_MEDIA_TYPE = "*/*";
  private static final Collection<String> DEFAULT_201_METHODS = Collections.singletonList("POST");
  private static final Collection<String> DEFAULT_204_METHODS = Arrays.asList("PATCH", "PUT", "DELETE");

  private final ResourceGroup resourceGroup;
  private final Method method;
  private final List<Param> parameters = new ArrayList<>();
  private final List<Response> responses = new ArrayList<>();
  private EntityRenderer entityRenderer;

  public Operation(EnunciateLogger logger, Method method, ResourceGroup resourceGroup) {
    this.method = method;
    this.resourceGroup = resourceGroup;
    entityRenderer = new EntityRenderer(method);
    
    for (Parameter parameter : method.getParameters()) {
      parameters.add(new Param(logger, parameter));
    }
    
    computeResponses(logger);
  }

  public String getHttpMethod() {
    return method.getHttpMethod().toLowerCase();
  }
  
  public String getDescription() {
    return safeYamlString(method.getDescription());
  }
  
  public String getSummary() {
    return safeYamlString(method.getDescription().replaceAll("\\..*", "."));
  }
  
  public String getDeprecated() {
    return Boolean.toString(method.getDeprecated() != null || resourceGroup.getDeprecated() != null);
  }
  
  public String getOperationId() {
    return OperationIds.getOperationId(method.getSlug(), method.getDeveloperLabel());
  }
  
  public boolean getHasParameters() {
    return !parameters.isEmpty();
  }
  
  public List<Param> getParameters() {
    return parameters;
  }
  
  public boolean getHasEntity() {
    return method.getRequestEntity() != null;
  }

  public String getIsEntityRequired() {
    // TODO: this is probably a bad assumption
    return Boolean.TRUE.toString();
  }
  
  public String getEntityDescription() {
    String description = method.getRequestEntity().getDescription();
    return safeYamlString(description == null ? "" : description);
  }
  
  public EntityRenderer getRenderEntity() {
    return entityRenderer;
  }
  
  public List<Response> getResponses() {
    return responses;
  }

  private void computeResponses(EnunciateLogger logger) {
    @SuppressWarnings("unchecked")
    List<Parameter> successHeaders = (List<Parameter>)method.getResponseHeaders();
    Entity responseEntity = method.getResponseEntity();
    DataTypeReference successDataType = FindBestDataTypeMethod.findBestDataType(responseEntity);
    boolean successResponseFound = false;
    if (method.getResponseCodes() != null) {
      for (StatusCode code : method.getResponseCodes()) {
        boolean successResponse = code.getCode() >= 200 && code.getCode() < 300;
        
        MediaAndType mediaAndType = FindBestDataTypeMethod.findBestMediaAndType(code.getMediaTypes());
        
        DataTypeReference dataType = mediaAndType == null ? null : mediaAndType.type;
        dataType = dataType == null && successResponse ? successDataType : dataType;
        List<Parameter> headers = successResponse ? successHeaders : Collections.<Parameter>emptyList();
        
        String mediaType = mediaAndType == null ? DUMMY_SUCCESS_MEDIA_TYPE : mediaAndType.media.getMediaType();
        responses.add(new Response(logger, code.getCode(), mediaType, dataType, headers, code.getCondition()));
        successResponseFound |= successResponse;
      }
    }

    if (!successResponseFound) {
      int code = DEFAULT_201_METHODS.contains(method.getHttpMethod().toUpperCase()) ? 201 : DEFAULT_204_METHODS.contains(method.getHttpMethod().toUpperCase()) ? 204 : 200;
      String description = responseEntity != null ? responseEntity.getDescription() : "Success";
      responses.add(new Response(logger, code, DUMMY_SUCCESS_MEDIA_TYPE, successDataType, successHeaders, description));
    }
  }
}
