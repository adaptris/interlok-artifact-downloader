package com.adaptris.downloader.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceUtils {

  private ResourceUtils() {
  }

  public static String toString(String name) throws IOException {
    InputStream inputStream = ResourceUtils.class.getClassLoader().getResourceAsStream(name);
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, length);
      }
      return outputStream.toString(StandardCharsets.UTF_8.name());
    }
  }

}
