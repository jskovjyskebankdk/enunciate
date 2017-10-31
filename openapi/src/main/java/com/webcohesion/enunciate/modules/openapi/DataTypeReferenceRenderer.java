package com.webcohesion.enunciate.modules.openapi;

import java.util.List;

import com.webcohesion.enunciate.api.datatype.BaseType;
import com.webcohesion.enunciate.api.datatype.BaseTypeFormat;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.DataTypeReference.ContainerType;
import com.webcohesion.enunciate.modules.openapi.yaml.IndententationPrinter;

public class DataTypeReferenceRenderer {
  private DataTypeReferenceRenderer() {}
  
  public static void render(IndententationPrinter ip, DataTypeReference dtr, String description) {
    DataType value = dtr.getValue();
    List<ContainerType> containers = dtr.getContainers();
    if (value != null) {
      if (containers != null && !containers.isEmpty()) {
        if (description != null && !description.isEmpty()) {
          ip.add("description: ", description);
        }
        for (ContainerType ct : containers) {
          if (!ct.isMap()) {
            ip.add("type: array");
            ip.add("items:");
          } else {
            ip.add("type: object");
            ip.add("additionalProperties:");
          }
          ip.nextLevel();
          addSchemaRef(ip, value);
          ip.prevLevel();
        }
      } else {
        addSchemaRef(ip, value);
      }
    } else {
      if (containers != null && !containers.isEmpty()) {
        if (description != null && !description.isEmpty()) {
          ip.add("description: ", description);
        }
        for (ContainerType ct : containers) {
          if (!ct.isMap()) {
            ip.add("type: array");
            ip.add("items:");
          } else {
            ip.add("type: object");
            ip.add("additionalProperties:");
          }
          ip.nextLevel();
          ip.add("type: ", getBaseType(dtr));
          ip.prevLevel();
        }
      } else if (getFormatNameFor(dtr) != null) {
        if (description != null && !description.isEmpty()) {
          ip.add("description: ", description);
        }
        ip.add("type: ", getBaseType(dtr));
        ip.add("format: ", getFormatNameFor(dtr));
      } else {
        if (description != null && !description.isEmpty()) {
          ip.add("description: ", description);
        }
        
        if (dtr.getBaseType() == BaseType.object) {
          ip.add("type: string");
          ip.add("format: binary"); // TODO: Need to check type for base64/binary - assume binary for now
        } else {
          ip.add("type: ", getBaseType(dtr));
        }
      }
    }
  }

  private static void addSchemaRef(IndententationPrinter ip, DataType value) {
    ip.add("$ref: \"#/components/schemas/" + value.getSlug() + "\"");
  }

  private static String getFormatNameFor(DataTypeReference dtr) { 
    return BaseTypeToOpenApiType.toSwaggerFormat(dtr.getBaseTypeFormat());
  }

  private static String getBaseType(DataTypeReference dtr) {
    BaseType baseType = dtr.getBaseType();
    BaseTypeFormat format = dtr.getBaseTypeFormat();

    switch (baseType) {
      case bool:
        return "boolean";
      case number:
        if (BaseTypeFormat.INT32 == format || BaseTypeFormat.INT64 == format) {
          return "integer";
        } else {
          return "number";
        }
      case string:
        return "string";
      case object:
        return "object";
      default:
        throw new IllegalStateException("Called with unhandled type " + baseType);
    }
  }
}
