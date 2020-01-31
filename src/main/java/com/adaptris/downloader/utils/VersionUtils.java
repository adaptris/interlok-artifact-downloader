package com.adaptris.downloader.utils;

import org.springframework.util.StringUtils;

public class VersionUtils {

  private VersionUtils() {
  }

  public static boolean isSnapshot(String version) {
    return StringUtils.hasText(version) && version.toUpperCase().endsWith("-SNAPSHOT");
  }

}
