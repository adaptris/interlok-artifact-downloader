package com.adaptris.downloader.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class ClientErrorTest {

  @Test
  public void testNew() {
    ClientError resource = new ClientError("Not Found", 404, "/path/to/page");

    assertEquals("Not Found", resource.getError());
    assertEquals("Not Found", resource.getMessage());
    assertEquals(404, resource.getStatus());
    assertEquals("/path/to/page", resource.getPath());
    assertNotNull(resource.getTimestamp());
  }

  @Test
  public void testNewWithMessage() {
    ClientError resource = new ClientError("Not Found", "The resource could not be found", 404, "/path/to/page");

    assertEquals("Not Found", resource.getError());
    assertEquals("The resource could not be found", resource.getMessage());
    assertEquals(404, resource.getStatus());
    assertEquals("/path/to/page", resource.getPath());
    assertNotNull(resource.getTimestamp());
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

    assertEquals("Bad Request", resource.getError());
    assertEquals("The request was not correct", resource.getMessage());
    assertEquals(400, resource.getStatus());
    assertNull(resource.getPath());
    assertEquals(now.getTime(), resource.getTimestamp());
  }

}
