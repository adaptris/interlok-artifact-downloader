package com.adaptris.downloader.resolvers;

import java.util.List;

import com.adaptris.downloader.utils.CollectionUtils;

/**
 * <p>
 * <code>Exception</code> generated when an exception is thrown while resolving dependencies
 * </p>
 */
public class DependenciesResolverException extends Exception {

  private List<String> dependencyProblemMessages;

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -7926209948269019887L;

  /**
   * <p>
   * Creates a new instance with a reference to a previous <code>Exception</code>.
   * </p>
   *
   * @param cause a previous, causal <code>Exception</code>
   */
  public DependenciesResolverException(Throwable cause) {
    super(cause);
  }

  /**
   * <p>
   * Creates a new instance with a reference to a previous <code>Exception</code> and a description
   * of the <code>Exception</code>.
   * </p>
   *
   * @param description of the <code>Exception</code>
   * @param cause previous <code>Exception</code>
   */
  public DependenciesResolverException(String description, Throwable cause) {
    super(description, cause);
  }

  /**
   * <p>
   * Creates a new instance with a list of dependency errors.
   * </p>
   *
   * @param dependencyErrors ist of dependency errors
   */
  public DependenciesResolverException(List<String> dependencyErrors) {
    super("Dependency resolved with errors.");
    dependencyProblemMessages = dependencyErrors;
  }

  public List<String> getDependencyProblemMessages() {
    return dependencyProblemMessages;
  }

  public boolean hasUnresolvedDependencyNotFound(String groupId, String artifactId, String version) {
    return !CollectionUtils.isEmpty(getDependencyProblemMessages())
        && getDependencyProblemMessages().contains("unresolved dependency: " + groupId + ":" + artifactId + ":" + version + ": not found");
  }

  public boolean hasDependencyProblemMessages() {
    return !CollectionUtils.isEmpty(getDependencyProblemMessages());
  }

}
