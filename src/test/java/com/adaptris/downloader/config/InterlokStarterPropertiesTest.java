package com.adaptris.downloader.config;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class InterlokStarterPropertiesTest {

  @Test
  public void testNew() {
    InterlokStarterProperties properties = new InterlokStarterProperties();

    assertNull(properties.getBaseFilesystemUrlNightly());
  }

  @Test
  public void testNewWithData() {
    InterlokStarterProperties properties = new InterlokStarterProperties();
    properties.setBaseFilesystemUrlNightly("https://development.adaptris.net/nightly_builds/v3.x/${today}/base-filesystem.zip");

    assertEquals("https://development.adaptris.net/nightly_builds/v3.x/${today}/base-filesystem.zip",
        properties.getBaseFilesystemUrlNightly());
  }

}
