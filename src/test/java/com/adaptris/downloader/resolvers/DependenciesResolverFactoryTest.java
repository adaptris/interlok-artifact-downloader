package com.adaptris.downloader.resolvers;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.adaptris.downloader.config.ArtifactDownloaderProperties;

public class DependenciesResolverFactoryTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testGetResolver() {
    DependenciesResolver resolver = new DependenciesResolverFactory(new ArtifactDownloaderProperties()).getResolver();

    Assert.assertNotNull(resolver);
  }

  @Test
  public void testGetResolverNullProperties() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("The Dependencies Resolver Properties should not be null");

    new DependenciesResolverFactory(null).getResolver();
  }

}
