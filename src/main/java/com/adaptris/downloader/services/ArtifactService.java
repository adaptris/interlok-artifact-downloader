package com.adaptris.downloader.services;

import java.io.File;
import java.util.List;

import com.adaptris.downloader.config.ArtifactDownloaderException;

public interface ArtifactService {

  List<File> download(String groupId, String artifactId, String version, String url, boolean optional, String excludes)
      throws ArtifactDownloaderException;

}
