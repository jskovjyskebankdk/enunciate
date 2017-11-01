package com.webcohesion.enunciate.modules.openapi;

import static com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper.safeYamlString;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.webcohesion.enunciate.api.datatype.BaseType;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.api.datatype.DataTypeReference.ContainerType;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.api.datatype.Namespace;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.PropertyMetadata;
import com.webcohesion.enunciate.api.datatype.Value;
import com.webcohesion.enunciate.modules.openapi.yaml.IndententationPrinter;

public class ObjectTypeRenderer {
  private ObjectTypeRenderer() {}
  
  public static void render(IndententationPrinter ip, DataType datatype, boolean syntaxIsJson) {
    ip.pushNextLevel();
    ip.add("title: ", safeYamlString(datatype.getLabel()));
    addOptionalSupertypeHeader(ip, datatype);

    ip.add("type: ", getBaseType(datatype));
    addOptionalRequired(ip, datatype);
    addOptionalProperties(ip, datatype, syntaxIsJson);
    addOptionalEnum(ip, datatype);
    addOptionalXml(ip, datatype);
    
    addOptionalExample(ip, datatype, syntaxIsJson);
    
    ip.popLevel();
  }

  private static void addOptionalSupertypeHeader(IndententationPrinter ip, DataType datatype) {
    List<DataTypeReference> supertypes = datatype.getSupertypes();
    if (supertypes == null || supertypes.isEmpty()) {
      return;
    }
    
    ip.add("allOf:");
    ip.nextLevel();
    ip.itemFollows();

    DataTypeReference superType = supertypes.iterator().next();
    DataTypeReferenceRenderer.addSchemaRef(ip, superType);

    ip.itemFollows();
  }

  private static void addOptionalRequired(IndententationPrinter ip, DataType datatype) {
    List<? extends Property> properties = datatype.getProperties();
    if (properties == null) {
      return;
    }
    
    List<Property> requiredProperties = new ArrayList<>();
    for (Property p : properties) {
      if (p.isRequired()) {
        requiredProperties.add(p);
      }
    }
    
    if (requiredProperties.isEmpty()) {
      return;
    }
    
    ip.add("required:");
    ip.nextLevel();
    for (Property p : requiredProperties) {
      ip.item(p.getName());
    }
    ip.prevLevel();
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
    if (values == null || values.isEmpty()) {
      return;
    }
    
    List<String> enums = new ArrayList<>();
    for (Value v: values) {
      enums.add(v.getValue());
    }
    renderEnum(ip, enums);
  }

  public static void renderEnum(IndententationPrinter ip, List<String> values) {
    ip.add("enum:");
    ip.nextLevel();
    for (String e : values) {
      ip.item(e);
    }
    ip.prevLevel();
  }

  private static void addOptionalProperties(IndententationPrinter ip, DataType datatype, boolean syntaxIsJson) {
    List<? extends Property> properties = datatype.getProperties();
    if (properties == null || properties.isEmpty()) {
      return;
    }
    
    ip.add("properties:");
    ip.nextLevel();
    for (Property p : properties) {
      addProperty(ip, datatype, p, syntaxIsJson);
    }
    ip.prevLevel();
  }

  private static void addProperty(IndententationPrinter ip, DataType datatype, Property p, boolean syntaxIsJson) {
    ip.add(p.getName(), ":");
    ip.nextLevel();
    if (datatype.getPropertyMetadata().containsKey("namespaceInfo")) {
      // TODO: would be nicer to have it on the property
      addNamespaceXml(ip, p);
    }
    
    addConstraints(ip, p);
    if (p.isReadOnly()) {
      ip.add("readonly: ", Boolean.toString(p.isReadOnly()));
    }
    
    DataTypeReferenceRenderer.render(ip, p.getDataType(), p.getDescription());
    
    addOptionalPropertyExample(ip, p, syntaxIsJson);
    
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
    PropertyMetadata metadata = AccessorProperty.getMetadata(p);
    if (metadata == null) {
      return;
    }

    String wrappedName = metadata.getValue();
    String namespace = metadata.getTitle();

    boolean renderWrappedName = wrappedName != null && !wrappedName.isEmpty();
    boolean renderAttribute = AccessorProperty.isAttribute(p);
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
    String xmlName = AccessorDataType.getXmlName(datatype);
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
  

  private static void addOptionalExample(IndententationPrinter ip, DataType datatype, boolean syntaxIsJson) {
    if (!syntaxIsJson) {
      return;
    }

    renderExample(ip, getExampleFromType(datatype, datatype.getBaseType(), null));
  }

  private static void addOptionalPropertyExample(IndententationPrinter ip, Property property, boolean syntaxIsJson) {
    if (!syntaxIsJson) {
      return;
    }
        
    BaseType baseType = property.getDataType() != null ? property.getDataType().getBaseType() : null;
    String specifiedExample = findSpecifiedExample(property);
    renderExample(ip, getExampleFromType(null, baseType, specifiedExample));
  }
  
  private static void renderExample(IndententationPrinter ip, String example) {
    if (example != null && !example.isEmpty()) {
      ip.add("example: ", safeYamlString(example));
    }
  }
  
  private static String getExampleFromType(DataType dataType, BaseType baseType, String specifiedExample) {
    if (baseType != null) {
      switch (baseType) {
        case object:
          if (dataType != null) {
            if (dataType.getBaseType() == BaseType.object) {
              Example example = dataType.getExample();
              if (example != null) {
                return example.getBody();
              }
            }
          }
        default:
          return specifiedExample;
      }
    }
    return null;
  }
  

  private static String findSpecifiedExample(Property property) {
    String example = null;

    JavaDoc.JavaDocTagList tags = property.getJavaDoc().get("documentationExample");
    if (tags != null && !tags.isEmpty()) {
      String tag = tags.get(0).trim();
      example = tag.isEmpty() ? null : tag;
    }

    DocumentationExample documentationExample = property.getAnnotation(DocumentationExample.class);
    if (documentationExample != null) {
      if (documentationExample.exclude()) {
        return null;
      }

      example = documentationExample.value();
      example = "##default".equals(example) ? null : example;
    }

    if (example != null && (property.getDataType() == null || property.getDataType().getBaseType() == BaseType.string)) {
      example = "\"" + new String(new JsonStringEncoder().quoteAsString(example)) + "\"";
    }

    return example;
  }
}
