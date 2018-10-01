package com.adaptris.downloader.resources;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class ClientErrorTest {

  @Test
  public void testNew() {
    ClientError resource = new ClientError("Not Found", 404, "/path/to/page");

    Assert.assertEquals("Not Found", resource.getError());
    Assert.assertEquals("Not Found", resource.getMessage());
    Assert.assertEquals(404, resource.getStatus());
    Assert.assertEquals("/path/to/page", resource.getPath());
    Assert.assertNotNull(resource.getTimestamp());
  }

  @Test
  public void testNewWithMessage() {
    ClientError resource = new ClientError("Not Found", "The resource could not be found", 404, "/path/to/page");

    Assert.assertEquals("Not Found", resource.getError());
    Assert.assertEquals("The resource could not be found", resource.getMessage());
    Assert.assertEquals(404, resource.getStatus());
    Assert.assertEquals("/path/to/page", resource.getPath());
    Assert.assertNotNull(resource.getTimestamp());
  }

  @Test
  public void testUpdateWithMessage() {
    ClientError resource = new ClientError("Not Found", "The resource could not be found", 404, "/path/to/page");
    resource.setError("Bad Request");
    resource.setMessage("The request was not correct");
    resource.setStatus(400);
    resource.setPath(null);
    Date now = new Date();
    resource.setTimestamp(now.getTime());

    Assert.assertEquals("Bad Request", resource.getError());
    Assert.assertEquals("The request was not correct", resource.getMessage());
    Assert.assertEquals(400, resource.getStatus());
    Assert.assertNull(resource.getPath());
    Assert.assertEquals(now.getTime(), resource.getTimestamp());
  }

}
