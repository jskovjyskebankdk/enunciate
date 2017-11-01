package com.webcohesion.enunciate.modules.openapi.components;

import static com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper.safeYamlString;

import java.util.regex.Pattern;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.modules.freemarker.Typed1ArgTemplateMethod;
import com.webcohesion.enunciate.modules.openapi.ObjectTypeRenderer;
import com.webcohesion.enunciate.modules.openapi.yaml.IndententationPrinter;

public class SchemaRenderer extends Typed1ArgTemplateMethod<String, String> {
  private static final Pattern VALID_REF_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\.\\-_]+$");
  
  @SuppressWarnings("unused") private final EnunciateLogger logger;
  private final DataType datatype;

  private boolean syntaxIsJson;

  public SchemaRenderer(EnunciateLogger logger, DataType datatype, boolean syntaxIsJson) {
    super(String.class);
    this.logger = logger;
    this.datatype = datatype;
    this.syntaxIsJson = syntaxIsJson;
  }

  @Override
  protected String exec(String nextLineIndent) {
    IndententationPrinter ip = new IndententationPrinter(nextLineIndent);
    renderLines(ip);
    return ip.toString();
  }
  
  private void renderLines(IndententationPrinter ip) {
    ip.add(getRefId() + ":");
    ObjectTypeRenderer.render(ip, datatype, syntaxIsJson);
  }

  private String getRefId() {
    String slug = datatype.getSlug();
    if (!VALID_REF_ID_PATTERN.matcher(slug).matches()) {
      throw new IllegalStateException("Invalid reference id '" + slug + "' for datatype " + datatype);
    }
    return safeYamlString(slug);
  }
}
