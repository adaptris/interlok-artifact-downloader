package com.adaptris.downloader.resolvers;

import java.util.List;

import com.adaptris.downloader.config.ArtifactDownloaderCredentialsProperties;

public interface DependenciesResolverProperties {

  String getRepoBaseUrl();

  void setRepoBaseUrl(String repoBaseUrl);

  List<String> getRepos();

  void setRepos(List<String> repos);

  ArtifactDownloaderCredentialsProperties getCredentials();

  void setCredentials(ArtifactDownloaderCredentialsProperties credentials);

  String getResolverLogLevel();

  void setResolverLogLevel(String resolverLogLevel);

}
