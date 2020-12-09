package com.adaptris.downloader.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ZipUtilsTest {

  private final URL resource;

  public ZipUtilsTest() {
    resource = getClass().getClassLoader().getResource("zip-test");
  }

  @AfterEach
  public void afterTest() throws Exception {
    File testFileZip = Paths.get(resource.toURI()).resolve("zip-test-file.txt" + ZipUtils.ZIP).toFile();
    FileUtils.deleteQuietly(testFileZip);
  }

  @Test
  public void testZipFileList() throws Exception {
    File testFile = Paths.get(resource.toURI()).resolve("zip-test-file.txt").toFile();
    ByteArrayOutputStream baos = ZipUtils.zipFileList(Collections.<File> singletonList(testFile));
    // TODO better assert for baos
    assertNotNull(baos);
    assertEquals(161, baos.size());
    String baosString = new String(baos.toByteArray());
    assertTrue(baosString.contains("zip-test-file.txt"));
  }

  @Test
  public void testZipFileListWithOutputStream() throws Exception {
    File testFile = Paths.get(resource.toURI()).resolve("zip-test-file.txt").toFile();
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ZipUtils.zipFileList(Collections.<File>singletonList(testFile), baos);
      // TODO better assert for baos
      assertNotNull(baos);
      assertEquals(161, baos.size());
      String baosString = new String(baos.toByteArray());
      assertTrue(baosString.contains("zip-test-file.txt"));
    }
  }

  @Test
  public void testZipFileListNullFile() throws Exception {
    assertThrows(NullPointerException.class, () -> {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        ZipUtils.zipFileList(Collections.<File> singletonList(null), baos);
      }
    });
  }

  @Test
  public void testZipFileListDuplicatedFile() throws Exception {
    File testFile = Paths.get(resource.toURI()).resolve("zip-test-file.txt").toFile();
    List<File> files = new ArrayList<>();
    files.add(testFile);
    files.add(testFile);

    ZipException exception = assertThrows(ZipException.class, () -> {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        ZipUtils.zipFileList(files, baos);
      }
    });
    assertEquals("duplicate entry: zip-test-file.txt", exception.getMessage());
  }

}
