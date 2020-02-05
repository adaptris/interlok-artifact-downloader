package com.adaptris.downloader.resolvers;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

import com.adaptris.downloader.config.ArtifactDownloaderProperties;

public class DependenciesResolverFactoryTest {

  @Test
  public void testGetResolver() {
    DependenciesResolver resolver = new DependenciesResolverFactory(new ArtifactDownloaderProperties()).getResolver();

    assertNotNull(resolver);
  }

  @Test
  public void testGetResolverNullProperties() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      new DependenciesResolverFactory(null).getResolver();
    });
    assertEquals("The Dependencies Resolver Properties should not be null", exception.getMessage());
  }

}
