package com.webcohesion.enunciate.modules.openapi;

import java.util.List;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference.ContainerType;
import com.webcohesion.enunciate.api.datatype.Namespace;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.PropertyMetadata;
import com.webcohesion.enunciate.api.datatype.Value;
import com.webcohesion.enunciate.modules.openapi.yaml.IndententationPrinter;

public class ObjectTypeRenderer {
  private ObjectTypeRenderer() {}
  
  public static void render(IndententationPrinter ip, DataType datatype) {
    ip.nextLevel();
    ip.add("type: ", getBaseType(datatype));
    // TODO: title
    // TODO: required
    // TODO: supertypes
    
    addOptionalProperties(ip, datatype);
    addOptionalXml(ip, datatype);
    addOptionalEnum(ip, datatype);
    
    // TODO: example
  }

  private static String getBaseType(DataType datatype) {
    switch (datatype.getBaseType()) {
    case bool:
      return "boolean";
    case number:
      return "number";
    case string:
      return "string";
    default:
      return "object";
    }
  }
  
  private static void addOptionalEnum(IndententationPrinter ip, DataType datatype) {
    List<? extends Value> values = datatype.getValues();
    if (values != null && !values.isEmpty()) {
      ip.add("enum:");
      ip.nextLevel();
      for (Value v : values) {
        ip.add("- ", v.getValue());
      }
      ip.prevLevel();
    }
  }

  private static void addOptionalProperties(IndententationPrinter ip, DataType datatype) {
    List<? extends Property> properties = datatype.getProperties();
    if (properties == null || properties.isEmpty()) {
      return;
    }
    
    ip.add("properties:");
    ip.nextLevel();
    for (Property p : properties) {
      addProperty(ip, datatype, p);
    }
    ip.prevLevel();
  }

  private static void addProperty(IndententationPrinter ip, DataType datatype, Property p) {
    ip.add(p.getName(), ":");
    ip.nextLevel();
    if (datatype.getPropertyMetadata().containsKey("namespaceInfo")) {
      // TODO: would be nicer to have it on the property
      addNamespaceXml(ip, p);
    }
    // TODO: example:
    
    addConstraints(ip, p);
    if (p.isReadOnly()) {
      ip.add("readonly: ", Boolean.toString(p.isReadOnly()));
    }
    
    DataTypeReferenceRenderer.render(ip, p.getDataType(), p.getDescription());
    
    ip.prevLevel();
  }

  private static void addConstraints(IndententationPrinter ip, Property p) {
    List<ContainerType> containers = p.getDataType().getContainers();
    boolean isArray = containers != null && !containers.isEmpty();
    
    Max max = p.getAnnotation(Max.class);
    DecimalMax decimalMax = p.getAnnotation(DecimalMax.class);
    if (max != null) {
      ip.add("maximum: ", Long.toString(max.value()));
    } else if (decimalMax != null) {
      ip.add("maximum: ", decimalMax.value());
      ip.add("exclusiveMaximum: ", Boolean.toString(!decimalMax.inclusive()));
    }

    Min min = p.getAnnotation(Min.class);
    DecimalMin decimalMin = p.getAnnotation(DecimalMin.class);
    if (min != null) {
      ip.add("minimum: ", Long.toString(min.value()));
    } else if (decimalMin != null) {
      ip.add("minimum: ", decimalMin.value());
      ip.add("exclusiveMinimum: ", Boolean.toString(!decimalMin.inclusive()));
    }

    Size size = p.getAnnotation(Size.class);
    if (size != null) {
      if (isArray) {
        ip.add("maxItems: ", Integer.toString(size.max()));
        ip.add("minItems: ", Integer.toString(size.min()));
      }
      else {
        ip.add("maxLength: ", Integer.toString(size.max()));
        ip.add("minLength: ", Integer.toString(size.min()));
      }
    }

    Pattern mustMatchPattern = p.getAnnotation(Pattern.class);
    if (mustMatchPattern != null) {
      ip.add("pattern: ", mustMatchPattern.regexp());
    }
  }

  private static void addNamespaceXml(IndententationPrinter ip, Property p) {
    PropertyMetadata metadata = p.getMetadata();
    if (metadata == null) {
      return;
    }

    String wrappedName = metadata.getValue();
    String namespace = metadata.getTitle();

    boolean renderWrappedName = wrappedName != null && !wrappedName.isEmpty();
    boolean renderAttribute = p.isAttribute();
    boolean renderNamespace = namespace != null && !namespace.isEmpty();
    
    if (!renderWrappedName && !renderAttribute && !renderNamespace) {
      return;
    }
    
    ip.add("xml:");
    ip.nextLevel();
    if (renderWrappedName) {
      ip.add("name: ", wrappedName);
      ip.add("wrapped: true");
    }
    if (renderAttribute) {
      ip.add("attribute: ", Boolean.TRUE.toString());
    }
    if (renderNamespace) {
      ip.add("namespace: ", namespace);
    }
    ip.prevLevel();
  }

  private static void addOptionalXml(IndententationPrinter ip, DataType datatype) {
    String xmlName = datatype.getXmlName();
    Namespace namespace = datatype.getNamespace();
    if (xmlName != null && !xmlName.isEmpty()) {
      ip.add("xml:");
      ip.nextLevel();
      ip.add("name: ", xmlName);
      if (namespace != null) {
        String uri = namespace.getUri();
        if (uri != null && !uri.isEmpty()) {
          ip.add("namespace: ", uri);
        }
      }
      ip.prevLevel();
    }
  }
}
