package com.webcohesion.enunciate.modules.openapi.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.Namespace;
import com.webcohesion.enunciate.api.datatype.Syntax;

public class Components {
  private final List<Schema> schemas = new ArrayList<>();

  public Components(EnunciateLogger logger, Set<Syntax> syntaxes) {
    for (Syntax syntax: syntaxes) {
      boolean syntaxIsJson = syntax.isAssignableToMediaType("application/json");
      for (Namespace namespace: syntax.getNamespaces()) {
        for (DataType datatype: namespace.getTypes()) {
          schemas.add(new Schema(logger, datatype, syntaxIsJson));
        }
      }
    }
  }
  
  public boolean getIsEmpty() {
    return schemas.isEmpty();
  }
  
  public List<Schema> getSchemas() {
    return schemas;
  }

}
