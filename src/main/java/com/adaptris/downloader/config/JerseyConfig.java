package com.adaptris.downloader.config;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.adaptris.downloader.controllers.ArtifactController;
import com.adaptris.downloader.controllers.StarterController;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Component
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

  public JerseyConfig() {
    configureSwagger();
    // Controllers Packages
    // Not working well with spring boot
    // packages("com.adaptris.downloader.controllers");
    // Using register instead
    register(ArtifactController.class);
    register(StarterController.class);
  }

  private void configureSwagger() {
    // Turn on Swagger
    register(new OpenApiResource());

    // Configure Swagger
    OpenAPI oas = new OpenAPI();
    Info info = new Info();
    info.setVersion("1.0.0");
    info.setTitle("Interlok Artifact Downloader Api Documentation");
    info.setDescription("Interlok Artifact Downloader Api web service documentation.");

    oas.info(info);
    SwaggerConfiguration oasConfig = new SwaggerConfiguration()
        .openAPI(oas)
        .prettyPrint(true)
        .resourcePackages(Stream.of("com.adaptris.downloader.controllers").collect(Collectors.toSet()));

    try {
      new JaxrsOpenApiContextBuilder<>()
        .openApiConfiguration(oasConfig)
        .buildContext(true);
    } catch (OpenApiConfigurationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
