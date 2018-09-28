package com.adaptris.downloader.config;

/**
 * <p>
 * Standard <code>Exception</code> in the <code>downloader</code> package and sub-packages.
 * </p>
 */
public class ArtifactDownloaderException extends Exception {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -4497052462771826992L;

  /**
   * <p>
   * Creates a new instance with a reference to a previous <code>Exception</code>.
   * </p>
   *
   * @param cause a previous, causal <code>Exception</code>
   */
  public ArtifactDownloaderException(Throwable cause) {
    super(cause);
  }

  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   *
   * @param description description of the <code>Exception</code>
   */
  public ArtifactDownloaderException(String description) {
    super(description);
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
  public ArtifactDownloaderException(String description, Throwable cause) {
    super(description, cause);
  }

}
