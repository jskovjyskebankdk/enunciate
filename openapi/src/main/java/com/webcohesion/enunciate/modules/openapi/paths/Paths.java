package com.webcohesion.enunciate.modules.openapi.paths;

import java.util.ArrayList;
import java.util.List;

import com.webcohesion.enunciate.EnunciateConfiguration;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.PathSummary;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.resources.ResourceGroup;

public class Paths {
  @SuppressWarnings("unused") private EnunciateLogger logger;
  private EnunciateConfiguration configuration;
  private EnunciateContext context;
  private List<Endpoint> endpoints = new ArrayList<>();

  public Paths(EnunciateLogger logger, EnunciateConfiguration configuration, EnunciateContext context, List<ResourceApi> resourceApis) {
    this.logger = logger;
    this.configuration = configuration;
    this.context = context;
    
    for (ResourceApi resourceApi : resourceApis) {
      for (ResourceGroup resourceGroup : resourceApi.getResourceGroups()) {
        for (PathSummary pathSummary : resourceGroup.getPaths()) {
          Resource resource = findResourceForPath(resourceGroup, pathSummary.getPath());
          
          endpoints.add(new Endpoint(logger, resource, resourceApi, resourceGroup, pathSummary));
        }
      }
    }
  }
  
  private Resource findResourceForPath(ResourceGroup group, String path) {
    for (Resource r : group.getResources()) {
      if (r.getPath().equals(path)) {
        return r;
      }
    }
    throw new IllegalStateException("Failed to find resource for path " + path);
  }

  public boolean getIsEmpty() {
    return endpoints.isEmpty();
  }
  
  public List<Endpoint> getEndpoints() {
    return endpoints;
  }
}
