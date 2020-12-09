package com.adaptris.downloader.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class ArtifactAndDependendenciesTest {

  @Test
  public void testNew() {
    ArtifactAndDependendencies resource = new ArtifactAndDependendencies();

    assertNull(resource.getArtifact());
    assertNotNull(resource.getDependencies());
    assertEquals(0, resource.getDependencies().size());
  }

  @Test
  public void testNewWithData() {
    ArtifactAndDependendencies resource = new ArtifactAndDependendencies();
    resource.setArtifact("com.adaptris:artifact:version");
    resource.setDependencies(new ArrayList<>());
    resource.addDependency("com.adaptris:dependency:version");

    assertEquals("com.adaptris:artifact:version", resource.getArtifact());
    assertNotNull(resource.getDependencies());
    assertEquals(1, resource.getDependencies().size());
    assertEquals("com.adaptris:dependency:version", resource.getDependencies().get(0));
  }

}
