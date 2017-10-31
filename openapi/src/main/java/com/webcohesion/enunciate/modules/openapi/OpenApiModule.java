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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.module.ApiFeatureProviderModule;
import com.webcohesion.enunciate.module.ApiRegistryAwareModule;
import com.webcohesion.enunciate.module.ApiRegistryProviderModule;
import com.webcohesion.enunciate.module.BasicGeneratingModule;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.module.EnunciateModule;
import com.webcohesion.enunciate.modules.openapi.components.Components;
import com.webcohesion.enunciate.modules.openapi.info.Info;
import com.webcohesion.enunciate.modules.openapi.paths.Paths;
import com.webcohesion.enunciate.util.freemarker.FileDirective;

import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * <h1>OpenAPI Module</h1>
 * Based on Swagger Module.
 * @author Ryan Heaton
 */
public class OpenApiModule extends BasicGeneratingModule implements ApiFeatureProviderModule, ApiRegistryAwareModule, ApiRegistryProviderModule {

  private ApiRegistry apiRegistry;
  private EnunciateLogger logger;

  @Override
  public void init(Enunciate engine) {
    super.init(engine);

    this.logger = enunciate.getLogger();
  }
  
  @Override
  public String getName() {
    return "openapi";
  }

  @Override
  public void setApiRegistry(ApiRegistry registry) {
    this.apiRegistry = registry;
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Arrays.asList((DependencySpec) new DependencySpec() {
      @Override
      public boolean accept(EnunciateModule module) {
        return !getName().equals(module.getName()) && module instanceof ApiRegistryProviderModule;
      }

      @Override
      public boolean isFulfilled() {
        return true;
      }


      @Override
      public String toString() {
        return "all api registry provider modules";
      }
    });
  }

  /**
   * The URL to "openapi.fmt".
   *
   * @return The URL to "openapi.fmt".
   */
  protected URL getTemplateURL() throws MalformedURLException {
    String template = getFreemarkerProcessingTemplate();
    if (template != null) {
      return this.enunciate.getConfiguration().resolveFile(template).toURI().toURL();
    }
    else {
      return OpenApiModule.class.getResource("openapi.fmt");
    }
  }

  @Override
  public void call(EnunciateContext context) {
    //no-op; work happens with the swagger interface description.
  }

  @Override
  public ApiRegistry getApiRegistry() {
    return new ApiRegistry() {
      @Override
      public List<ServiceApi> getServiceApis(ApiRegistrationContext context) {
        return Collections.emptyList();
      }

      @Override
      public List<ResourceApi> getResourceApis(ApiRegistrationContext context) {
        return Collections.emptyList();
      }

      @Override
      public Set<Syntax> getSyntaxes(ApiRegistrationContext context) {
        return Collections.emptySet();
      }

      @Override
      public InterfaceDescriptionFile getSwaggerUI(ApiRegistrationContext context) {
        List<ResourceApi> resourceApis = apiRegistry.getResourceApis(context);

        if (resourceApis == null || resourceApis.isEmpty()) {
          info("No resource APIs registered: Swagger UI will not be generated.");
        }

        return new SwaggerInterfaceDescription(resourceApis, context);
      }
    };
  }

  private class SwaggerInterfaceDescription implements InterfaceDescriptionFile {

    private final List<ResourceApi> resourceApis;
    private final ApiRegistrationContext apiRegistrationContext;

    public SwaggerInterfaceDescription(List<ResourceApi> resourceApis, ApiRegistrationContext context) {
      this.resourceApis = resourceApis;
      this.apiRegistrationContext = context;
    }

    @Override
    public String getHref() {
      return getDocsSubdir() + "/index.html";
    }

    @Override
    public void writeTo(File srcDir) throws IOException {
      srcDir.mkdirs();
      String subdir = getDocsSubdir();
      if (subdir != null) {
        srcDir = new File(srcDir, subdir);
        srcDir.mkdirs();
      }

      Map<String, Object> model = new HashMap<>();
      model.put("info", new Info(logger, enunciate.getConfiguration(), context)); 
      model.put("paths", new Paths(logger, enunciate.getConfiguration(), context, resourceApis));
      
      Set<Syntax> syntaxes = apiRegistry.getSyntaxes(apiRegistrationContext);
      model.put("components", new Components(logger, syntaxes));
      
      // REWORK BELOW
      model.put("apis", this.resourceApis);
      model.put("syntaxes", apiRegistry.getSyntaxes(apiRegistrationContext));
      model.put("file", new FileDirective(srcDir, logger));
      model.put("uniqueMediaTypesFor", new UniqueMediaTypesForMethod());
      model.put("jsonExamplesFor", new JsonExamplesForMethod());
      model.put("jsonExampleFor", new JsonExampleForMethod());
      model.put("findBestDataType", new FindBestDataTypeMethod());
      model.put("host", getHost());
      model.put("schemes", getSchemes());
      model.put("basePath", getBasePath());

      buildBase(srcDir);
      try {
        processTemplate(getTemplateURL(), model);
      }
      catch (TemplateException e) {
        throw new EnunciateException(e);
      }

      Set<File> jsonFilesToValidate = new HashSet<>();
      gatherJsonFiles(jsonFilesToValidate, srcDir);
      ObjectMapper mapper = new ObjectMapper();
      for (File file : jsonFilesToValidate) {
        FileReader reader = new FileReader(file);
        try {
          mapper.readTree(reader);
        }
        catch (JsonProcessingException e) {
          warn("Error processing %s.", file.getAbsolutePath());
          throw e;
        }
        finally {
          reader.close();
        }
      }

      FileArtifact swaggerArtifact = new FileArtifact(getName(), "swagger", srcDir);
      swaggerArtifact.setPublic(false);
      OpenApiModule.this.enunciate.addArtifact(swaggerArtifact);
    }
  }

