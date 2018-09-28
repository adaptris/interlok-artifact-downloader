package com.adaptris.downloader.config;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.adaptris.downloader.controllers.ArtifactController;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

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
  }

  private void configureSwagger() {
    // Turn on Swagger
    register(ApiListingResource.class);
    register(SwaggerSerializers.class);
    // Configure Swagger
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("1.0.0");
    beanConfig.setBasePath("/api");
    beanConfig.setTitle("Interlok Artifact Downloader Api Documentation");
    beanConfig.setDescription("Interlok Artifact Downloader Api web service documentation.");
    beanConfig.setResourcePackage("com.adaptris.downloader.controllers");
    beanConfig.setPrettyPrint(true);
    beanConfig.setScan(true);
  }

}
