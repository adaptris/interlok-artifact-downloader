package com.adaptris.downloader.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.adaptris.downloader.config.ArtifactDownloaderException;
import com.adaptris.downloader.config.ArtifactDownloaderProperties;
import com.adaptris.downloader.config.ArtifactUnresolvedDownloaderException;
import com.adaptris.downloader.config.DownloaderClientErrorException;
import com.adaptris.downloader.config.InvalidGroupIdDownloaderException;
import com.adaptris.downloader.resolvers.DependenciesResolver;
import com.adaptris.downloader.resolvers.DependenciesResolverException;
import com.adaptris.downloader.resolvers.DependenciesResolverFactory;
import com.adaptris.downloader.resources.ArtifactAndDependendencies;
import com.adaptris.downloader.resources.Usage;
import com.adaptris.downloader.services.ArtifactService;
import com.adaptris.downloader.services.OptionalComponentsService;
import com.adaptris.downloader.services.impl.ArtifactServiceImpl;
import com.adaptris.downloader.services.impl.OptionalComponentsServiceImpl;

public class ArtifactControllerTest {

  private static final String GROUP = "com.adaptris";
  private static final String ARTIFACT = "artifact";
  private static final String VERSION = "version";
  private static final String EXCLUDE_ARTIFACT = "com.adaptris:exclude";

  URL resource = getClass().getClassLoader().getResource("artifact-controller-test");

