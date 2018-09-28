package com.adaptris.downloader.resources;

import org.junit.Assert;
import org.junit.Test;

public class UsageTest {

  @Test
  public void testNew() {
    Usage resource = new Usage();

    Assert.assertNull(resource.getLink());
  }

  @Test
  public void testNewWithData() {
    Usage resource = new Usage();
    resource.setLink("/{group}/{artifact}/{version}");

    Assert.assertEquals("/{group}/{artifact}/{version}", resource.getLink());
  }

}
