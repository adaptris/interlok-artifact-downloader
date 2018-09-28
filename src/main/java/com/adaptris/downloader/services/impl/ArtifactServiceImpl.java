package com.adaptris.downloader.services.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.adaptris.downloader.config.ArtifactDownloaderException;
import com.adaptris.downloader.config.ArtifactDownloaderProperties;
import com.adaptris.downloader.config.ArtifactUnresolvedDownloaderException;
import com.adaptris.downloader.config.InvalidGroupIdDownloaderException;
import com.adaptris.downloader.resolvers.DependenciesResolver;
import com.adaptris.downloader.resolvers.DependenciesResolverException;
import com.adaptris.downloader.resolvers.DependenciesResolverFactory;
import com.adaptris.downloader.services.ArtifactService;
import com.adaptris.downloader.utils.ExcludesUtils;

@Service
public class ArtifactServiceImpl implements ArtifactService {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  protected static final String EXPECTED_GROUP_ID = "com.adaptris";
  protected static final String EXPECTED_GROUP_ID_PATTERN = "^com\\.adaptris(?:$|\\..+$)";
  protected static final String INTERLOK_ARTIFACT_CACHE = ".interlok-artifact-cache";

  @Inject
  private ArtifactDownloaderProperties properties;
  @Inject
  private DependenciesResolverFactory dependenciesResolverFactory;

  @Override
  public List<File> download(String groupId, String artifactId, String version, String url, String excludes)
      throws ArtifactDownloaderException {
    validateGroupId(groupId);
    try {
      log.debug("Start downloading [{}:{}:{}] and its dependencies", groupId, artifactId, version);

      List<String> excludesList = buildExcludesList(excludes);

      return resolveArtifacts(groupId, artifactId, version, url, properties.getDestination(), excludesList);
    } catch (DependenciesResolverException dre) {
      if (dre.hasUnresolvedDependencyNotFound(groupId, artifactId, version)) {
        throw new ArtifactUnresolvedDownloaderException("Artifact could not be resolved");
      }
      if (dre.hasDependencyProblemMessages()) {
        throw new ArtifactUnresolvedDownloaderException(dre.getDependencyProblemMessages().toString());
      }
      throw new ArtifactDownloaderException(dre);
    }
  }

  private void validateGroupId(String groupId) throws InvalidGroupIdDownloaderException {
    if (!StringUtils.trimToEmpty(groupId).matches(EXPECTED_GROUP_ID_PATTERN)) {
      throw new InvalidGroupIdDownloaderException(groupId, EXPECTED_GROUP_ID);
    }
  }

  private List<String> buildExcludesList(String excludes) {
    List<String> excludesList = new ArrayList<>();
    ExcludesUtils.addToExcludesList(properties.getExcludes(), excludesList);
    ExcludesUtils.addToExcludesList(excludes, excludesList);
    log.debug("Excludes: {}", excludesList);
    return excludesList;
  }

  private List<File> resolveArtifacts(String groupId, String artifactId, String version, String repoUrl, String destination,
      List<String> excludes) throws DependenciesResolverException {
    DependenciesResolver dependenciesResolver = dependenciesResolverFactory.getResolver();
    String cacheDir = destination + File.separator + INTERLOK_ARTIFACT_CACHE;

    List<File> artifactFiles = dependenciesResolver.resolveArtifacts(groupId, artifactId, version, repoUrl, cacheDir,
        excludes.toArray(new String[excludes.size()]));

    log.debug("Artifact files: [{}]", artifactFiles);
    return artifactFiles;
  }

}
