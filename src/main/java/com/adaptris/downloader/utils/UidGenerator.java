package com.adaptris.downloader.utils;

import java.util.UUID;

public class UidGenerator {
  
  private UidGenerator() {}

  /**
   * Get the next unique ID.
   *
   * @return the next unique ID
   */
  public static String getUUID() {
    return UUID.randomUUID().toString();
  }

}
