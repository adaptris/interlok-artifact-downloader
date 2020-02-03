package com.adaptris.downloader.services.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.downloader.config.ArtifactDownloaderProperties;

public class OptionalComponentsServiceImplTest {

  @Mock
  private ArtifactDownloaderProperties properties;
  @InjectMocks
  private OptionalComponentsServiceImpl optionalComponentsService = new OptionalComponentsServiceImpl();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    doReturn("https://nexus.adaptris.net/nexus").when(properties).getNexusBaseUrl();
    doReturn(
        "${artifact.downloader.nexusBaseUrl}/service/local/lucene/search?repositoryId=${repository}&g=com.adaptris&v=${artifact.version}&p=jar")
    .when(properties).getIndexUrl();
    doReturn("releases").when(properties).getRepositoryReleases();
    doReturn("snapshots").when(properties).getRepositorySnapshots();
    doReturn(Collections.singletonList("interlok-core")).when(properties).getUnwanted();
    doReturn("/searchNGResponse/data/artifact/artifactId/text()").when(properties).getIndexArtifactIdXpath();
  }

  @Test
  public void testLoad() throws Exception {
    List<String> artifacts = optionalComponentsService.loadArtifacts("3.9.3-RELEASE");

    assertFalse(artifacts.isEmpty());
    assertTrue(artifacts.contains("interlok-json"));
    assertFalse(artifacts.contains("interlok-core"));
  }

  @Test
  public void testLoadEmpty() throws Exception {

    assertTrue(optionalComponentsService.loadArtifacts("INVALID-VERSION").isEmpty());
  }

  @Test
  public void testGetRepositoryRelease() throws Exception {

    assertEquals("releases", optionalComponentsService.getRepository("VERSION-RELEASE"));
  }

  @Test
  public void testGetRepositoryNull() throws Exception {

    assertEquals("releases", optionalComponentsService.getRepository(null));
  }

  @Test
  public void testGetRepositorySnapshot() throws Exception {

    assertEquals("snapshots", optionalComponentsService.getRepository("VERSION-SNAPSHOT"));
  }

}
