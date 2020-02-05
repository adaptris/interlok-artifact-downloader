package com.adaptris.downloader.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.downloader.config.InterlokStarterProperties;
import com.adaptris.downloader.services.BuildGradleFileService;
import com.adaptris.downloader.services.BuildGradleFileService.GradleItem;

public class BuildGradleFileServiceImplTest {

  @Mock
  private InterlokStarterProperties properties;
  @InjectMocks
  private final BuildGradleFileService buildGradleFileService = new BuildGradleFileServiceImpl();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    doReturn("https://development.adaptris.net/nightly_builds/v3.x/${today}/base-filesystem.zip").when(properties)
    .getBaseFilesystemUrlNightly();
  }

  @Test
  public void testGenerate() throws Exception {
    List<GradleItem> gradleItems = buildGradleFileService.generate("3.9.2-RELEASE",
        Arrays.asList("interlok-json", "interlok-csv"));

    assertEquals("build.gradle", gradleItems.get(0).getName());
    String gradleFile = new String(gradleItems.get(0).getPayload(), StandardCharsets.UTF_8.name());

    assertEquals("gradle.properties", gradleItems.get(1).getName());
    Properties gradleProperties = new Properties();
    gradleProperties.load(new ByteArrayInputStream(gradleItems.get(1).getPayload()));

    assertFalse(gradleFile.isEmpty());
    assertTrue(gradleFile.contains("interlok-json"));
    assertTrue(gradleFile.contains("interlok-csv"));
    assertEquals("3.9.2-RELEASE", gradleProperties.get("interlokVersion"));
    assertNull(gradleProperties.get("interlokBaseFilesystemUrl"));
  }

  @Test
  public void testGenerateSnapshotVersion() throws Exception {
    List<GradleItem> gradleItems = buildGradleFileService.generate("3.9.3-SNAPSHOT",
        Arrays.asList("interlok-json", "interlok-csv"));

    assertEquals("build.gradle", gradleItems.get(0).getName());
    String gradleFile = new String(gradleItems.get(0).getPayload(), StandardCharsets.UTF_8.name());

    assertEquals("gradle.properties", gradleItems.get(1).getName());
    Properties gradleProperties = new Properties();
    gradleProperties.load(new ByteArrayInputStream(gradleItems.get(1).getPayload()));

    assertFalse(gradleFile.isEmpty());
    assertTrue(gradleFile.contains("interlok-json"));
    assertTrue(gradleFile.contains("interlok-csv"));
    assertEquals("3.9.3-SNAPSHOT", gradleProperties.get("interlokVersion"));
    assertNotNull(gradleProperties.get("interlokBaseFilesystemUrl"));
  }

}
