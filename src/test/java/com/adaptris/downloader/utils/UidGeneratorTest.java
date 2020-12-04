package com.adaptris.downloader.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UidGeneratorTest {

  @Test
  public void testGetUUID() throws Exception {
    String uuid = UidGenerator.getUUID();
    assertNotNull(uuid);
    assertEquals(5, uuid.split("-").length);
  }

}
