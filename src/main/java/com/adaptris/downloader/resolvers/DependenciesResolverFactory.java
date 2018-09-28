package com.adaptris.downloader.resolvers;

import org.springframework.stereotype.Component;

import com.adaptris.downloader.resolvers.ivy.IvyDependenciesResolver;

@Component
public class DependenciesResolverFactory {

  private final DependenciesResolverProperties properties;

  public DependenciesResolverFactory(DependenciesResolverProperties properties) {
    this.properties = properties;
    if (properties == null) {
      throw new IllegalArgumentException("The Dependencies Resolver Properties should not be null");
    }
  }

  public DependenciesResolver getResolver() {
    return new IvyDependenciesResolver(properties);
  }

}
