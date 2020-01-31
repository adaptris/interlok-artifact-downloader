package com.adaptris.downloader.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class ResourceUtilsTest {

  @Test
  public void testToString() throws IOException, URISyntaxException {
    String licenseText = ResourceUtils.toString("gradle-templates/build.gradle.template");

    String expectedText = new String(
        Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("gradle-templates/build.gradle.template").toURI())),
        StandardCharsets.UTF_8);

    // Just the first few chars to not deal with the end of line characters
    assertEquals(expectedText.substring(0, 10), licenseText.substring(0, 10));
  }

  @Test
  public void testToStringNullName() throws IOException, URISyntaxException {
    assertThrows(NullPointerException.class, () -> {
      ResourceUtils.toString(null);
    });
  }

}
