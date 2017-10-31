package com.webcohesion.enunciate.modules.openapi;

import java.util.Map;
import java.util.EnumMap;

import com.webcohesion.enunciate.api.datatype.BaseTypeFormat;

public class BaseTypeToOpenApiType {

  private static final Map<BaseTypeFormat, String> baseformat2openapiformat = new EnumMap<BaseTypeFormat, String>(BaseTypeFormat.class);
  static {
    baseformat2openapiformat.put(BaseTypeFormat.INT32, "int32");
    baseformat2openapiformat.put(BaseTypeFormat.INT64, "int64");
    baseformat2openapiformat.put(BaseTypeFormat.FLOAT, "float");
    baseformat2openapiformat.put(BaseTypeFormat.DOUBLE, "double");
  }

  public static String toSwaggerFormat(BaseTypeFormat format) {
    return format == null ? null : baseformat2openapiformat.get(format);
  }
}
