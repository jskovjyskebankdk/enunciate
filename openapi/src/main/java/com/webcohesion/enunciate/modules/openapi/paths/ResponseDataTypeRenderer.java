package com.webcohesion.enunciate.modules.openapi.paths;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.modules.freemarker.Typed1ArgTemplateMethod;
import com.webcohesion.enunciate.modules.openapi.DataTypeReferenceRenderer;
import com.webcohesion.enunciate.modules.openapi.yaml.IndententationPrinter;

public class ResponseDataTypeRenderer extends Typed1ArgTemplateMethod<String, String> {
  @SuppressWarnings("unused") private final EnunciateLogger logger;
  private final DataTypeReference dataType;
  private String description;

  public ResponseDataTypeRenderer(EnunciateLogger logger, DataTypeReference dataType, String description) {
    super(String.class);
    this.logger = logger;
    this.dataType = dataType;
    this.description = description;
  }

  @Override
  protected String exec(String nextLineIndent) {
    IndententationPrinter ip = new IndententationPrinter(nextLineIndent);

    DataTypeReferenceRenderer.render(ip, dataType, description);
    return ip.toString();
  }
}