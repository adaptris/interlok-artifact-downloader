package com.adaptris.downloader.services;

import java.util.List;

import com.adaptris.downloader.config.ArtifactDownloaderException;

public interface OptionalComponentsService {

  List<String> loadArtifacts(String version) throws ArtifactDownloaderException;

}
