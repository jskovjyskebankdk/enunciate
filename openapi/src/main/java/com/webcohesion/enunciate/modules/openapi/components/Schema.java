package com.webcohesion.enunciate.modules.openapi.components;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.datatype.DataType;

public class Schema {
  private SchemaRenderer renderer;

  public Schema(EnunciateLogger logger, DataType datatype) {
    this.renderer = new SchemaRenderer(logger, datatype);
  }

  public SchemaRenderer getRender() {
    return renderer;
  }

}
