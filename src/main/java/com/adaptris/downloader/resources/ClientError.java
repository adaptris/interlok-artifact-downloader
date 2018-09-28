package com.adaptris.downloader.resources;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClientError {
  private String error; // Bad Request
  private String message; // Bad Request
  private String path; // /api/artifacts/com.adaptrisassdas/interlok-mqtt/asd/resolve
  private int status; // 400
  private long timestamp;

  public ClientError(String error, int status, String path) {
    this(error, error, status, path);
  }

  public ClientError(String error, String message, int status, String path) {
    this.error = error;
    this.message = message;
    this.status = status;
    this.path = path;
    timestamp = System.currentTimeMillis();
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

}
