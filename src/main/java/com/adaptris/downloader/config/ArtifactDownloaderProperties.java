package com.adaptris.downloader.config;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.adaptris.downloader.resolvers.DependenciesResolverProperties;

@Component
@ConfigurationProperties("artifact.downloader")
public class ArtifactDownloaderProperties implements DependenciesResolverProperties {

  private String repoBaseUrl;
  private List<String> repos;
  private ArtifactDownloaderCredentialsProperties credentials;
  private String resolverLogLevel;
  private String destination;
  private List<String> excludes;

  @Override
  public String getRepoBaseUrl() {
    return repoBaseUrl;
  }

  @Override
  public void setRepoBaseUrl(String repoBaseUrl) {
    this.repoBaseUrl = repoBaseUrl;
  }

  @Override
  public List<String> getRepos() {
    return repos == null ? Collections.singletonList("public") : repos;
  }

  @Override
  public void setRepos(List<String> repos) {
    this.repos = repos;
  }

  @Override
  public ArtifactDownloaderCredentialsProperties getCredentials() {
    return credentials;
  }

  @Override
  public void setCredentials(ArtifactDownloaderCredentialsProperties credentials) {
    this.credentials = credentials;
  }

  @Override
  public String getResolverLogLevel() {
    return resolverLogLevel;
  }

  @Override
  public void setResolverLogLevel(String resolverLogLevel) {
    this.resolverLogLevel = resolverLogLevel;
  }

  public String getDestination() {
    return destination == null ? System.getProperty("user.home") : destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public List<String> getExcludes() {
    return excludes == null ? Collections.emptyList() : excludes;
  }

  public void setExcludes(List<String> excludes) {
    this.excludes = excludes;
  }

}
