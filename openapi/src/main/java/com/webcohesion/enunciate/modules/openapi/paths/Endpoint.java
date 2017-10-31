package com.webcohesion.enunciate.modules.openapi.paths;

import static com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper.safeYamlString;

import java.util.ArrayList;
import java.util.List;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.PathSummary;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.resources.ResourceGroup;

public class Endpoint {
  private List<Resource> resources;
  private ResourceApi resourceApi;
  private ResourceGroup resourceGroup;
  private PathSummary pathSummary;
  private List<Operation> operations = new ArrayList<>();

  public Endpoint(EnunciateLogger logger, List<Resource> resources, ResourceApi resourceApi, ResourceGroup resourceGroup, PathSummary pathSummary) {
    this.resources = resources;
    this.resourceApi = resourceApi;
    this.resourceGroup = resourceGroup;
    this.pathSummary = pathSummary;
    
    for (Resource resource : resources) {
      for (Method m : resource.getMethods()) {
        operations.add(new Operation(logger, m, resourceGroup));
      }
    }    
  }

  public String getPath() {
    return safeYamlString(pathSummary.getPath());
  }
  
  public String getResourceGroupTag() {
    return safeYamlString(resourceGroup.getLabel());
  }
  
  public boolean getResourceGroupDeprecated() {
    return resourceGroup.getDeprecated() != null;
  }
  
  public List<Operation> getOperations() {
    return operations;
  }
}
