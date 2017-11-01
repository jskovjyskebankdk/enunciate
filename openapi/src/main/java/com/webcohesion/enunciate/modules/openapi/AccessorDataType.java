package com.webcohesion.enunciate.modules.openapi;

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.modules.jaxb.api.impl.ComplexDataTypeImpl;

/**
 * Provides access to methods in DataType implementations in other modules.
 * 
 * @author Jesper Skov (jskov@jyskebank.dk)
 */
public class AccessorDataType {
  private AccessorDataType() {}
  
  public static String getXmlName(DataType dt) {
    if (dt instanceof ComplexDataTypeImpl) {
      return ((ComplexDataTypeImpl)dt).getXmlName();
    }
    return null;
  }
}
