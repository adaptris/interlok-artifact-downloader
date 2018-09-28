package com.adaptris.downloader.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ZipUtilsTest {

  private final URL resource;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  public ZipUtilsTest() {
    resource = getClass().getClassLoader().getResource("zip-test");
  }

  @After
  public void afterTest() throws Exception {
    File testFileZip = Paths.get(resource.toURI()).resolve("zip-test-file.txt" + ZipUtils.ZIP).toFile();
    FileUtils.deleteQuietly(testFileZip);
  }

  @Test
  public void testZipFileList() throws Exception {
    File testFile = Paths.get(resource.toURI()).resolve("zip-test-file.txt").toFile();
    ByteArrayOutputStream baos = ZipUtils.zipFileList(Collections.<File> singletonList(testFile));
    // TODO better assert for baos
    Assert.assertNotNull(baos);
    Assert.assertEquals(161, baos.size());
    String baosString = new String(baos.toByteArray());
    Assert.assertTrue(baosString.contains("zip-test-file.txt"));
  }

  @Test
  public void testZipFileListWithOutputStream() throws Exception {
    File testFile = Paths.get(resource.toURI()).resolve("zip-test-file.txt").toFile();
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ZipUtils.zipFileList(Collections.<File>singletonList(testFile), baos);
      // TODO better assert for baos
      Assert.assertNotNull(baos);
      Assert.assertEquals(161, baos.size());
      String baosString = new String(baos.toByteArray());
      Assert.assertTrue(baosString.contains("zip-test-file.txt"));
    }
  }

  @Test
  public void testZipFileListNullFile() throws Exception {
    expectedEx.expect(NullPointerException.class);

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ZipUtils.zipFileList(Collections.<File>singletonList(null), baos);
    }
  }

  @Test
  public void testZipFileListDuplicatedFile() throws Exception {
    expectedEx.expect(ZipException.class);
    expectedEx.expectMessage("duplicate entry: zip-test-file.txt");

    File testFile = Paths.get(resource.toURI()).resolve("zip-test-file.txt").toFile();
    List<File> files = new ArrayList<>();
    files.add(testFile);
    files.add(testFile);
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ZipUtils.zipFileList(files, baos);
    }
  }

}
