package com.adaptris.downloader.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class UsageTest {

  @Test
  public void testNew() {
    Usage resource = new Usage();

    assertNull(resource.getLink());
  }

  @Test
  public void testNewWithData() {
    Usage resource = new Usage();
    resource.setLink("/{group}/{artifact}/{version}");

    assertEquals("/{group}/{artifact}/{version}", resource.getLink());
  }

}
