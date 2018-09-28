package com.adaptris.downloader.config;

public class InvalidGroupIdDownloaderException extends ArtifactDownloaderException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 3811526815879902629L;

  public InvalidGroupIdDownloaderException(String groupId, String expectedGroupIdPrefix) {
    super("[" + groupId + "] is not a valid group Id. It need to start with [" + expectedGroupIdPrefix + "]");
  }

}
