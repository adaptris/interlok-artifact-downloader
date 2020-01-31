package com.adaptris.downloader.controllers;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import com.adaptris.downloader.config.ArtifactDownloaderException;
import com.adaptris.downloader.services.BuildGradleFileService;
import com.adaptris.downloader.services.BuildGradleFileService.GradleItem;
import com.adaptris.downloader.utils.ZipUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "starter")
@Path("/starter")
@Controller
public class StarterController extends AbstractController {

  private static final String ARTIFACTS = "artifacts";
  private static final String ARTIFACTS_DESC = "List of artifacts to use in the starter file";
  protected static final String INTERLOK_GRADLE_FILES = "interlok-gradle-files-";

  @Inject
  private BuildGradleFileService buildGradleFileService;

  @GET
  @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  @Produces({ APPLICATION_ZIP })
  @Path("generate/{version}/sync")
  @ApiOperation(value = "List the available interlok arficat for a given version.")
  public Response generateSync(@ApiParam(name = VERSION, value = VERSION_DESC) @PathParam(VERSION) String version,
      @ApiParam(name = ARTIFACTS, value = ARTIFACTS_DESC) @QueryParam(ARTIFACTS) List<String> artifacts)
          throws ArtifactDownloaderException {

    return doGenerate(version, getFullArtifactList(artifacts));
  }

  @GET
  @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  @Produces({ APPLICATION_ZIP })
  @Path("generate/{version}")
  @ApiOperation(value = "List the available interlok arficat for a given version.")
  public void generateAsync(@ApiParam(name = VERSION, value = VERSION_DESC) @PathParam(VERSION) String version,
      @ApiParam(name = ARTIFACTS, value = ARTIFACTS_DESC) @QueryParam(ARTIFACTS) List<String> artifacts,
      @Suspended final AsyncResponse asyncResponse)
          throws ArtifactDownloaderException {

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Response result = doGenerate(version, getFullArtifactList(artifacts));
          asyncResponse.resume(result);
        } catch (ArtifactDownloaderException | ClientErrorException expts) {
          asyncResponse.resume(expts);
        }
      }
    }).start();
  }

  private List<String> getFullArtifactList(List<String> artifacts) {
    return artifacts.stream().filter(StringUtils::hasText).flatMap(s -> Stream.of(s.split(","))).filter(StringUtils::hasText).distinct()
        .collect(Collectors.toList());
  }

  private Response doGenerate(String version, List<String> artifacts)
      throws ArtifactDownloaderException {
    List<GradleItem> gradleItems = generateGradleItem(version, artifacts);
    byte[] byteArray = buildZipBytes(gradleItems);
    return buildZipResponse(byteArray, INTERLOK_GRADLE_FILES + version);
  }

  private byte[] buildZipBytes(List<GradleItem> gradleItems) throws ArtifactDownloaderException {
    byte[] byteArray = new byte[0];
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      zipList(gradleItems, outputStream);
      byteArray = outputStream.toByteArray();
    } catch (IOException expts) {
      throw new ArtifactDownloaderException("Failed to zip files", expts);
    }
    return byteArray;
  }

  public void zipList(List<GradleItem> gradleItems, OutputStream outputstream) throws IOException {
    try (ZipOutputStream outputStream = ZipUtils.createZip(new BufferedOutputStream(outputstream))) {
      for (GradleItem gradleItem : gradleItems) {
        addToZip(gradleItem, outputStream);
      }
    }
  }

  private void addToZip(GradleItem gradleItem, ZipOutputStream outputStream) throws IOException {
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(gradleItem.getPayload())) {
      ZipUtils.addToZip(gradleItem.getName(), inputStream, outputStream);
    }
  }

  private List<GradleItem> generateGradleItem(String version, List<String> artifacts) throws ArtifactDownloaderException {
    try {
      List<GradleItem> gradleItems = buildGradleFileService.generate(version, artifacts);
      return gradleItems;
    } catch (IOException expt) {
      throw new ArtifactDownloaderException("Failed to generate gradle files", expt);
    }
  }

}
