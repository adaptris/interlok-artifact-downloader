package com.adaptris.downloader.config;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.adaptris.downloader.resources.ClientError;

public class DownloaderClientErrorException extends ClientErrorException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2674666134423398645L;

  public DownloaderClientErrorException(Status status, String message) {
    this(status.getReasonPhrase(), message, status.getStatusCode());
  }

  public DownloaderClientErrorException(String error, String message, int status) {
    this(new ClientError(error, message, status, null));
  }

  public DownloaderClientErrorException(ClientError clientError) {
    super(Response.status(clientError.getStatus()).entity(clientError).build());
  }

}
