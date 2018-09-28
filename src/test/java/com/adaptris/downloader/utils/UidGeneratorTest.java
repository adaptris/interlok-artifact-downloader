package com.adaptris.downloader.utils;

import org.junit.Assert;
import org.junit.Test;

public class UidGeneratorTest {

  @Test
  public void testGetUUID() throws Exception {
    String uuid = UidGenerator.getUUID();
    Assert.assertNotNull(uuid);
    Assert.assertEquals(5, uuid.split("-").length);
  }

}
