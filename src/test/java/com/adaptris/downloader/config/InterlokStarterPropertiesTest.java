package com.adaptris.downloader.config;

import org.junit.Assert;
import org.junit.Test;

public class InterlokStarterPropertiesTest {

  @Test
  public void testNew() {
    InterlokStarterProperties properties = new InterlokStarterProperties();

    Assert.assertNull(properties.getBaseFilesystemUrlNightly());
  }

  @Test
  public void testNewWithData() {
    InterlokStarterProperties properties = new InterlokStarterProperties();
    properties.setBaseFilesystemUrlNightly("https://development.adaptris.net/nightly_builds/v3.x/${today}/base-filesystem.zip");

    Assert.assertEquals("https://development.adaptris.net/nightly_builds/v3.x/${today}/base-filesystem.zip",
        properties.getBaseFilesystemUrlNightly());
  }

}
