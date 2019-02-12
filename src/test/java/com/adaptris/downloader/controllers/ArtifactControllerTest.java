package com.adaptris.downloader.controllers;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.adaptris.downloader.config.ArtifactDownloaderException;
import com.adaptris.downloader.config.ArtifactDownloaderProperties;
import com.adaptris.downloader.config.DownloaderClientErrorException;
import com.adaptris.downloader.config.InvalidGroupIdDownloaderException;
import com.adaptris.downloader.resolvers.DependenciesResolver;
import com.adaptris.downloader.resolvers.DependenciesResolverException;
import com.adaptris.downloader.resolvers.DependenciesResolverFactory;
import com.adaptris.downloader.resources.ArtifactAndDependendencies;
import com.adaptris.downloader.resources.Usage;
import com.adaptris.downloader.services.ArtifactService;
import com.adaptris.downloader.services.impl.ArtifactServiceImpl;

public class ArtifactControllerTest {

  private static final String GROUP = "com.adaptris";
  private static final String ARTIFACT = "artifact";
  private static final String VERSION = "version";
  private static final String EXCLUDE_ARTIFACT = "com.adaptris:exclude";

  URL resource = getClass().getClassLoader().getResource("artifact-controller-test");

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Mock
  private ArtifactDownloaderProperties properties;
  @Mock
  private DependenciesResolverFactory dependenciesResolverFactory;
  @Spy
  @InjectMocks
  private ArtifactService artifactService = new ArtifactServiceImpl();
  @InjectMocks
  private final ArtifactController artifactController = new ArtifactController();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    doReturn(".").when(properties).getDestination();
  }

  @Test
  public void testUsage() {
    Usage usage = artifactController.usage();

    Assert.assertNotNull(usage);
    Assert.assertEquals("/{group}/{artifact}/{version}", usage.getLink());
  }

  @Test
  public void testResolveSync() throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(getDependencyFiles()).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    ArtifactAndDependendencies artifactAndDependendencies = artifactController.resolveSync(GROUP, ARTIFACT, VERSION, false,
        Collections.emptyList());

    assertValidArtifactAndDependendencies(artifactAndDependendencies);
    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testResolveSyncWithExcludes() throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(getDependencyFiles()).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(),
        false, EXCLUDE_ARTIFACT);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    ArtifactAndDependendencies artifactAndDependendencies = artifactController.resolveSync(GROUP, ARTIFACT, VERSION, false,
        Collections.singletonList(EXCLUDE_ARTIFACT));

    assertValidArtifactAndDependendencies(artifactAndDependendencies);
    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false, EXCLUDE_ARTIFACT);
  }

  @Test
  public void testResolveSyncInvalidGroupId() throws ArtifactDownloaderException, DependenciesResolverException {
    expectedEx.expect(DownloaderClientErrorException.class);
    expectedEx.expectMessage("HTTP 400 Bad Request");

    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    doThrow(new InvalidGroupIdDownloaderException("invalid.group", GROUP)).when(artifactService).download("invalid.group", ARTIFACT,
        VERSION, null, false, "");

    artifactController.resolveSync("invalid.group", ARTIFACT, VERSION, false, Collections.emptyList());

    verify(dependenciesResolver, never()).resolveArtifacts("com.invalid", ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testResolveSyncUnresolvedDependency() throws ArtifactDownloaderException, DependenciesResolverException {
    expectedEx.expect(DownloaderClientErrorException.class);
    expectedEx.expectMessage("HTTP 404 Not Found");

    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    List<String> dependencyErrors = new ArrayList<>();
    dependencyErrors.add("unresolved dependency: com.adaptris:artifact:version: not found");
    doThrow(new DependenciesResolverException(dependencyErrors)).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null,
        getCacheDir(), false);

    artifactController.resolveSync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList());

    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testResolveAsync()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException, URISyntaxException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(getDependencyFiles()).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.resolveAsync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(ArtifactAndDependendencies.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<ArtifactAndDependendencies>() {
      @Override
      public boolean matches(Object argument) {
        assertValidArtifactAndDependendencies((ArtifactAndDependendencies) argument);
        return true;
      }
    }));
    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testResolveAsyncWithExcludes() throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException, URISyntaxException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(getDependencyFiles()).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false, EXCLUDE_ARTIFACT);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.resolveAsync(GROUP, ARTIFACT, VERSION, false, Collections.singletonList(EXCLUDE_ARTIFACT), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(ArtifactAndDependendencies.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<ArtifactAndDependendencies>() {
      @Override
      public boolean matches(Object argument) {
        assertValidArtifactAndDependendencies((ArtifactAndDependendencies) argument);
        return true;
      }
    }));
    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false, EXCLUDE_ARTIFACT);
  }

  @Test
  public void testResolveAsyncInvalidGroupId() throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    doThrow(new InvalidGroupIdDownloaderException("invalid.group", GROUP)).when(artifactService).download("invalid.group", ARTIFACT,
        VERSION, null, false, "");

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.resolveAsync("invalid.group", ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(DownloaderClientErrorException.class));
    verify(asyncResponse).resume(argThat(new ThrowableMessageMatcher<>(new StringContains("HTTP 400 Bad Request"))));
    verify(dependenciesResolver, never()).resolveArtifacts("com.invalid", ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testResolveAsyncUnresolvedDependency()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    List<String> dependencyErrors = new ArrayList<>();
    dependencyErrors.add("unresolved dependency: com.adaptris:artifact:version: not found");
    doThrow(new DependenciesResolverException(dependencyErrors)).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null,
        getCacheDir(), false);

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.resolveAsync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(DownloaderClientErrorException.class));
    verify(asyncResponse).resume(argThat(new ThrowableMessageMatcher<>(new StringContains("HTTP 404 Not Found"))));
    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testDownloadSync() throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(getDependencyFiles()).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    Response response = artifactController.downloadSync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList());

    assertValidResponse(response);

    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testDownloadSyncWithExcludes() throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(getDependencyFiles()).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(),
        false, EXCLUDE_ARTIFACT);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    Response response = artifactController.downloadSync(GROUP, ARTIFACT, VERSION, false, Collections.singletonList(EXCLUDE_ARTIFACT));

    assertValidResponse(response);

    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false, EXCLUDE_ARTIFACT);
  }

  @Test
  public void testDownloadSyncInvalidGroupId() throws ArtifactDownloaderException, DependenciesResolverException {
    expectedEx.expect(BadRequestException.class);
    expectedEx.expectMessage("[invalid.group] is not a valid group Id. It need to start with [com.adaptris]");

    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    doThrow(new InvalidGroupIdDownloaderException("invalid.group", GROUP)).when(artifactService).download("invalid.group", ARTIFACT,
        VERSION, null, false, "");

    artifactController.downloadSync("invalid.group", ARTIFACT, VERSION, false, Collections.emptyList());

    verify(dependenciesResolver, never()).resolveArtifacts("com.invalid", ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testDownloadSyncUnresolvedDependency() throws ArtifactDownloaderException, DependenciesResolverException {
    expectedEx.expect(NotFoundException.class);
    expectedEx.expectMessage("Artifact could not be resolved");

    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    List<String> dependencyErrors = new ArrayList<>();
    dependencyErrors.add("unresolved dependency: com.adaptris:artifact:version: not found");
    doThrow(new DependenciesResolverException(dependencyErrors)).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null,
        getCacheDir(), false);

    artifactController.downloadSync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList());

    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testDownloadAsync()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException, URISyntaxException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(getDependencyFiles()).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.downloadAsync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(Response.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<ArtifactAndDependendencies>() {
      @Override
      public boolean matches(Object argument) {
        assertValidResponse((Response) argument);
        return true;
      }
    }));
    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testDownloadAsyncWithExcludes()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException, URISyntaxException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(getDependencyFiles()).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(),
        false, EXCLUDE_ARTIFACT);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.downloadAsync(GROUP, ARTIFACT, VERSION, false, Collections.singletonList(EXCLUDE_ARTIFACT), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(Response.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<ArtifactAndDependendencies>() {
      @Override
      public boolean matches(Object argument) {
        assertValidResponse((Response) argument);
        return true;
      }
    }));
    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false, EXCLUDE_ARTIFACT);
  }

  @Test
  public void testDownloadAsyncInvalidGroupId() throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    doThrow(new InvalidGroupIdDownloaderException("invalid.group", GROUP)).when(artifactService).download("invalid.group", ARTIFACT,
        VERSION, null, false, "");

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.downloadAsync("invalid.group", ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(BadRequestException.class));
    verify(asyncResponse).resume(argThat(new ThrowableMessageMatcher<>(
        new StringContains("[invalid.group] is not a valid group Id. It need to start with [com.adaptris]"))));
    verify(dependenciesResolver, never()).resolveArtifacts("com.invalid", ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testDownloadAsyncUnresolvedDependency()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    List<String> dependencyErrors = new ArrayList<>();
    dependencyErrors.add("unresolved dependency: com.adaptris:artifact:version: not found");
    doThrow(new DependenciesResolverException(dependencyErrors)).when(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null,
        getCacheDir(), false);

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.downloadAsync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(NotFoundException.class));
    verify(asyncResponse).resume(argThat(new ThrowableMessageMatcher<>(
        new StringContains("Artifact could not be resolved"))));
    verify(dependenciesResolver).resolveArtifacts(GROUP, ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  private String getCacheDir() {
    return "." + File.separator + ".interlok-artifact-cache";
  }

  private List<File> getDependencyFiles() throws URISyntaxException {
    Path resourcePath = Paths.get(resource.toURI());
    List<File> dependencies = Arrays.asList(resourcePath.resolve(ARTIFACT + ".jar").toFile(),
        resourcePath.resolve("dependency.jar").toFile());
    return dependencies;
  }

  private void assertValidArtifactAndDependendencies(ArtifactAndDependendencies artifactAndDependendencies) {
    Assert.assertNotNull(artifactAndDependendencies);
    Assert.assertEquals(ARTIFACT + ".jar", artifactAndDependendencies.getArtifact());
    Assert.assertEquals(1, artifactAndDependendencies.getDependencies().size());
    Assert.assertEquals("dependency.jar", artifactAndDependendencies.getDependencies().get(0));
  }

  private void assertValidResponse(Response response) {
    Assert.assertNotNull(response);
    Assert.assertEquals(200, response.getStatus());
    Assert.assertEquals(MediaType.valueOf("application/zip"), response.getMediaType());
    Assert.assertEquals("attachment; filename=\"" + ARTIFACT + "-" + VERSION + ".zip\"",
        response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION));
    // Check the files are in the response byte array
    String baosString = new String((byte[]) response.getEntity());
    Assert.assertTrue(baosString.contains("artifact.jar"));
    Assert.assertTrue(baosString.contains("dependency.jar"));
  }

}
