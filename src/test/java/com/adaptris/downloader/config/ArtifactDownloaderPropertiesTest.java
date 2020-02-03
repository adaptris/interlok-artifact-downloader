package com.adaptris.downloader.config;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class ArtifactDownloaderPropertiesTest {

  @Test
  public void testNew() {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();

    Assert.assertNull(properties.getNexusBaseUrl());
    Assert.assertNull(properties.getIndexUrl());
    Assert.assertNull(properties.getRepoBaseUrl());
    Assert.assertNotNull(properties.getRepos());
    Assert.assertNull(properties.getRepositoryReleases());
    Assert.assertNull(properties.getRepositorySnapshots());
    Assert.assertEquals(Collections.singletonList("public"), properties.getRepos());
    Assert.assertNull(properties.getResolverLogLevel());
    Assert.assertEquals(System.getProperty("user.home"), properties.getDestination());
    Assert.assertNotNull(properties.getExcludes());
    Assert.assertTrue(properties.getExcludes().isEmpty());
    Assert.assertNotNull(properties.getUnwanted());
    Assert.assertTrue(properties.getUnwanted().isEmpty());
    Assert.assertNull(properties.getIndexArtifactIdXpath());
    Assert.assertNull(properties.getCredentials());
  }

  @Test
  public void testNewWithData() {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setNexusBaseUrl("http://repo-base-url");
    properties.setIndexUrl("http://repo-base-url/indexes");
    properties.setRepoBaseUrl("http://repo-base-url/repositories");
    properties.setRepos(Arrays.asList("public, snapshots"));
    properties.setRepositoryReleases("releases");
    properties.setRepositorySnapshots("snapshots");
    properties.setResolverLogLevel("info");
    properties.setDestination("/destintation/dir");
    properties.setExcludes(Arrays.asList("group:artifact"));
    properties.setUnwanted(Arrays.asList("artifact"));
    properties.setIndexArtifactIdXpath("/xpath/text()");

    ArtifactDownloaderCredentialsProperties credentials = new ArtifactDownloaderCredentialsProperties();
    credentials.setRealm("realm");
    credentials.setHost("http://repo-base-url");
    credentials.setUsername("username");
    credentials.setPassword("password");
    properties.setCredentials(credentials);

    Assert.assertEquals("http://repo-base-url", properties.getNexusBaseUrl());
    Assert.assertEquals("http://repo-base-url/indexes", properties.getIndexUrl());
    Assert.assertEquals("http://repo-base-url/repositories", properties.getRepoBaseUrl());
    Assert.assertNotNull(properties.getRepos());
    Assert.assertEquals("releases", properties.getRepositoryReleases());
    Assert.assertEquals("snapshots", properties.getRepositorySnapshots());
    Assert.assertEquals(Arrays.asList("public, snapshots"), properties.getRepos());
    Assert.assertEquals("info", properties.getResolverLogLevel());
    Assert.assertEquals("/destintation/dir", properties.getDestination());
    Assert.assertNotNull(properties.getExcludes());
    Assert.assertEquals(Arrays.asList("group:artifact"), properties.getExcludes());
    Assert.assertEquals(Arrays.asList("artifact"), properties.getUnwanted());
    Assert.assertEquals("/xpath/text()", properties.getIndexArtifactIdXpath());

    Assert.assertNotNull(properties.getCredentials());
    Assert.assertEquals("realm", properties.getCredentials().getRealm());
    Assert.assertEquals("http://repo-base-url", properties.getCredentials().getHost());
    Assert.assertEquals("username", properties.getCredentials().getUsername());
    Assert.assertEquals("password", properties.getCredentials().getPassword());
  }

}