  protected String getHost() {
    String host = this.config.getString("[@host]", null);

    if (host == null) {
      String root = enunciate.getConfiguration().getApplicationRoot();
      if (root != null) {
        try {
          URI uri = URI.create(root);
          host = uri.getHost();
          if (uri.getPort() > 0) {
            host += ":" + uri.getPort();
          }
        }
        catch (IllegalArgumentException e) {
          host = null;
        }
      }
    }

    return host;
  }

  protected String[] getSchemes() {
    return this.config.getStringArray("scheme");
  }

  protected String getBasePath() {
    String basePath = this.config.getString("[@basePath]", null);

    if (basePath == null) {
      String root = enunciate.getConfiguration().getApplicationRoot();
      if (root != null) {
        try {
          URI uri = URI.create(root);
          basePath = uri.getPath();
        }
        catch (IllegalArgumentException e) {
          basePath = null;
        }
      }

      while (basePath != null && basePath.endsWith("/")) {
        basePath = basePath.substring(0, basePath.length() - 1);
      }
    }

    return basePath;
  }

  /**
   * Processes the specified template with the given model.
   *
   * @param templateURL The template URL.
   * @param model       The root model.
   */
  public String processTemplate(URL templateURL, Object model) throws IOException, TemplateException {
    debug("Processing template %s.", templateURL);
    Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
    configuration.setLocale(new Locale("en", "US"));

    configuration.setTemplateLoader(new URLTemplateLoader() {
      protected URL getURL(String name) {
        try {
          return new URL(name);
        }
        catch (MalformedURLException e) {
          return null;
        }
      }
    });

    configuration.setTemplateExceptionHandler(new TemplateExceptionHandler() {
      public void handleTemplateException(TemplateException templateException, Environment environment, Writer writer) throws TemplateException {
        throw templateException;
      }
    });

    configuration.setLocalizedLookup(false);
    configuration.setDefaultEncoding("UTF-8");
    configuration.setObjectWrapper(new OpenApiUIObjectWrapper());
    Template template = configuration.getTemplate(templateURL.toString());
    StringWriter unhandledOutput = new StringWriter();
    template.process(model, unhandledOutput);
    unhandledOutput.close();
    return unhandledOutput.toString();
  }

  /**
   * Builds the base output directory.
   */
  protected void buildBase(File buildDir) throws IOException {
    String base = getBase();
    if (base == null) {
      InputStream discoveredBase = OpenApiModule.class.getResourceAsStream("/META-INF/enunciate/swagger-base.zip");
      if (discoveredBase == null) {
        debug("Default base to be used for swagger base.");
        enunciate.unzip(loadDefaultBase(), buildDir);

        String css = getCss();
        if (css != null) {
          enunciate.copyFile(enunciate.getConfiguration().resolveFile(css), new File(new File(buildDir, "css"), "screen.css"));
        }
      }
      else {
        debug("Discovered documentation base at /META-INF/enunciate/swagger-base.zip");
        enunciate.unzip(discoveredBase, buildDir);
      }
    }
    else {
      File baseFile = enunciate.getConfiguration().resolveFile(base);
      if (baseFile.isDirectory()) {
        debug("Directory %s to be used as the documentation base.", baseFile);
        enunciate.copyDir(baseFile, buildDir);
      }
      else {
        debug("Zip file %s to be extracted as the documentation base.", baseFile);
        enunciate.unzip(new FileInputStream(baseFile), buildDir);
      }
    }
  }

  private void gatherJsonFiles(Set<File> bucket, File buildDir) {
    File[] files = buildDir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.getName().endsWith(".json")) {
          bucket.add(file);
        }
        else if (file.isDirectory()) {
          gatherJsonFiles(bucket, file);
        }
      }
    }
  }

  /**
   * Loads the default base for the swagger ui.
   *
   * @return The default base for the swagger ui.
   */
  protected InputStream loadDefaultBase() {
    return OpenApiModule.class.getResourceAsStream("/swagger-ui.zip");
  }

  /**
   * The cascading stylesheet to use instead of the default.  This is ignored if the 'base' is also set.
   *
   * @return The cascading stylesheet to use.
   */
  public String getCss() {
    return this.config.getString("[@css]", null);
  }

  public String getFreemarkerProcessingTemplate() {
    return this.config.getString("[@freemarkerProcessingTemplate]", null);
  }

  /**
   * The swagger "base".  The swagger base is the initial contents of the directory
   * where the swagger ui will be output.  Can be a zip file or a directory.
   *
   * @return The documentation "base".
   */
  public String getBase() {
    return this.config.getString("[@base]", null);
  }

  public Set<String> getFacetIncludes() {
    List<Object> includes = this.config.getList("facets.include[@name]");
    Set<String> facetIncludes = new TreeSet<>();
    for (Object include : includes) {
      facetIncludes.add(String.valueOf(include));
    }
    return facetIncludes;
  }

  public Set<String> getFacetExcludes() {
    List<Object> excludes = this.config.getList("facets.exclude[@name]");
    Set<String> facetExcludes = new TreeSet<>();
    for (Object exclude : excludes) {
      facetExcludes.add(String.valueOf(exclude));
    }
    return facetExcludes;
  }

  public String getDocsSubdir() {
    return this.config.getString("[@docsSubdir]", "ui");
  }

}
