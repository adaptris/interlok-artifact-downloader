package com.adaptris.downloader.config;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class ArtifactDownloaderPropertiesTest {

  @Test
  public void testNew() {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();

    Assert.assertNull(properties.getRepoBaseUrl());
    Assert.assertNotNull(properties.getRepos());
    Assert.assertEquals(Collections.singletonList("public"), properties.getRepos());
    Assert.assertNull(properties.getResolverLogLevel());
    Assert.assertEquals(System.getProperty("user.home"), properties.getDestination());
    Assert.assertNotNull(properties.getExcludes());
    Assert.assertTrue(properties.getExcludes().isEmpty());
    Assert.assertNull(properties.getCredentials());
  }

  @Test
  public void testNewWithData() {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setRepoBaseUrl("http://repo-base-url");
    properties.setRepos(Arrays.asList("public, snapshots"));
    properties.setResolverLogLevel("info");
    properties.setDestination("/destintation/dir");
    properties.setExcludes(Arrays.asList("group:artifact"));

    ArtifactDownloaderCredentialsProperties credentials = new ArtifactDownloaderCredentialsProperties();
    credentials.setRealm("realm");
    credentials.setHost("http://repo-base-url");
    credentials.setUsername("username");
    credentials.setPassword("password");
    properties.setCredentials(credentials);

    Assert.assertEquals("http://repo-base-url", properties.getRepoBaseUrl());
    Assert.assertNotNull(properties.getRepos());
    Assert.assertEquals(Arrays.asList("public, snapshots"), properties.getRepos());
    Assert.assertEquals("info", properties.getResolverLogLevel());
    Assert.assertEquals("/destintation/dir", properties.getDestination());
    Assert.assertNotNull(properties.getExcludes());
    Assert.assertEquals(Arrays.asList("group:artifact"), properties.getExcludes());

    Assert.assertNotNull(properties.getCredentials());
    Assert.assertEquals("realm", properties.getCredentials().getRealm());
    Assert.assertEquals("http://repo-base-url", properties.getCredentials().getHost());
    Assert.assertEquals("username", properties.getCredentials().getUsername());
    Assert.assertEquals("password", properties.getCredentials().getPassword());
  }

}
