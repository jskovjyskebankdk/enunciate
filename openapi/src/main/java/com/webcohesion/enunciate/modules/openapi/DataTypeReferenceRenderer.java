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
    if (dtr == null) {
      throw new IllegalStateException("Cannot render null data type");
    }
    
    if (description != null && !description.isEmpty()) {
      ip.add("description: ", description);
    }

    DataType value = dtr.getValue();
    List<ContainerType> containers = dtr.getContainers();
    if (value != null) {
      if (containers != null && !containers.isEmpty()) {
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
      } else {
        renderSimpleType(ip, dtr);
      }
    }
  }

  private static void renderSimpleType(IndententationPrinter ip, DataTypeReference dtr) {
    String baseType = getBaseType(dtr);
    String format = getFormatNameFor(dtr);
    if (format != null) {
      renderBaseTypeWithFormat(ip, baseType, format);
    } else {
      if (dtr.getBaseType() == BaseType.object) {
        renderObsoletedFileFormat(ip);
      } else {
        renderBaseType(ip, baseType);
      }
    }
  }

  public static void renderBaseType(IndententationPrinter ip, String baseType) {
    ip.add("type: ", baseType);
  }

  public static void renderBaseTypeWithOptFormat(IndententationPrinter ip, String baseType, BaseTypeFormat format) {
    if (format != null) {
      String fomatStr = BaseTypeToOpenApiType.toSwaggerFormat(format);
      renderBaseTypeWithFormat(ip, baseType, fomatStr);
    } else {
      renderBaseType(ip, baseType);
    }
  }

  private static void renderBaseTypeWithFormat(IndententationPrinter ip, String baseType, String format) {
    renderBaseType(ip, baseType);
    ip.add("format: ", format);
  }

  public static void renderObsoletedFileFormat(IndententationPrinter ip) {
    ip.add("type: string");
    ip.add("format: binary"); // TODO: Need to check type for base64/binary - assume binary for now
  }

  private static void addSchemaRef(IndententationPrinter ip, DataType value) {
    addSchemaSlugReference(ip, value.getSlug());
  }

  public static void addSchemaRef(IndententationPrinter ip, DataTypeReference ref) {
    String slug = ref.getSlug();
    if (slug != null && !slug.isEmpty()) {
      addSchemaSlugReference(ip, slug);
    }
  }

  private static void addSchemaSlugReference(IndententationPrinter ip, String slug) {
    ip.add("$ref: \"#/components/schemas/" + slug + "\"");
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
