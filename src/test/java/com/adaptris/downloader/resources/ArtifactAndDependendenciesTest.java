package com.adaptris.downloader.resources;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class ArtifactAndDependendenciesTest {

  @Test
  public void testNew() {
    ArtifactAndDependendencies resource = new ArtifactAndDependendencies();

    Assert.assertNull(resource.getArtifact());
    Assert.assertNotNull(resource.getDependencies());
    Assert.assertEquals(0, resource.getDependencies().size());
  }

  @Test
  public void testNewWithData() {
    ArtifactAndDependendencies resource = new ArtifactAndDependendencies();
    resource.setArtifact("com.adaptris:artifact:version");
    resource.setDependencies(new ArrayList<>());
    resource.addDependency("com.adaptris:dependency:version");

    Assert.assertEquals("com.adaptris:artifact:version", resource.getArtifact());
    Assert.assertNotNull(resource.getDependencies());
    Assert.assertEquals(1, resource.getDependencies().size());
    Assert.assertEquals("com.adaptris:dependency:version", resource.getDependencies().get(0));
  }

}
