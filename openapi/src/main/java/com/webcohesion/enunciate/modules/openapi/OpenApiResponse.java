/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.openapi;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.modules.openapi.yaml.YamlHelper;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class OpenApiResponse {
  @SuppressWarnings("unused") private final EnunciateLogger logger;
  private final int code;
  private final DataTypeReference dataType;
  private final List<? extends Parameter> headers;
  private final String description;
  private final OpenApiDataTypeRenderer renderer;

  public OpenApiResponse(EnunciateLogger logger, int code, DataTypeReference dataType,
      List<? extends Parameter> headers, String description) {
    this.logger = logger;
    this.code = code;
    this.dataType = dataType;
    this.headers = headers;
    this.description = YamlHelper.safeYamlString(description);
    renderer = new OpenApiDataTypeRenderer(logger, dataType, description);
  }

  public int getCode() {
    return code;
  }

  public DataTypeReference getDataType() {
    return dataType;
  }

  public String getMediaType() {
    return "FIXME:*/*";
  }

  public OpenApiDataTypeRenderer getRenderDataType() {
    return renderer;
  }

  public List<? extends Parameter> getHeaders() {
    return headers;
  }

  public String getDescription() {
    return description;
  }
}