  @Mock
  private ArtifactDownloaderProperties properties;
  @Mock
  private DependenciesResolverFactory dependenciesResolverFactory;
  @Spy
  private ArtifactService artifactService = new ArtifactServiceImpl();
  @Spy
  private OptionalComponentsService optionalComponentsService = new OptionalComponentsServiceImpl();
  @InjectMocks
  private final ArtifactController artifactController = new ArtifactController();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    doReturn(".").when(properties).getDestination();
  }

  @Test
  public void testUsage() {
    Usage usage = artifactController.usage();

    assertNotNull(usage);
    assertEquals("/{group}/{artifact}/{version}", usage.getLink());
  }

  @Test
  public void testList() throws ArtifactDownloaderException {
    doReturn(Collections.singletonList("interlok-json")).when(optionalComponentsService).loadArtifacts(VERSION);

    List<String> artifacts = artifactController.list(VERSION);

    assertNotNull(artifacts);
    assertEquals(Collections.singletonList("interlok-json"), artifacts);
  }

  @Test
  public void testListFails() throws ArtifactDownloaderException {
    doThrow(new ArtifactDownloaderException("error")).when(optionalComponentsService).loadArtifacts(VERSION);

    assertThrows(ArtifactDownloaderException.class, () -> {
      artifactController.list(VERSION);
    });
  }

  @Test
  public void testResolveSync() throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException {
    doReturn(getDependencyFiles()).when(artifactService).download(eq(GROUP), eq(ARTIFACT),
        eq(VERSION),
        isNull(), eq(false),
        isA(String.class));

    ArtifactAndDependendencies artifactAndDependendencies = artifactController.resolveSync(GROUP, ARTIFACT, VERSION, false,
        Collections.emptyList());

    assertValidArtifactAndDependendencies(artifactAndDependendencies);
    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));
  }

  @Test
  public void testResolveSyncWithExcludes() throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException {
    doReturn(getDependencyFiles()).when(artifactService).download(eq(GROUP), eq(ARTIFACT),
        eq(VERSION), isNull(), eq(false), eq(EXCLUDE_ARTIFACT));

    ArtifactAndDependendencies artifactAndDependendencies = artifactController.resolveSync(GROUP, ARTIFACT, VERSION, false,
        Collections.singletonList(EXCLUDE_ARTIFACT));

    assertValidArtifactAndDependendencies(artifactAndDependendencies);
    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), eq(EXCLUDE_ARTIFACT));
  }

  @Test
  public void testResolveSyncInvalidGroupId() throws ArtifactDownloaderException, DependenciesResolverException {
    doThrow(new InvalidGroupIdDownloaderException("invalid.group", GROUP)).when(artifactService).download("invalid.group", ARTIFACT,
        VERSION, null, false, "");

    DownloaderClientErrorException exception = assertThrows(DownloaderClientErrorException.class, () -> {
      artifactController.resolveSync("invalid.group", ARTIFACT, VERSION, false, Collections.emptyList());
    });
    assertEquals("HTTP 400 Bad Request", exception.getMessage());

    verify(artifactService).download(eq("invalid.group"), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));
  }

  @Test
  public void testResolveSyncUnresolvedDependency() throws ArtifactDownloaderException, DependenciesResolverException {
    doThrow(new ArtifactUnresolvedDownloaderException("Artifact could not be resolved")).when(artifactService).download(eq(GROUP),
        eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));

    DownloaderClientErrorException exception = assertThrows(DownloaderClientErrorException.class, () -> {
      artifactController.resolveSync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList());
    });
    assertEquals("HTTP 404 Not Found", exception.getMessage());

    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));
  }

  @Test
  public void testResolveAsync()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException, URISyntaxException {
    doReturn(getDependencyFiles()).when(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false),
        isA(String.class));

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.resolveAsync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(ArtifactAndDependendencies.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<ArtifactAndDependendencies>() {
      @Override
      public boolean matches(ArtifactAndDependendencies argument) {
        assertValidArtifactAndDependendencies(argument);
        return true;
      }
    }));
    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));
  }

  @Test
  public void testResolveAsyncWithExcludes() throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException, URISyntaxException {
    doReturn(getDependencyFiles()).when(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false),
        eq(EXCLUDE_ARTIFACT));

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.resolveAsync(GROUP, ARTIFACT, VERSION, false, Collections.singletonList(EXCLUDE_ARTIFACT), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(ArtifactAndDependendencies.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<ArtifactAndDependendencies>() {
      @Override
      public boolean matches(ArtifactAndDependendencies argument) {
        assertValidArtifactAndDependendencies(argument);
        return true;
      }
    }));
    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), eq(EXCLUDE_ARTIFACT));
  }

  @Test
  public void testResolveAsyncInvalidGroupId() throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException {
    doThrow(new InvalidGroupIdDownloaderException("invalid.group", GROUP)).when(artifactService).download(eq("invalid.group"), eq(ARTIFACT),
        eq(VERSION), isNull(), eq(false), isA(String.class));

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.resolveAsync("invalid.group", ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(DownloaderClientErrorException.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<DownloaderClientErrorException>() {
      @Override
      public boolean matches(DownloaderClientErrorException exception) {
        return exception.getMessage().contains("HTTP 400 Bad Request");
      }
    }));
    verify(artifactService).download(eq("invalid.group"), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));
  }

  @Test
  public void testResolveAsyncUnresolvedDependency()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException {
    doThrow(new ArtifactUnresolvedDownloaderException("")).when(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(),
        eq(false), isA(String.class));

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.resolveAsync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(DownloaderClientErrorException.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<DownloaderClientErrorException>() {
      @Override
      public boolean matches(DownloaderClientErrorException exception) {
        return exception.getMessage().contains("HTTP 404 Not Found");
      }
    }));
    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));
  }

  @Test
  public void testDownloadSync() throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException {
    doReturn(getDependencyFiles()).when(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false),
        isA(String.class));

    Response response = artifactController.downloadSync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList());

    assertValidResponse(response);

    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));
  }

  @Test
  public void testDownloadSyncWithExcludes() throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException {
    doReturn(getDependencyFiles()).when(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false),
        eq(EXCLUDE_ARTIFACT));

    Response response = artifactController.downloadSync(GROUP, ARTIFACT, VERSION, false, Collections.singletonList(EXCLUDE_ARTIFACT));

    assertValidResponse(response);

    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), eq(EXCLUDE_ARTIFACT));
  }

  @Test
  public void testDownloadSyncInvalidGroupId() throws ArtifactDownloaderException, DependenciesResolverException {
    DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    doReturn(dependenciesResolver).when(dependenciesResolverFactory).getResolver();

    doThrow(new InvalidGroupIdDownloaderException("invalid.group", GROUP)).when(artifactService).download("invalid.group", ARTIFACT,
        VERSION, null, false, "");

    BadRequestException exception = assertThrows(BadRequestException.class, () -> {
      artifactController.downloadSync("invalid.group", ARTIFACT, VERSION, false, Collections.emptyList());
    });
    assertEquals("[invalid.group] is not a valid group Id. It need to start with [com.adaptris]", exception.getMessage());

    verify(dependenciesResolver, never()).resolveArtifacts("com.invalid", ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testDownloadSyncUnresolvedDependency() throws ArtifactDownloaderException, DependenciesResolverException {
    doThrow(new ArtifactUnresolvedDownloaderException("Artifact could not be resolved")).when(artifactService).download(eq(GROUP),
        eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));


    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
      artifactController.downloadSync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList());
    });
    assertEquals("Artifact could not be resolved", exception.getMessage());

    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));
  }

  @Test
  public void testDownloadAsync()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException, URISyntaxException {
    doReturn(getDependencyFiles()).when(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false),
        isA(String.class));

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.downloadAsync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(Response.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<Response>() {
      @Override
      public boolean matches(Response argument) {
        assertValidResponse(argument);
        return true;
      }
    }));
    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));
  }

  @Test
  public void testDownloadAsyncWithExcludes()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException, URISyntaxException {
    doReturn(getDependencyFiles()).when(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false),
        eq(EXCLUDE_ARTIFACT));

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.downloadAsync(GROUP, ARTIFACT, VERSION, false, Collections.singletonList(EXCLUDE_ARTIFACT), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(Response.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<Response>() {
      @Override
      public boolean matches(Response argument) {
        assertValidResponse(argument);
        return true;
      }
    }));
    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), eq(EXCLUDE_ARTIFACT));
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
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<BadRequestException>() {
      @Override
      public boolean matches(BadRequestException exception) {
        return exception.getMessage().contains("[invalid.group] is not a valid group Id. It need to start with [com.adaptris]");
      }
    }));
    verify(dependenciesResolver, never()).resolveArtifacts("com.invalid", ARTIFACT, VERSION, null, getCacheDir(), false);
  }

  @Test
  public void testDownloadAsyncUnresolvedDependency()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException {
    doThrow(new ArtifactUnresolvedDownloaderException("Artifact could not be resolved")).when(artifactService).download(eq(GROUP),
        eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));

    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    artifactController.downloadAsync(GROUP, ARTIFACT, VERSION, false, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(NotFoundException.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<NotFoundException>() {
      @Override
      public boolean matches(NotFoundException exception) {
        return exception.getMessage().contains("Artifact could not be resolved");
      }
    }));
    verify(artifactService).download(eq(GROUP), eq(ARTIFACT), eq(VERSION), isNull(), eq(false), isA(String.class));
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
    assertNotNull(artifactAndDependendencies);
    assertEquals(ARTIFACT + ".jar", artifactAndDependendencies.getArtifact());
    assertEquals(1, artifactAndDependendencies.getDependencies().size());
    assertEquals("dependency.jar", artifactAndDependendencies.getDependencies().get(0));
  }

  private void assertValidResponse(Response response) {
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.valueOf("application/zip"), response.getMediaType());
    assertEquals("attachment; filename=\"" + ARTIFACT + "-" + VERSION + ".zip\"",
        response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION));
    // Check the files are in the response byte array
    String baosString = new String((byte[]) response.getEntity());
    assertTrue(baosString.contains("artifact.jar"));
    assertTrue(baosString.contains("dependency.jar"));
  }

}
