package com.adaptris.downloader.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class ArtifactDownloaderPropertiesTest {

  @Test
  public void testNew() {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();

    assertNull(properties.getNexusBaseUrl());
    assertNull(properties.getIndexUrl());
    assertNull(properties.getRepoBaseUrl());
    assertNotNull(properties.getRepos());
    assertNull(properties.getRepositoryReleases());
    assertNull(properties.getRepositorySnapshots());
    assertEquals(Collections.singletonList("public"), properties.getRepos());
    assertNull(properties.getResolverLogLevel());
    assertEquals(System.getProperty("user.home"), properties.getDestination());
    assertNotNull(properties.getExcludes());
    assertTrue(properties.getExcludes().isEmpty());
    assertNotNull(properties.getUnwanted());
    assertTrue(properties.getUnwanted().isEmpty());
    assertNull(properties.getIndexArtifactIdXpath());
    assertNull(properties.getCredentials());
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

    assertEquals("http://repo-base-url", properties.getNexusBaseUrl());
    assertEquals("http://repo-base-url/indexes", properties.getIndexUrl());
    assertEquals("http://repo-base-url/repositories", properties.getRepoBaseUrl());
    assertNotNull(properties.getRepos());
    assertEquals("releases", properties.getRepositoryReleases());
    assertEquals("snapshots", properties.getRepositorySnapshots());
    assertEquals(Arrays.asList("public, snapshots"), properties.getRepos());
    assertEquals("info", properties.getResolverLogLevel());
    assertEquals("/destintation/dir", properties.getDestination());
    assertNotNull(properties.getExcludes());
    assertEquals(Arrays.asList("group:artifact"), properties.getExcludes());
    assertEquals(Arrays.asList("artifact"), properties.getUnwanted());
    assertEquals("/xpath/text()", properties.getIndexArtifactIdXpath());

    assertNotNull(properties.getCredentials());
    assertEquals("realm", properties.getCredentials().getRealm());
    assertEquals("http://repo-base-url", properties.getCredentials().getHost());
    assertEquals("username", properties.getCredentials().getUsername());
    assertEquals("password", properties.getCredentials().getPassword());
  }

}
