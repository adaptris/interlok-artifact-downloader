package com.adaptris.downloader.controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Controller;

import com.adaptris.downloader.config.ArtifactDownloaderException;
import com.adaptris.downloader.config.ArtifactUnresolvedDownloaderException;
import com.adaptris.downloader.config.DownloaderClientErrorException;
import com.adaptris.downloader.config.InvalidGroupIdDownloaderException;
import com.adaptris.downloader.resources.ArtifactAndDependendencies;
import com.adaptris.downloader.resources.Usage;
import com.adaptris.downloader.services.ArtifactService;
import com.adaptris.downloader.utils.ZipUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "artifacts")
@Path("/artifacts")
@Controller
public class ArtifactController {

  private static final String GROUP = "group";
  private static final String GROUP_DESC = "Artifact group, e.g. com.adaptris";
  private static final String ARTIFACT = "artifact";
  private static final String ARTIFACT_DESC = "Artifact name";
  private static final String VERSION = "version";
  private static final String VERSION_DESC = "Artifact version";
  private static final String OPTIONAL = "optional";
  private static final String OPTIONAL_DESC = "Add optional dependencies";
  private static final String EXCLUDES = "excludes";
  private static final String EXCLUDES_DESC = "List of artifact to exclude separated with a comma";

  @Inject
  private ArtifactService artifactService;

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Display usage")
  // @Path("")
  public Usage usage() {
    Usage usage = new Usage();
    usage.setLink("/{group}/{artifact}/{version}");
    return usage;
  }

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Resolve the artifact and its dependencies. Normal synchronous request.")
  @Path("{group}/{artifact}/{version}/resolve/sync")
  public ArtifactAndDependendencies resolveSync(
      @ApiParam(name = GROUP, value = GROUP_DESC) @PathParam(GROUP) String group,
      @ApiParam(name = ARTIFACT, value = ARTIFACT_DESC) @PathParam(ARTIFACT) String artifact,
      @ApiParam(name = VERSION, value = VERSION_DESC) @PathParam(VERSION) String version,
      @ApiParam(name = OPTIONAL, value = OPTIONAL_DESC) @QueryParam(OPTIONAL) boolean optional,
      @ApiParam(name = EXCLUDES, value = EXCLUDES_DESC) @QueryParam(EXCLUDES) List<String> excludes)
          throws ArtifactDownloaderException {

    ArtifactAndDependendencies doResolve = doResolve(group, artifact, version, optional, excludes);
    return doResolve;
  }

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiOperation(value = "Resolve the artifact and its dependencies. Asynchronous request.")
  @Path("{group}/{artifact}/{version}/resolve")
  public void resolveAsync(
      @ApiParam(name = GROUP, value = GROUP_DESC) @PathParam(GROUP) String group,
      @ApiParam(name = ARTIFACT, value = ARTIFACT_DESC) @PathParam(ARTIFACT) String artifact,
      @ApiParam(name = VERSION, value = VERSION_DESC) @PathParam(VERSION) String version,
      @ApiParam(name = OPTIONAL, value = OPTIONAL_DESC) @QueryParam(OPTIONAL) boolean optional,
      @ApiParam(name = EXCLUDES, value = EXCLUDES_DESC) @QueryParam(EXCLUDES) List<String> excludes,
      @Suspended final AsyncResponse asyncResponse) throws ArtifactDownloaderException {

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          ArtifactAndDependendencies result = doResolve(group, artifact, version, optional, excludes);
          asyncResponse.resume(result);
        } catch (ArtifactDownloaderException | ClientErrorException expts) {
          asyncResponse.resume(expts);
        }
      }
    }).start();
  }

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({"application/zip"})
  @Path("{group}/{artifact}/{version}/sync")
  @ApiOperation(value = "Resolve and download a zip file with the artifact and its dependencies. Normal synchronous request.")
  public Response downloadSync(
      @ApiParam(name = GROUP, value = GROUP_DESC) @PathParam(GROUP) String group,
      @ApiParam(name = ARTIFACT, value = ARTIFACT_DESC) @PathParam(ARTIFACT) String artifact,
      @ApiParam(name = VERSION, value = VERSION_DESC) @PathParam(VERSION) String version,
      @ApiParam(name = OPTIONAL, value = OPTIONAL_DESC) @QueryParam(OPTIONAL) boolean optional,
      @ApiParam(name = EXCLUDES, value = EXCLUDES_DESC) @QueryParam(EXCLUDES) List<String> excludes)
          throws ArtifactDownloaderException {

    Response doDownload = doDownload(group, artifact, version, optional, excludes);
    return doDownload;
  }

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({"application/zip"})
  @Path("{group}/{artifact}/{version}")
  @ApiOperation(value = "Resolve and download a zip file with the artifact and its dependencies. Asynchronous request.")
  public void downloadAsync(@ApiParam(name = GROUP, value = GROUP_DESC) @PathParam(GROUP) String group,
      @ApiParam(name = ARTIFACT, value = ARTIFACT_DESC) @PathParam(ARTIFACT) String artifact,
      @ApiParam(name = VERSION, value = VERSION_DESC) @PathParam(VERSION) String version,
      @ApiParam(name = OPTIONAL, value = OPTIONAL_DESC) @QueryParam(OPTIONAL) boolean optional,
      @ApiParam(name = EXCLUDES, value = EXCLUDES_DESC) @QueryParam(EXCLUDES) List<String> excludes,
      @Suspended final AsyncResponse asyncResponse)
          throws ArtifactDownloaderException {

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Response result = doDownload(group, artifact, version, optional, excludes);
          asyncResponse.resume(result);
        } catch (ArtifactDownloaderException | ClientErrorException expts) {
          asyncResponse.resume(expts);
        }
      }
    }).start();
  }

  private ArtifactAndDependendencies doResolve(String group, String artifact, String version, boolean optional, List<String> excludes)
      throws ArtifactDownloaderException {
    try {
      List<File> artifacts = artifactServiceDownload(group, artifact, version, optional, excludes);

      ArtifactAndDependendencies artifactAndDependendencies = new ArtifactAndDependendencies();
      for (File artifactFile : artifacts) {
        if (artifactFile.getName().equals(artifact + ".jar")) {
          artifactAndDependendencies.setArtifact(artifactFile.getName());
        } else {
          artifactAndDependendencies.addDependency(artifactFile.getName());
        }
      }

      return artifactAndDependendencies;
    } catch (InvalidGroupIdDownloaderException igde) {
      throw new DownloaderClientErrorException(Status.BAD_REQUEST, igde.getLocalizedMessage());
    } catch (ArtifactUnresolvedDownloaderException aude) {
      throw new DownloaderClientErrorException(Status.NOT_FOUND, aude.getLocalizedMessage());
    }
  }

  private Response doDownload(String group, String artifact, String version, boolean optional, List<String> excludes)
      throws ArtifactDownloaderException {
    try {
      List<File> artifacts = artifactServiceDownload(group, artifact, version, optional, excludes);

      ByteArrayOutputStream baos = buildZip(artifacts);
      return buildZipResponse(baos.toByteArray(), artifact + "-" + version);
    } catch (InvalidGroupIdDownloaderException igde) {
      throw new BadRequestException(igde.getLocalizedMessage());
    } catch (ArtifactUnresolvedDownloaderException aude) {
      throw new NotFoundException(aude.getLocalizedMessage());
    }
  }

  private List<File> artifactServiceDownload(String group, String artifact, String version, boolean optional, List<String> excludes)
      throws ArtifactDownloaderException {
    String excludesStr = String.join(",", excludes);
    List<File> artifacts = artifactService.download(group, artifact, version, null, optional, excludesStr);
    return artifacts;
  }

  private ByteArrayOutputStream buildZip(List<File> artifacts) throws ArtifactDownloaderException {
    try {
      return ZipUtils.zipFileList(artifacts);
    } catch (IOException expts) {
      throw new ArtifactDownloaderException("Failed to zip files", expts);
    }
  }

  private Response buildZipResponse(byte[] byteArray, String name) {
    return Response.ok(byteArray).type("application/zip")
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + ".zip\"").build();
  }

}
