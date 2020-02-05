package com.adaptris.downloader.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.adaptris.downloader.config.ArtifactDownloaderException;
import com.adaptris.downloader.config.InterlokStarterProperties;
import com.adaptris.downloader.resolvers.DependenciesResolverException;
import com.adaptris.downloader.services.BuildGradleFileService;
import com.adaptris.downloader.services.impl.BuildGradleFileServiceImpl;

public class StarterControllerTest {

  private static final String VERSION = "version";

  @Mock
  private InterlokStarterProperties properties;
  @Spy
  private BuildGradleFileService buildGradleFileService = new BuildGradleFileServiceImpl();
  @InjectMocks
  private final StarterController starterController = new StarterController();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGenerateSync() throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException {
    Response response = starterController.generateSync(VERSION, Collections.singletonList("interlok-json"));

    assertValidResponse(response);
  }

  @Test
  public void testGenerateSyncNoArtifact() throws ArtifactDownloaderException, DependenciesResolverException {
    Response response = starterController.generateSync(VERSION, Collections.emptyList());

    assertValidResponse(response);
  }

  @Test
  public void testGenerateSyncFails() throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException, IOException {
    List<String> artifacts = Collections.singletonList("interlok-json");

    doThrow(new IOException("error")).when(buildGradleFileService).generate(eq(VERSION), argThat(new ArgumentMatcher<List<String>>() {
      @Override
      public boolean matches(List<String> argument) {
        return List.class.cast(argument).contains("interlok-json");
      }
    }));

    ArtifactDownloaderException exceptions = assertThrows(ArtifactDownloaderException.class, () -> {
      starterController.generateSync(VERSION, artifacts);
    });
    assertEquals("Failed to generate gradle files", exceptions.getMessage());
  }

  @Test
  public void testGenerateAsync()
      throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException, URISyntaxException {
    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    starterController.generateAsync(VERSION, Collections.singletonList("interlok-json"), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(Response.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<Response>() {
      @Override
      public boolean matches(Response argument) {
        assertValidResponse(argument);
        return true;
      }
    }));
  }

  @Test
  public void testGenerateAsyncFails()
      throws ArtifactDownloaderException, DependenciesResolverException, URISyntaxException, InterruptedException, IOException {
    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    List<String> artifacts = Collections.singletonList("interlok-json");

    doThrow(new IOException("error")).when(buildGradleFileService).generate(eq(VERSION), argThat(new ArgumentMatcher<List<String>>() {
      @Override
      public boolean matches(List<String> argument) {
        return List.class.cast(argument).contains("interlok-json");
      }
    }));

    starterController.generateAsync(VERSION, artifacts, asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(argThat(new ArgumentMatcher<ArtifactDownloaderException>() {
      @Override
      public boolean matches(ArtifactDownloaderException argument) {
        return ArtifactDownloaderException.class.cast(argument).getMessage().equals("Failed to generate gradle files");
      }
    }));
  }

  @Test
  public void testGenerateAsyncInvalidGroupId() throws ArtifactDownloaderException, DependenciesResolverException, InterruptedException {
    AsyncResponse asyncResponse = mock(AsyncResponse.class);

    starterController.generateAsync(VERSION, Collections.emptyList(), asyncResponse);

    Thread.sleep(400);

    verify(asyncResponse).resume(isA(Response.class));
    verify(asyncResponse).resume(argThat(new ArgumentMatcher<Response>() {
      @Override
      public boolean matches(Response argument) {
        assertValidResponse(argument);
        return true;
      }
    }));
  }

  private void assertValidResponse(Response response) {
    Assert.assertNotNull(response);
    Assert.assertEquals(200, response.getStatus());
    Assert.assertEquals(MediaType.valueOf("application/zip"), response.getMediaType());
    Assert.assertEquals("attachment; filename=\"" + StarterController.INTERLOK_GRADLE_FILES + VERSION + ".zip\"",
        response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION));
    // Check the files are in the response byte array
    String baosString = new String((byte[]) response.getEntity());
    Assert.assertTrue(baosString.contains("build.gradle"));
    Assert.assertTrue(baosString.contains("gradle.properties"));
  }

}
