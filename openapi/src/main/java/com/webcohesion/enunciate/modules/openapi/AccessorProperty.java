package com.webcohesion.enunciate.modules.openapi;

import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.PropertyMetadata;
import com.webcohesion.enunciate.modules.jaxb.api.impl.PropertyImpl;

/**
 * Provides access to methods in Property implementations in other modules.
 * 
 * @author Jesper Skov (jskov@jyskebank.dk)
 */
public class AccessorProperty {
  private AccessorProperty() {}
  
  public static PropertyMetadata getMetadata(Property p) {
    if (p instanceof PropertyImpl) {
      return ((PropertyImpl)p).getNamespaceInfo();
    }
    return null;
  }
 
  public static boolean isAttribute(Property p) {
    if (p instanceof PropertyImpl) {
      return ((PropertyImpl)p).isAttribute();
    }
    return false;
  }
}
