package com.webcohesion.enunciate.modules.openapi.yaml;

public class YamlHelper {
  private YamlHelper() {}
  
  public static final String safeYamlString(String str) {
    if (str == null) {
      return null;
    }
    String woNewlines = str.replace("\n", "\\n").replace("\r", "\\r");
    if (woNewlines.startsWith("\"") && woNewlines.endsWith("\"")) {
      return woNewlines;
    }
    return '"' + woNewlines.replace("\"", "\\\"") + '"';
  }
}
