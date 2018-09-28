package com.adaptris.downloader.services.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.downloader.config.ArtifactDownloaderException;
import com.adaptris.downloader.config.ArtifactDownloaderProperties;
import com.adaptris.downloader.config.ArtifactUnresolvedDownloaderException;
import com.adaptris.downloader.config.InvalidGroupIdDownloaderException;
import com.adaptris.downloader.resolvers.DependenciesResolver;
import com.adaptris.downloader.resolvers.DependenciesResolverException;
import com.adaptris.downloader.resolvers.DependenciesResolverFactory;
import com.adaptris.downloader.services.ArtifactService;


public class ArtifactServiceImplTest {

  private static final String GROUP = "com.adaptris";
  private static final String ARTIFACT = "artifact";
  private static final String VERSION = "version";
  private static final String GROUP_ARTIFACT_VERSION = "com.adaptris-artifact-version";
  private static final String EXCLUDE_ARTIFACT = "com.adaptris:exclude";

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Mock
  private ArtifactDownloaderProperties properties;
  @Mock
  private DependenciesResolverFactory dependenciesResolverFactory;
  @InjectMocks
  private final ArtifactService artifactService = new ArtifactServiceImpl();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    doReturn(".").when(properties).getDestination();
  }

  @Test
  public void testDownload() throws DependenciesResolverException, ArtifactDownloaderException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(Collections.singletonList(new File(GROUP_ARTIFACT_VERSION))).when(dependenciesResolver)
    .resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir());
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    List<File> artifacts = artifactService.download(GROUP, ARTIFACT, VERSION, null, null);

    Assert.assertEquals(1, artifacts.size());
    Assert.assertEquals(GROUP_ARTIFACT_VERSION, artifacts.get(0).getName());
    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir());
  }

  @Test
  public void testDownloadWithExcludes() throws DependenciesResolverException, ArtifactDownloaderException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(Collections.singletonList(new File(GROUP_ARTIFACT_VERSION))).when(dependenciesResolver).resolveArtifacts(
        GROUP, ARTIFACT, VERSION, null, getCacheDir(), EXCLUDE_ARTIFACT);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    List<File> artifacts = artifactService.download(GROUP, ARTIFACT, VERSION, null, EXCLUDE_ARTIFACT);

    Assert.assertEquals(1, artifacts.size());
    Assert.assertEquals(GROUP_ARTIFACT_VERSION, artifacts.get(0).getName());
    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), EXCLUDE_ARTIFACT);
  }

  @Test
  public void testDownloadFails() throws DependenciesResolverException, ArtifactDownloaderException {
    expectedEx.expect(ArtifactDownloaderException.class);
    expectedEx.expectCause(IsInstanceOf.<Throwable>instanceOf(DependenciesResolverException.class));

    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doThrow(new DependenciesResolverException(new IOException("error"))).when(dependenciesResolver).resolveArtifacts(GROUP,
        ARTIFACT, VERSION, null, getCacheDir());
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    artifactService.download(GROUP, ARTIFACT, VERSION, null, null);

    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir());
  }

  @Test
  public void testDownloadFailsInvalidGroupId() throws DependenciesResolverException, ArtifactDownloaderException {
    expectedEx.expect(InvalidGroupIdDownloaderException.class);
    expectedEx.expectMessage("[com.invalid] is not a valid group Id. It need to start with [com.adaptris]");

    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    artifactService.download("com.invalid", ARTIFACT, VERSION, null, null);

    verify(dependenciesResolver, never()).resolveArtifacts("com.invalid", ARTIFACT, VERSION, null, getCacheDir());
  }

  @Test
  public void testDownloadFailsInvalidGroupIdTwo() throws DependenciesResolverException, ArtifactDownloaderException {
    expectedEx.expect(InvalidGroupIdDownloaderException.class);
    expectedEx.expectMessage("[com.adaptrisinvalid] is not a valid group Id. It need to start with [com.adaptris]");

    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    artifactService.download("com.adaptrisinvalid", ARTIFACT, VERSION, null, null);

    verify(dependenciesResolver, never()).resolveArtifacts("com.adaptrisinvalid", ARTIFACT, VERSION, null, getCacheDir());
  }

  @Test
  public void testDownloadFailsWithUnresolveDependencyNotFound() throws DependenciesResolverException, ArtifactDownloaderException {
    expectedEx.expect(ArtifactUnresolvedDownloaderException.class);
    expectedEx.expectMessage("Artifact could not be resolved");

    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    List<String> dependencyErrors = new ArrayList<>();
    dependencyErrors.add("unresolved dependency: com.adaptris:artifact:version: not found");
    doThrow(new DependenciesResolverException(dependencyErrors)).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT,
        VERSION, null, getCacheDir());
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    artifactService.download(GROUP, ARTIFACT, VERSION, null, null);

    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir());
  }

  @Test
  public void testDownloadFailsWithDownloadFailed() throws DependenciesResolverException, ArtifactDownloaderException {
    expectedEx.expect(ArtifactUnresolvedDownloaderException.class);
    expectedEx.expectMessage("[download failed: com.adaptris:artifact:version]");

    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    List<String> dependencyErrors = new ArrayList<>();
    dependencyErrors.add("download failed: com.adaptris:artifact:version");
    doThrow(new DependenciesResolverException(dependencyErrors)).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT,
        VERSION, null, getCacheDir());
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    artifactService.download(GROUP, ARTIFACT, VERSION, null, null);

    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir());
  }

  private String getCacheDir() {
    return "." + File.separator + ArtifactServiceImpl.INTERLOK_ARTIFACT_CACHE;
  }

}
