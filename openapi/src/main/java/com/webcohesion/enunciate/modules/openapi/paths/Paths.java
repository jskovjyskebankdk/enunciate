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
          List<Resource> resources = findResourcesForPath(resourceGroup, pathSummary.getPath());
          
          endpoints.add(new Endpoint(logger, resources, resourceApi, resourceGroup, pathSummary));
        }
      }
    }
  }
  
  private List<Resource> findResourcesForPath(ResourceGroup group, String path) {
    List<Resource> resources = new ArrayList<>();
    for (Resource r : group.getResources()) {
      if (r.getPath().equals(path)) {
        resources.add(r);
      }
    }
    return resources;
  }

  public boolean getIsEmpty() {
    return endpoints.isEmpty();
  }
  
  public List<Endpoint> getEndpoints() {
    return endpoints;
  }
}
