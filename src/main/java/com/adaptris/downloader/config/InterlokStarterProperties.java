package com.adaptris.downloader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("interlok.starter")
public class InterlokStarterProperties {

  private String baseFilesystemUrlNightly;

  public String getBaseFilesystemUrlNightly() {
    return baseFilesystemUrlNightly;
  }

  public void setBaseFilesystemUrlNightly(String baseFilesystemUrlNightly) {
    this.baseFilesystemUrlNightly = baseFilesystemUrlNightly;
  }

}
