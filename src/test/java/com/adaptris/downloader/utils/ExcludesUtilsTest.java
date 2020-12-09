package com.adaptris.downloader.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ExcludesUtilsTest {

  private final URL resource;

  public ExcludesUtilsTest() {
    resource = getClass().getClassLoader().getResource("excludes-test");
  }

  @Test
  public void testAddCollection() {
    List<String> excludes = new ArrayList<>();

    List<String> excludesToAdd = new ArrayList<>();
    excludesToAdd.add("com.adaptris:toexclude");
    excludesToAdd.add("*:toexclude");
    ExcludesUtils.addToExcludesList(excludesToAdd, excludes);

    assertEquals(excludesToAdd.size(), excludes.size());
    assertEquals(excludesToAdd.get(0), excludes.get(0));
    assertEquals(excludesToAdd.get(1), excludes.get(1));
  }

  @Test
  public void testAddCollectionDir() throws URISyntaxException {
    List<String> excludes = new ArrayList<>();

    File libDir = new File(resource.toURI());
    List<String> excludesToAdd = new ArrayList<>();
    excludesToAdd.add(libDir.getAbsolutePath());
    ExcludesUtils.addToExcludesList(excludesToAdd, excludes);

    assertEquals(1, excludes.size());
    assertEquals("*:adp-core-dummy", excludes.get(0));
  }

  @Test
  public void testAddString() {
    List<String> excludes = new ArrayList<>();

    String excludesToAdd = "com.adaptris:toexclude,*:toexclude";
    ExcludesUtils.addToExcludesList(excludesToAdd, excludes);

    assertEquals(2, excludes.size());
    assertEquals("com.adaptris:toexclude", excludes.get(0));
    assertEquals("*:toexclude", excludes.get(1));
  }

  @Test
  public void testAddStringDir() throws URISyntaxException {
    List<String> excludes = new ArrayList<>();

    File libDir = new File(resource.toURI());
    String excludesToAdd = libDir.getAbsolutePath();
    ExcludesUtils.addToExcludesList(excludesToAdd, excludes);

    assertEquals(1, excludes.size());
    assertEquals("*:adp-core-dummy", excludes.get(0));
  }

}
