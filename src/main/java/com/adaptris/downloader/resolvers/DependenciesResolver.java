package com.adaptris.downloader.resolvers;

import java.io.File;
import java.util.List;

public interface DependenciesResolver {

  String MAVEN_CENTRAL_BASE_URL = "https://repo1.maven.org/maven2";

  List<File> resolveArtifacts(String groupId, String artifactId, String version, String repoUrl,
      String cacheDir, boolean addOptional, String... excludes) throws DependenciesResolverException;

}
