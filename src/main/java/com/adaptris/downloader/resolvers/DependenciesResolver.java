package com.adaptris.downloader.resolvers;

import java.io.File;
import java.util.List;

public interface DependenciesResolver {

  String MAVEN_CENTRAL_BASE_URL = "http://central.maven.org/maven2";

  List<File> resolveArtifacts(String groupId, String artifactId, String version, String repoUrl,
      String cacheDir, String... excludes) throws DependenciesResolverException;

}
