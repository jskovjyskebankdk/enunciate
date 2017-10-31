package com.webcohesion.enunciate.modules.openapi.paths;

import static com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper.safeYamlString;

import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.modules.freemarker.Typed1ArgTemplateMethod;
import com.webcohesion.enunciate.modules.openapi.DataTypeReferenceRenderer;
import com.webcohesion.enunciate.modules.openapi.FindBestDataTypeMethod;
import com.webcohesion.enunciate.modules.openapi.FindBestDataTypeMethod.MediaAndType;
import com.webcohesion.enunciate.modules.openapi.yaml.IndententationPrinter;

public class EntityRenderer extends Typed1ArgTemplateMethod<String, String> {
  private MediaAndType mediaAndType;
  private boolean hasMedia;
  private String mediaWithFallback;

  public EntityRenderer(Method method) {
    super(String.class);
    
    mediaAndType = FindBestDataTypeMethod.findBestMediaAndType(method.getRequestEntity());
    hasMedia = mediaAndType != null && mediaAndType.media != null;
    mediaWithFallback = hasMedia ? mediaAndType.media.getMediaType() : "*/*";
  }
  
  @Override
  protected String exec(String nextLineIndent) {
      IndententationPrinter ip = new IndententationPrinter(nextLineIndent);

      ip.add(safeYamlString(mediaWithFallback), ":");
      ip.nextLevel();
      ip.add("schema:");
      ip.nextLevel();
      if (hasMedia) {
        DataTypeReferenceRenderer.addSchemaRef(ip, mediaAndType.type);
      } else {
        DataTypeReferenceRenderer.renderObsoletedFileFormat(ip);
      }
      
      ip.prevLevel();
      ip.prevLevel();
      return ip.toString();
  }
}
