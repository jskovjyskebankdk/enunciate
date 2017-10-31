package com.webcohesion.enunciate.modules.openapi.yaml;

import java.util.Arrays;
import java.util.List;

public class YamlHelper {
  private YamlHelper() {}
  // as per http://www.yaml.org/spec/1.2/spec.html#id2776092
  private static final List<Character> VALID_CHAR_AFTER_ESCAPE = Arrays.asList('0', 'a', 'b', 't', '\t', 'n', 'v', 'f', 'r', 'e', ' ', '"', '/', '\\', 'N', '_', 'L', 'P', 'x', 'u', 'U');
  
  public static final String safeYamlString(String str) {
    if (str == null) {
      return null;
    }
    String woNewlines = str.replace("\n", "\\n").replace("\r", "\\r");
    verifyStringIsValidYaml(woNewlines);
    
    if (woNewlines.startsWith("\"") && woNewlines.endsWith("\"")) {
      return woNewlines;
    }
    return '"' + woNewlines.replace("\"", "\\\"") + '"';
  }

  private static void verifyStringIsValidYaml(String str) {
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c == '\\') {
        if (i+1 == str.length()) {
          throw new IllegalStateException("String ends in escape character: " + str);
        }
        char second = str.charAt(i+1);
        if (!VALID_CHAR_AFTER_ESCAPE.contains(second)) {
          throw new IllegalStateException("String has bad character (" + second + ") after escape: " + str);
        }
      }
    }
  }
}
