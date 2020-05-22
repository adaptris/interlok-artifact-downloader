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
import com.adaptris.downloader.services.OptionalComponentsService;
import com.adaptris.downloader.utils.ZipUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

@Tags(@Tag(name = "artifacts"))
@Path("/artifacts")
@Controller
public class ArtifactController extends AbstractController {

  private static final String GROUP = "group";
  private static final String GROUP_DESC = "Artifact group, e.g. com.adaptris";
  private static final String ARTIFACT = "artifact";
  private static final String ARTIFACT_DESC = "Artifact name";
  private static final String OPTIONAL = "optional";
  private static final String OPTIONAL_DESC = "Add optional dependencies";
  private static final String EXCLUDES = "excludes";
  private static final String EXCLUDES_DESC = "List of artifact to exclude separated with a comma";

  @Inject
  private ArtifactService artifactService;
  @Inject
  private OptionalComponentsService optionalComponentsService;

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Operation(description = "Display usage")
  // @Path("")
  public Usage usage() {
    Usage usage = new Usage();
    usage.setLink("/{group}/{artifact}/{version}");
    return usage;
  }

  @GET
  @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  @Path("{version}")
  @Operation(description = "List the available interlok artifacts for a given version.")
  public List<String> list(@Parameter(name = VERSION, description = VERSION_DESC) @PathParam(VERSION) String version)
      throws ArtifactDownloaderException {
    return optionalComponentsService.loadArtifacts(version);
  }

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Operation(description = "Resolve the artifact and its dependencies. Normal synchronous request.")
  @Path("{group}/{artifact}/{version}/resolve/sync")
  public ArtifactAndDependendencies resolveSync(
      @Parameter(name = GROUP, description = GROUP_DESC) @PathParam(GROUP) String group,
      @Parameter(name = ARTIFACT, description = ARTIFACT_DESC) @PathParam(ARTIFACT) String artifact,
      @Parameter(name = VERSION, description = VERSION_DESC) @PathParam(VERSION) String version,
      @Parameter(name = OPTIONAL, description = OPTIONAL_DESC) @QueryParam(OPTIONAL) boolean optional,
      @Parameter(name = EXCLUDES, description = EXCLUDES_DESC) @QueryParam(EXCLUDES) List<String> excludes)
          throws ArtifactDownloaderException {

    ArtifactAndDependendencies doResolve = doResolve(group, artifact, version, optional, excludes);
    return doResolve;
  }

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Operation(description = "Resolve the artifact and its dependencies. Asynchronous request.")
  @Path("{group}/{artifact}/{version}/resolve")
  public void resolveAsync(
      @Parameter(name = GROUP, description = GROUP_DESC) @PathParam(GROUP) String group,
      @Parameter(name = ARTIFACT, description = ARTIFACT_DESC) @PathParam(ARTIFACT) String artifact,
      @Parameter(name = VERSION, description = VERSION_DESC) @PathParam(VERSION) String version,
      @Parameter(name = OPTIONAL, description = OPTIONAL_DESC) @QueryParam(OPTIONAL) boolean optional,
      @Parameter(name = EXCLUDES, description = EXCLUDES_DESC) @QueryParam(EXCLUDES) List<String> excludes,
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
  @Produces({APPLICATION_ZIP})
  @Path("{group}/{artifact}/{version}/sync")
  @Operation(description = "Resolve and download a zip file with the artifact and its dependencies. Normal synchronous request.")
  public Response downloadSync(
      @Parameter(name = GROUP, description = GROUP_DESC) @PathParam(GROUP) String group,
      @Parameter(name = ARTIFACT, description = ARTIFACT_DESC) @PathParam(ARTIFACT) String artifact,
      @Parameter(name = VERSION, description = VERSION_DESC) @PathParam(VERSION) String version,
      @Parameter(name = OPTIONAL, description = OPTIONAL_DESC) @QueryParam(OPTIONAL) boolean optional,
      @Parameter(name = EXCLUDES, description = EXCLUDES_DESC) @QueryParam(EXCLUDES) List<String> excludes)
          throws ArtifactDownloaderException {

    Response doDownload = doDownload(group, artifact, version, optional, excludes);
    return doDownload;
  }

  @GET
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({APPLICATION_ZIP})
  @Path("{group}/{artifact}/{version}")
  @Operation(description = "Resolve and download a zip file with the artifact and its dependencies. Asynchronous request.")
  public void downloadAsync(@Parameter(name = GROUP, description = GROUP_DESC) @PathParam(GROUP) String group,
      @Parameter(name = ARTIFACT, description = ARTIFACT_DESC) @PathParam(ARTIFACT) String artifact,
      @Parameter(name = VERSION, description = VERSION_DESC) @PathParam(VERSION) String version,
      @Parameter(name = OPTIONAL, description = OPTIONAL_DESC) @QueryParam(OPTIONAL) boolean optional,
      @Parameter(name = EXCLUDES, description = EXCLUDES_DESC) @QueryParam(EXCLUDES) List<String> excludes,
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

      return filesToZipResponse(artifacts, artifact + "-" + version);
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

  private Response filesToZipResponse(List<File> files, String zipName) throws ArtifactDownloaderException {
    ByteArrayOutputStream baos = buildZip(files);
    return buildZipResponse(baos.toByteArray(), zipName);
  }

  private ByteArrayOutputStream buildZip(List<File> artifacts) throws ArtifactDownloaderException {
    try {
      return ZipUtils.zipFileList(artifacts);
    } catch (IOException expts) {
      throw new ArtifactDownloaderException("Failed to zip files", expts);
    }
  }

}
