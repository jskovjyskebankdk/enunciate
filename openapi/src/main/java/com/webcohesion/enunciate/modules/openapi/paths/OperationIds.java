package com.webcohesion.enunciate.modules.openapi.paths;

import java.util.Map;
import java.util.TreeMap;

public class OperationIds {
  private static final Map<String, String> OPERATIONID_BY_SLUG = new TreeMap<>();

  private OperationIds() {}

  public static String getOperationId(String slug, String developerLabel) {
    if (!OPERATIONID_BY_SLUG.containsKey(slug)) {
      computeId(slug, developerLabel);
    }
    return OPERATIONID_BY_SLUG.get(slug);
  }
  
  private static void computeId(String slug, String developerLabel) {
    int suffix = 2;
    String assignment = developerLabel;
    String root = developerLabel;
    while (OPERATIONID_BY_SLUG.values().contains(assignment)) {
      assignment = root + suffix++;
    }
  
    OPERATIONID_BY_SLUG.put(slug, assignment);
  }
}
